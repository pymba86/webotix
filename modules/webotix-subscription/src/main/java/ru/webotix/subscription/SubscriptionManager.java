package ru.webotix.subscription;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.market.data.api.MarketDataSubscription;
import ru.webotix.market.data.api.MarketDataType;
import ru.webotix.market.data.api.TickerSpec;
import ru.webotix.utils.SafelyClose;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class SubscriptionManager implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);

    private final SubscriptionAccess subscriptionAccess;
    private final ExchangeEventRegistry exchangeEventRegistry;
    private final Transactionally transactionally;

    private volatile ExchangeEventRegistry.ExchangeEventSubscription subscription;

    @Inject
    SubscriptionManager(
            SubscriptionAccess subscriptionAccess,
            ExchangeEventRegistry exchangeEventRegistry,
            Transactionally transactionally) {
        this.subscriptionAccess = subscriptionAccess;
        this.exchangeEventRegistry = exchangeEventRegistry;
        this.transactionally = transactionally;
    }

    @Override
    public void start() throws Exception {
        subscription = exchangeEventRegistry.subscribe();
        transactionally.run(Transactionally.READ_ONLY_UNIT, this::update);
    }

    @Override
    public void stop() throws Exception {
        SafelyClose.the(subscription);
    }

    private synchronized void update() {
        Set<MarketDataSubscription> all =
                subscriptionAccess.all().stream()
                        .flatMap(s -> subscriptionsFor(s).stream())
                        .collect(Collectors.toSet());
        LOGGER.info("Updating permanent subscriptions to {}", all);
        subscription = subscription.replace(all);
    }

    void add(TickerSpec spec) {
        subscriptionAccess.add(spec);
        update();
    }

    void remove(TickerSpec spec) {
        subscriptionAccess.remove(spec);
        update();
    }

    private Collection<MarketDataSubscription> subscriptionsFor(TickerSpec spec) {
        return ImmutableList.of(
                MarketDataSubscription.create(spec, MarketDataType.Ticker),
                MarketDataSubscription.create(spec, MarketDataType.OrderBook),
                MarketDataSubscription.create(spec, MarketDataType.OpenOrders),
                MarketDataSubscription.create(spec, MarketDataType.UserTrade),
                MarketDataSubscription.create(spec, MarketDataType.Balance),
                MarketDataSubscription.create(spec, MarketDataType.Trades),
                MarketDataSubscription.create(spec, MarketDataType.Order));
    }
}
