package ru.webotix.market.data.api;

import io.reactivex.Flowable;
import org.knowm.xchange.dto.Order;

public interface MarketDataSubscriptionManager extends SubscriptionController {

    /**
     * Получает поток подписанных тикеров, начиная с любых кешированных тикеров.
     */
    Flowable<TickerEvent> getTickers();

    /**
     * Получает поток с обновлениями баланса.
     */
    Flowable<BalanceEvent> getBalances();

    /**
     * Получает поток подписанных списков открытых заказов.
     */
    Flowable<OpenOrdersEvent> getOrderSnapshots();

    /**
     * Получает поток, содержащий обновления книги заказов.
     */
    Flowable<OrderBookEvent> getOrderBookSnapshots();

    /**
     * Получает поток сделок.
     */
    Flowable<TradeEvent> getTrades();

    /**
     * Получает поток пользовательских сделок.
     */
    Flowable<UserTradeEvent> getUserTrades();

    /**
     * Получает поток с отчетами о выполнении binance.
     */
    Flowable<OrderChangeEvent> getOrderChanges();

    /**
     * Позвоните сразу после отправки заказа, чтобы в мероприятии появилась полная информация о заказе.
     */
    void postOrder(TickerSpec spec, Order order);
}
