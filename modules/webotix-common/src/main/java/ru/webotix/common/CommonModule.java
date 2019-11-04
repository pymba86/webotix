package ru.webotix.common;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import ru.webotix.datasource.database.DatabaseModule;

public class CommonModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), EnvironmentInitialiser.class)
                .addBinding()
                .toInstance(environment -> environment.jersey()
                        .register(new JerseyMappingErrorLoggingExceptionHandler()));
        install(new DatabaseModule());
    }
}
