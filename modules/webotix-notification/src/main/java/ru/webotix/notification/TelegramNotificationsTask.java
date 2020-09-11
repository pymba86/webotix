package ru.webotix.notification;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Set;

@Singleton
public class TelegramNotificationsTask implements Managed {

    private static final Set<NotificationLevel> SEND_FOR
            = ImmutableSet.of(NotificationLevel.Error, NotificationLevel.Alert);

    private final TelegramConfiguration configuration;
    private final Provider<TelegramService> telegramService;
    private final EventBus eventBus;

    @Inject
    public TelegramNotificationsTask(
            @Nullable TelegramConfiguration configuration,
            Provider<TelegramService> telegramService,
            EventBus eventBus) {
        this.configuration = configuration;
        this.telegramService = telegramService;
        this.eventBus = eventBus;
    }

    @Override
    public void start() {
        if (isEnabled()) {
            eventBus.register(this);
        }
    }

    @Override
    public void stop() {
        if (isEnabled()) {
            eventBus.unregister(this);
        }
    }

    @Subscribe
    void notify(Notification notification) {
        if (SEND_FOR.contains(notification.level())) {
            telegramService.get()
                    .sendMessage(notification.message());
        }
    }

    private boolean isEnabled() {
        return configuration != null && StringUtils
                .isNotBlank(configuration.getBotToken());
    }
}
