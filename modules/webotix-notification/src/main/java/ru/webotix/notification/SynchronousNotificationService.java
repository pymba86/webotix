package ru.webotix.notification;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Сервис уведомлений, который отправляет все входящие уведомления в EventBus
 */
@Singleton
public class SynchronousNotificationService implements TransientNotificationService {

    private final EventBus eventBus;

    @Inject
    public SynchronousNotificationService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void send(Notification notification) {
        eventBus.post(notification);
    }

    @Override
    public void error(String message, Throwable cause) {
        error(message);
    }
}
