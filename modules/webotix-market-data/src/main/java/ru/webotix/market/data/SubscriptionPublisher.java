package ru.webotix.market.data;

import com.google.inject.Singleton;
import io.reactivex.Flowable;
import ru.webotix.market.data.api.*;

import java.util.Set;

@Singleton
public class SubscriptionPublisher implements MarketDataSubscriptionManager {

    private SubscriptionController controller;

    private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;
    private final CachingPersistentPublisher<TickerEvent, TickerSpec> tickerOut;

    public SubscriptionPublisher() {

        this.tickerOut = new CachingPersistentPublisher<>(TickerEvent::spec);

        this.balanceOut =
                new CachingPersistentPublisher<>(
                        (BalanceEvent e) -> e.exchange() + "/" + e.balance().getCurrency());

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
        return tickerOut.getAll();
    }

    @Override
    public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
        if (this.controller == null) {
            throw new IllegalStateException(
                    SubscriptionPublisher.class.getSimpleName() + " not initialised");
        }
        this.controller.updateSubscriptions(subscriptions);
    }

    void emit(BalanceEvent e) {
        balanceOut.emit(e);
    }

    void emit(TickerEvent e) {
        tickerOut.emit(e);
    }

    void clearCacheForSubscription(MarketDataSubscription subscription) {

        balanceOut.removeFromCache(
                subscription.spec().exchange() + "/" + subscription.spec().base());

        balanceOut.removeFromCache(
                subscription.spec().exchange() + "/" + subscription.spec().counter());
    }
}
