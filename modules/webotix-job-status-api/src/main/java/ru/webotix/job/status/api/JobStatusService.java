package ru.webotix.job.status.api;

/**
 * Сервис для оповещения пользователя о важном событии
 */
public interface JobStatusService {

    /**
     * Отправить обновление статуса задания
     *
     * @param statusUpdate Обновление статуса
     */
    void send(JobStatus statusUpdate);

    /**
     * Отправляет сообщение, показывающее состояние асинхронного запроса
     *
     * @param requestId Идентификатор асинхронного запроса
     * @param status Текущий статус запроса
     */
    default void status(String requestId, Status status) {
        send(JobStatus.create(requestId, status, null));
    }

    /**
     * Отправляет сообщение, показывающее состояние асинхронного запроса
     *
     * @param requestId Идентификатор асинхронного запроса
     * @param status Текущий статус запроса
     * @param payload Объект
     */
    default void status(String requestId, Status status, Object payload) {
        send(JobStatus.create(requestId, status, payload));
    }
}
