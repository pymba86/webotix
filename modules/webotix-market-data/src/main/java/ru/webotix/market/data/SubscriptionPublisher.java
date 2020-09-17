package ru.webotix.market.data;

import com.google.inject.Singleton;
import io.reactivex.Flowable;
import ru.webotix.market.data.api.BalanceEvent;
import ru.webotix.market.data.api.MarketDataSubscription;
import ru.webotix.market.data.api.MarketDataSubscriptionManager;
import ru.webotix.market.data.api.SubscriptionController;

import java.util.Set;

@Singleton
public class SubscriptionPublisher implements MarketDataSubscriptionManager {

    private SubscriptionController controller;

    private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;

    public SubscriptionPublisher() {

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

    void clearCacheForSubscription(MarketDataSubscription subscription) {

        balanceOut.removeFromCache(
                subscription.spec().exchange() + "/" + subscription.spec().base());

        balanceOut.removeFromCache(
                subscription.spec().exchange() + "/" + subscription.spec().counter());
    }
}
