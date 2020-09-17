package ru.webotix.market.data;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.knowm.xchange.simulated.AccountFactory;
import org.knowm.xchange.simulated.MatchingEngineFactory;
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

    @Provides
    @Singleton
    AccountFactory accountFactory() {
        return new AccountFactory();
    }

    @Provides
    @Singleton
    MatchingEngineFactory matchingEngineFactory(AccountFactory accountFactory) {
        return new MatchingEngineFactory(accountFactory);
    }
}
