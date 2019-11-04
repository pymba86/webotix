package ru.webotix.exchange;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.dto.meta.RateLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.common.api.WebotixConfiguration;
import ru.webotix.exchange.api.ExchangeConfiguration;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static info.bitrich.xchangestream.service.ConnectableService.BEFORE_CONNECTION_HANDLER;
import static info.bitrich.xchangestream.util.Events.BEFORE_API_CALL_HANDLER;
import static java.util.stream.Stream.concat;

@Singleton
public class ExchangeServiceImpl implements ExchangeService {

    private static final ExchangeConfiguration VANILLA_CONFIG = new ExchangeConfiguration();
    private static final RateLimit DEFAULT_RATE = new RateLimit(1, 3, TimeUnit.SECONDS);
    private static final RateLimit[] NO_LIMITS = new RateLimit[0];
    private static final Duration THROTTLE_DURATION = Duration.ofMinutes(2);

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    private final WebotixConfiguration configuration;

    private final LoadingCache<String, Exchange> exchanges = CacheBuilder.newBuilder().build(new CacheLoader<String, Exchange>() {
        @Override
        public Exchange load(String name) throws Exception {
            final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges() == null
                    ? VANILLA_CONFIG
                    : MoreObjects.firstNonNull(configuration.getExchanges().get(name), VANILLA_CONFIG);
            if (exchangeConfiguration.isAuthenticated()) {
                return privateApi(name, exchangeConfiguration);
            } else {
                return publicApi(name, exchangeConfiguration);
            }
        }

        private Exchange publicApi(String name, ExchangeConfiguration exchangeConfiguration) {
            try {
                LOGGER.debug("No API connection details.  Connecting to public API: {}", name);
                final ExchangeSpecification exSpec = createExchangeSpecification(name, exchangeConfiguration);
                return createExchange(exSpec);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
                throw new IllegalArgumentException("Failed to connect to exchange [" + name + "]", e);
            }
        }

        private Exchange privateApi(String name, final ExchangeConfiguration exchangeConfiguration) {
            try {
                LOGGER.debug("Connecting to private API: {}", name);
                final ExchangeSpecification exSpec = createExchangeSpecification(name, exchangeConfiguration);
                exSpec.setUserName(exchangeConfiguration.getUserName());
                exSpec.setApiKey(exchangeConfiguration.getApiKey());
                exSpec.setSecretKey(exchangeConfiguration.getSecretKey());
                exSpec.setExchangeSpecificParametersItem("passphrase", exchangeConfiguration.getPassphrase());
                return createExchange(exSpec);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
                throw new IllegalArgumentException("Failed to connect to exchange [" + name + "]", e);
            }
        }

        private Exchange createExchange(final ExchangeSpecification exSpec) {
            if (exSpec.getExchangeClassName().contains("Streaming")) {
                return StreamingExchangeFactory.INSTANCE.createExchange(exSpec);
            } else {
                return ExchangeFactory.INSTANCE.createExchange(exSpec);
            }
        }

        private ExchangeSpecification createExchangeSpecification(String exchangeName, ExchangeConfiguration exchangeConfiguration) throws InstantiationException, IllegalAccessException {
            final ExchangeSpecification exSpec = ExchangeFactory.INSTANCE
                    .createExchangeWithoutSpecification(Exchanges.friendlyNameToClass(exchangeName))
                    .getDefaultExchangeSpecification();
            if (exchangeConfiguration.isSandbox()) {
                LOGGER.info("Using {} sandbox", exchangeName);
                exSpec.setExchangeSpecificParametersItem("Use_Sandbox", true);
            }
            exSpec.setShouldLoadRemoteMetaData(exchangeConfiguration.isLoadRemoteData());
            RateLimiter rateLimiter = RateLimiter.create(0.25); // TODO make this exchange specific
            exSpec.setExchangeSpecificParametersItem(
                    BEFORE_CONNECTION_HANDLER,
                    (Runnable) rateLimiter::acquire
            );
            exSpec.setExchangeSpecificParametersItem(
                    BEFORE_API_CALL_HANDLER,
                    (Runnable) rateLimiter::acquire
            );
            return exSpec;
        }
    });

    private final LoadingCache<String, RateController> rateLimiters = CacheBuilder.newBuilder().build(new CacheLoader<String, RateController>() {
        @Override
        public RateController load(String exchangeName) throws Exception {
            try {
                ExchangeMetaData metaData = get(exchangeName).getExchangeMetaData();
                return concat(asStream(metaData.getPrivateRateLimits()), asStream(metaData.getPublicRateLimits()))
                        .max(Ordering.natural().onResultOf(RateLimit::getPollDelayMillis))
                        .map(rateLimit -> {
                            LOGGER.info("Rate limit for [{}] is {}", exchangeName, rateLimit);
                            return asLimiter(rateLimit);
                        })
                        .map(rateLimiter -> new RateController(exchangeName, rateLimiter, THROTTLE_DURATION))
                        .orElseGet(() -> {
                            LOGGER.info("Rate limit for [{}] is unknown, defaulting to: {}", exchangeName, DEFAULT_RATE);
                            return new RateController(exchangeName, asLimiter(DEFAULT_RATE), THROTTLE_DURATION);
                        });
            } catch (Exception e) {
                LOGGER.warn("Failed to fetch rate limit for [" + exchangeName + "], defaulting to " + DEFAULT_RATE, e);
                return new RateController(exchangeName, asLimiter(DEFAULT_RATE), THROTTLE_DURATION);
            }
        }

        private Stream<RateLimit> asStream(RateLimit[] limits) {
            return Arrays.stream(MoreObjects.firstNonNull(limits, NO_LIMITS));
        }

        private RateLimiter asLimiter(RateLimit rateLimit) {
            double permitsPerSecond = ((double)rateLimit.calls / rateLimit.timeUnit.toSeconds(rateLimit.timeSpan)) - 0.01;
            LOGGER.debug("Permits per second = {}", permitsPerSecond);
            return RateLimiter.create(permitsPerSecond);
        }
    });

    @Inject
    public ExchangeServiceImpl(WebotixConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Collection<String> getExchanges() {
        return ImmutableSet.<String>builder()
                .addAll(Exchanges.EXCHANGE_TYPES.get().stream()
                        .map(Exchanges::classToFriendlyName).collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public Exchange get(String name) {
        try {
            return exchanges.getUnchecked(name);
        } catch (UncheckedExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw e;
        }
    }

    @Override
    public boolean isAuthenticated(String name) {
        if (configuration.getExchanges() == null)
            return false;
        ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(name);
        return exchangeConfiguration != null &&
                StringUtils.isNotEmpty(exchangeConfiguration.getApiKey());
    }

    @Override
    public RateController rateController(String name) {
        return rateLimiters.getUnchecked(name);
    }
}
