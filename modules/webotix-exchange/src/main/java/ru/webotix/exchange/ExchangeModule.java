package ru.webotix.exchange;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;

public class ExchangeModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder.newSetBinder(binder(), WebResource.class)
                .addBinding()
                .to(ExchangeResource.class);
    }
}
