package ru.webotix.notification;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.webotix.notification.api.Notification;
import ru.webotix.notification.api.NotificationService;

/**
 * Сервис уведомлений, который отправляет все входящие уведомления в EventBus
 */
@Singleton
public class SyncNotificationService implements NotificationService {

    private final EventBus eventBus;

    @Inject
    public SyncNotificationService(EventBus eventBus) {
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
