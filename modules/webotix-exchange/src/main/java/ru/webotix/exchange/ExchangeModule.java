package ru.webotix.exchange;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.exchange.api.ExchangeService;

public class ExchangeModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(ExchangeService.class)
                .to(CacheExchangeService.class);

        // Фабрика аккаунтом
        bind(AccountServiceFactory.class)
                .to(AccountServiceExchangeFactory.class);

        //  Поставщик событий биржы
        bind(ExchangeEventRegistry.class)
                .to(ExchangeEventBus.class);

        // Фабрика торговли
        bind(TradeServiceFactory.class)
                .to(TradeServiceExchangeFactory.class);

        // Веб доступ к бирже
        Multibinder.newSetBinder(binder(), WebResource.class)
                .addBinding()
                .to(ExchangeResource.class);

        // Проверка доступности к биржам
        Multibinder.newSetBinder(binder(), HealthCheck.class)
                .addBinding()
                .to(ExchangeAccessHealthCheck.class);
    }
}
