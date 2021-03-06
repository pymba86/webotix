package ru.webotix.job.api;

import ru.webotix.job.status.api.Status;

/**
 * Передается в экземпляр процесса при запуске,
 * для асинхронного обновления или завершение задания
 */
public interface JobControl {

    /**
     * Обновить задание
     *
     * @param job Задание
     */
    public void replace(Job job);

    /**
     * Завершить обработку задания, чтобы гарантировать, что оно отключено и удалено.
     * Будет вызвана очистка ресурсов задания
     *
     * @param status Состояние завершения задания
     */
    public void finish(Status status);
}
