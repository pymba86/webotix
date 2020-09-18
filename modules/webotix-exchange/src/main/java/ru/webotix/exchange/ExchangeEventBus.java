package ru.webotix.exchange;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.market.data.api.*;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Singleton
public class ExchangeEventBus implements ExchangeEventRegistry {

    private static final Logger log = LoggerFactory.getLogger(ExchangeEventBus.class);

    private final ConcurrentMap<MarketDataSubscription, AtomicInteger>
            allSubscriptions = Maps.newConcurrentMap();

    private final MarketDataSubscriptionManager marketDataSubscriptionManager;

    @Inject
    public ExchangeEventBus(MarketDataSubscriptionManager marketDataSubscriptionManager) {
        this.marketDataSubscriptionManager = marketDataSubscriptionManager;
    }

    @Override
    public ExchangeEventSubscription subscribe(
            Set<MarketDataSubscription> targetSubscriptions) {

        ExchangeEventSubscription subscription = new ExchangeEventSubscription(targetSubscriptions);

        log.debug("Created subscriber {} with subscriptions {}", subscription.name, targetSubscriptions);

        return subscription;
    }

    private final class ExchangeEventSubscription
            implements ExchangeEventRegistry.ExchangeEventSubscription {

        private final Set<MarketDataSubscription> subscriptions;
        private final String name;

        public ExchangeEventSubscription(Set<MarketDataSubscription> subscriptions) {
            this(subscriptions, UUID.randomUUID().toString());
        }

        public ExchangeEventSubscription(Set<MarketDataSubscription> subscriptions, String name) {
            this.subscriptions = subscriptions;
            this.name = name;

            if (subscribeAll()) {
                updateSubscriptions();
            }
        }

        @Override
        public Flowable<TickerEvent> getTickers() {

            Set<TickerSpec> filtered = subscriptionsFor(MarketDataType.Ticker);

            return marketDataSubscriptionManager.getTickers()
                    .filter(e -> filtered.contains(e.spec()))
                    .onBackpressureLatest();
        }

        @Override
        public Flowable<BalanceEvent> getBalances() {

            Set<String> exchangeCurrenciesSubscribed =
                    subscriptionsFor(MarketDataType.Balance)
                            .stream()
                            .flatMap(spec ->
                                    ImmutableSet.of(
                                            spec.exchange() + "/" + spec.base(),
                                            spec.exchange() + "/" + spec.counter()
                                    ).stream()
                            )
                            .collect(Collectors.toSet());

            return marketDataSubscriptionManager.getBalances()
                    .filter(e -> exchangeCurrenciesSubscribed.contains(
                            e.exchange() + "/" + e.balance().getCurrency()
                    ))
                    .onBackpressureLatest();
        }

        /**
         * Подписать нового слушателя к бирже
         *
         * @param subscription слушатель
         * @return слушатель добавлен в общий список
         */
        private boolean subscribe(MarketDataSubscription subscription) {
            log.debug("... subscribing {}", subscription);
            boolean newGlobally = allSubscriptions.computeIfAbsent(
                    subscription, s -> new AtomicInteger(0)).incrementAndGet() == 1;
            if (newGlobally) {
                log.debug("   ... new global subscription");
            }
            return newGlobally;
        }

        /**
         * Отписать слушателя от биржи
         *
         * @param subscription слушатель
         * @return слушатель удален из общего списка
         */
        private boolean unsubscribe(MarketDataSubscription subscription) {
            log.debug("... unsubscribing {}", subscription);
            AtomicInteger refCount = allSubscriptions.get(subscription);

            if (refCount == null) {
                log.warn(" ... RefCount is unset for live subscription: {}", subscription);
                return true;
            }

            int newRefCount = refCount.decrementAndGet();
            log.debug(" ... RefCount set to {}", newRefCount);

            if (newRefCount == 0) {
                log.debug(" ... removing global subscription");
                allSubscriptions.remove(subscription);
                return true;
            }
            {
                log.debug(" ... other subscribers still holding it open");
                return false;
            }
        }

        /**
         * Отписать всех слушателей от биржы
         *
         * @return один из слушателей отписан
         */
        private boolean unsubscribeAll() {
            boolean updated = false;
            for (MarketDataSubscription subscription : subscriptions) {
                if (unsubscribe(subscription)) {
                    updated = true;
                }
            }
            return updated;
        }

        /**
         * Подписать всех слушателей к бирже
         *
         * @return один из слушателей подписан
         */
        private boolean subscribeAll() {
            boolean updated = false;
            for (MarketDataSubscription subscription : subscriptions) {
                if (subscribe(subscription)) {
                    updated = true;
                }
            }
            return updated;
        }

        /**
         * Обновить список слушателей
         */
        private void updateSubscriptions() {
            marketDataSubscriptionManager.updateSubscriptions(
                    allSubscriptions.keySet()
            );
        }

        /**
         * Получить список тикеров отфильтрованных по типу данных
         *
         * @param marketDataType тип данных
         * @return Список тикеров отфильтрованных по типу данных
         */
        private Set<TickerSpec> subscriptionsFor(MarketDataType marketDataType) {
            return subscriptions.stream()
                    .filter(s -> s.type().equals(marketDataType))
                    .map(MarketDataSubscription::spec)
                    .collect(Collectors.toSet());
        }

        @Override
        public ExchangeEventRegistry.ExchangeEventSubscription replace(
                Set<MarketDataSubscription> targetSubscriptions) {
            if (targetSubscriptions.equals(subscriptions)) {
                return this;
            }
            if (unsubscribeAll()) {
                updateSubscriptions();
            }
            return new ExchangeEventSubscription(targetSubscriptions, name);
        }

        @Override
        public void close() {
            if (unsubscribeAll()) {
                updateSubscriptions();
            }
        }
    }
}
