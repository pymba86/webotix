package ru.webotix.market.data;

import com.google.common.collect.Ordering;
import com.google.inject.Singleton;
import io.reactivex.Flowable;
import org.knowm.xchange.dto.Order;
import ru.webotix.market.data.api.*;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Singleton
public class SubscriptionPublisher implements MarketDataSubscriptionManager {

    private SubscriptionController controller;

    private final CachingPersistentPublisher<TickerEvent, TickerSpec> tickersOut;
    private final CachingPersistentPublisher<OpenOrdersEvent, TickerSpec> openOrdersOut;
    private final CachingPersistentPublisher<OrderBookEvent, TickerSpec> orderbookOut;
    private final PersistentPublisher<TradeEvent> tradesOut;
    private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;
    private final PersistentPublisher<OrderChangeEvent> orderStatusChangeOut;
    private final CachingPersistentPublisher<UserTradeEvent, String> userTradesOut;

    public SubscriptionPublisher() {

        this.tickersOut = new CachingPersistentPublisher<>(TickerEvent::spec);
        this.openOrdersOut = new CachingPersistentPublisher<>(OpenOrdersEvent::spec);
        this.orderbookOut = new CachingPersistentPublisher<>(OrderBookEvent::spec);
        this.tradesOut = new PersistentPublisher<>();

        this.userTradesOut =
                new CachingPersistentPublisher<>((UserTradeEvent e) -> e.trade().getId())
                        .orderInitialSnapshotBy(
                                iterable ->
                                        Ordering.natural()
                                                .onResultOf((UserTradeEvent e) -> Objects
                                                        .requireNonNull(e).trade().getTimestamp())
                                                .sortedCopy(iterable));

        this.balanceOut =
                new CachingPersistentPublisher<>(
                        (BalanceEvent e) -> e.exchange() + "/" + e.balance().getCurrency());

        this.orderStatusChangeOut = new PersistentPublisher<>();
    }

    void setController(SubscriptionController controller) {
        this.controller = controller;
    }

    @Override
    public Flowable<BalanceEvent> getBalances() {
        return balanceOut.getAll();
    }

    @Override
    public Flowable<TickerEvent> getTickers() {
        return tickersOut.getAll();
    }

    @Override
    public Flowable<OpenOrdersEvent> getOrderSnapshots() {
        return openOrdersOut.getAll();
    }

    @Override
    public Flowable<OrderBookEvent> getOrderBookSnapshots() {
        return orderbookOut.getAll();
    }

    @Override
    public Flowable<TradeEvent> getTrades() {
        return tradesOut.getAll();
    }

    @Override
    public Flowable<UserTradeEvent> getUserTrades() {
        return userTradesOut.getAll();
    }

    @Override
    public Flowable<OrderChangeEvent> getOrderChanges() {
        return orderStatusChangeOut.getAll();
    }

    @Override
    public void postOrder(TickerSpec spec, Order order) {
        orderStatusChangeOut.emit(OrderChangeEvent.create(spec, order, new Date()));
    }
    @Override
    public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
        if (this.controller == null) {
            throw new IllegalStateException(
                    SubscriptionPublisher.class.getSimpleName() + " not initialised");
        }
        this.controller.updateSubscriptions(subscriptions);
    }

    void emit(TickerEvent e) {
        tickersOut.emit(e);
    }

    void emit(OpenOrdersEvent e) {
        openOrdersOut.emit(e);
    }

    void emit(OrderBookEvent e) {
        orderbookOut.emit(e);
    }

    void emit(TradeEvent e) {
        tradesOut.emit(e);
    }

    void emit(UserTradeEvent e) {
        userTradesOut.emit(e);
    }

    void emit(BalanceEvent e) {
        balanceOut.emit(e);
    }

    void emit(OrderChangeEvent e) {
        orderStatusChangeOut.emit(e);
    }

    void clearCacheForSubscription(MarketDataSubscription subscription) {

        tickersOut.removeFromCache(subscription.spec());
        orderbookOut.removeFromCache(subscription.spec());
        openOrdersOut.removeFromCache(subscription.spec());
        userTradesOut.removeFromCache(t -> t.spec().equals(subscription.spec()));

        balanceOut.removeFromCache(
                subscription.spec().exchange() + "/" + subscription.spec().base());

        balanceOut.removeFromCache(
                subscription.spec().exchange() + "/" + subscription.spec().counter());
    }
}
