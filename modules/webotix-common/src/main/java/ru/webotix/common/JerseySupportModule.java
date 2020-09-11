package ru.webotix.common;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.client.JerseyClientBuilder;

import javax.ws.rs.client.Client;

public class JerseySupportModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
                .addBinding()
                .toInstance(environment -> environment.jersey()
                        .register(new JerseyErrorLoggingException()));
    }

    @Provides
    @Singleton
    Client jerseyClient(Environment environment, JerseyClientConfiguration configuration) {
        return new JerseyClientBuilder(environment)
                .using(configuration == null ? new JerseyClientConfiguration()
                        : configuration)
                .build("client");
    }
}
