package ru.webotix.exchange;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.exchange.api.ExchangeEventRegistry.ExchangeEventSubscription;

public class PaperTradeService implements TradeService {

    private final PaperAccountService paperAccountService;

    private ExchangeEventSubscription subscription;

    private String exchange;

    private PaperTradeService(String exchange,
                              ExchangeEventRegistry exchangeEventRegistry,
                              PaperAccountService paperAccountService) {
        this.paperAccountService = paperAccountService;
        this.subscription = exchangeEventRegistry.subscribe();
        this.exchange = exchange;
    }

    @Singleton
    public static class Factory implements TradeServiceFactory {

        private static final Logger log = LoggerFactory.getLogger(Factory.class);

        private final ExchangeEventRegistry exchangeEventRegistry;
        private final PaperAccountService.Factory accountServiceFactory;

        private final LoadingCache<String, TradeService> services =
                CacheBuilder.newBuilder()
                        .initialCapacity(1000)
                        .build(
                                new CacheLoader<String, TradeService>() {
                                    @Override
                                    public TradeService load(String exchange) throws Exception {
                                        log.debug(
                                                "No API connection details for {}. Using paper trading.", exchange);
                                        return new PaperTradeService(
                                                exchange,
                                                exchangeEventRegistry,
                                                accountServiceFactory.getForExchange(exchange));
                                    }
                                });

        @Inject
        Factory(
                ExchangeEventRegistry exchangeEventRegistry,
                PaperAccountService.Factory accountServiceFactory) {
            this.exchangeEventRegistry = exchangeEventRegistry;
            this.accountServiceFactory = accountServiceFactory;
        }

        @Override
        public TradeService getForExchange(String exchange) {
            return services.getUnchecked(exchange);
        }
    }
}
