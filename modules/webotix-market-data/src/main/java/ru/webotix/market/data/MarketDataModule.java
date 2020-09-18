package ru.webotix.market.data;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import ru.webotix.market.data.api.MarketDataSubscriptionManager;
import ru.webotix.market.data.api.SubscriptionController;

public class MarketDataModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(MarketDataSubscriptionManager.class)
                .to(SubscriptionPublisher.class);

        bind(SubscriptionController.class)
                .to(MarketDataSubscriptionController.class);

        Multibinder.newSetBinder(binder(), Service.class)
                .addBinding()
                .to(MarketDataSubscriptionController.class);
    }
}
