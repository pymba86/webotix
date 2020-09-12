package ru.webotix.telegram;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.lifecycle.Managed;

public class TelegramModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder.newSetBinder(binder(), Managed.class)
                .addBinding()
                .to(TelegramNotificationsTask.class);
    }
}
