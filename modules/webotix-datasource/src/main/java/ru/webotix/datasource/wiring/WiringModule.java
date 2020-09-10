package ru.webotix.datasource.wiring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;

import java.util.concurrent.ExecutorService;

/**
 * Модуль схемы подключения
 */
public class WiringModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), Managed.class)
                .addBinding()
                .to(ExecutorServiceManager.class);
    }

    @Provides
    @Singleton
    EventBus eventBus() {
        return new EventBus();
    }

    @Provides
    ExecutorService executor(ExecutorServiceManager managedExecutor) {
        return managedExecutor.executor();
    }

    @Provides
    ObjectMapper objectMapper(Environment environment) {
        return environment.getObjectMapper();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WiringModule;
    }

    @Override
    public int hashCode() {
        return WiringModule.class.getName().hashCode();
    }
}
