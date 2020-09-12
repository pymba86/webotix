package ru.webotix.notification.api;

/**
 * Сервис уведомления о важном событии в системе
 */
public interface NotificationService {

    /**
     * Отправить уведомление
     *
     * @param notification Уведомление.
     */
    void send(Notification notification);

    /**
     * Оправить исключение в асинхронном режиме
     *
     * @param message Текст исключения
     * @param cause   Исключение
     */
    void error(String message, Throwable cause);

    /**
     * Оправить уведомление в асинхронном режиме
     *
     * @param message Текст уведомления
     */
    default void info(String message) {
        send(Notification.create(message, NotificationLevel.Info));
    }

    /**
     * Оправить предупреждение в асинхронном режиме
     *
     * @param message Текст предупреждения
     */
    default void alert(String message) {
        send(Notification.create(message, NotificationLevel.Alert));
    }

    /**
     * Оправить ошибку в асинхронном режиме
     *
     * @param message Текст ошибки
     */
    default void error(String message) {
        send(Notification.create(message, NotificationLevel.Error));
    }
}
