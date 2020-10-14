package ru.webotix.exchange.api;

import com.google.common.collect.ImmutableSet;
import io.reactivex.Flowable;
import ru.webotix.market.data.api.*;

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
         * Получить поток событий изменений тикетов на бирже
         * @return Подписка на события изменений тикетов на бирже
         */
        Flowable<TickerEvent> getTickers();

        /**
         * Получить поток событий баланса слушающей биржы
         * @return Подписка на события относящиеся к балансу
         */
        Flowable<BalanceEvent> getBalances();

        Iterable<Flowable<TickerEvent>> getTickersSplit();

        Flowable<OrderBookEvent> getOrderBooks();

        Flowable<TradeEvent> getTrades();

        Flowable<OpenOrdersEvent> getOrderSnapshots();

        Flowable<OrderChangeEvent> getOrderChanges();

        Flowable<UserTradeEvent> getUserTrades();

        public ExchangeEventSubscription replace(Set<MarketDataSubscription> targetSubscriptions);

        public default ExchangeEventSubscription replace(MarketDataSubscription... targetSubscriptions) {
            return replace(ImmutableSet.copyOf(targetSubscriptions));
        }

        @Override
        void close();
    }
}
