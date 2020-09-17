package ru.webotix.exchange.api;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import ru.webotix.market.data.api.BalanceEvent;
import ru.webotix.market.data.api.MarketDataSubscription;

import java.util.Set;

/**
 * Реестр событий биржы
 */
public interface ExchangeEventRegistry {

    /**
     * Подписать новый список слушателей
     *
     * @param targetSubscriptions Список слушателей
     * @return Подписчик на события биржы
     */
    public ExchangeEventSubscription subscribe(
            Set<MarketDataSubscription> targetSubscriptions);

    /**
     * Подписать новый список слушателей
     *
     * @param targetSubscriptions Список слушателей
     * @return Подписчик на события биржы
     */
    public default ExchangeEventSubscription subscribe(MarketDataSubscription... targetSubscriptions) {
        return subscribe(ImmutableSet.copyOf(targetSubscriptions));
    }

    /**
     * Подписчик на события биржы
     */
    public interface ExchangeEventSubscription extends AutoCloseable {

        /**
         * Получить поток событий баланса слушающей биржы
         * @return Подписка на события относящиеся к балансу
         */
        Flowable<BalanceEvent> getBalances();

        public ExchangeEventSubscription replace(Set<MarketDataSubscription> targetSubscriptions);

        public default ExchangeEventSubscription replace(MarketDataSubscription... targetSubscriptions) {
            return replace(ImmutableSet.copyOf(targetSubscriptions));
        }

        @Override
        void close();
    }
}
