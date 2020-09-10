package ru.webotix.notification;

import com.google.inject.ImplementedBy;

/**
 * Переходный класс сервиса уведомлений, который хранит ссылку на обьект сервиса
 */
@ImplementedBy(SynchronousNotificationService.class)
public interface TransientNotificationService extends NotificationService {
}
