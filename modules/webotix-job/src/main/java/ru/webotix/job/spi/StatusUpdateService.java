package ru.webotix.job.spi;

/**
 * Сервис для оповещения пользователя о важном событии
 */
public interface StatusUpdateService {

    /**
     * Отправить обновление статуса задания
     *
     * @param statusUpdate Обновление статуса
     */
    void send(StatusUpdate statusUpdate);

    /**
     * Отправляет сообщение, показывающее состояние асинхронного запроса
     *
     * @param requestId Идентификатор асинхронного запроса
     * @param status Текущий статус запроса
     */
    default void status(String requestId, Status status) {
        send(StatusUpdate.create(requestId, status, null));
    }

    /**
     * Отправляет сообщение, показывающее состояние асинхронного запроса
     *
     * @param requestId Идентификатор асинхронного запроса
     * @param status Текущий статус запроса
     * @param payload Объект
     */
    default void status(String requestId, Status status, Object payload) {
        send(StatusUpdate.create(requestId, status, payload));
    }
}
