package ru.webotix.job;

import java.util.UUID;

/**
 * Позволяет получать и выпускать эксклюзивный доступ к заданиям
 */
public interface JobLocker {

    /**
     * Заблокировать задание
     *
     * @param jobId Идентификатор задания
     * @param uuid  Идентификатор клиента
     * @return Задание было успешно заблокировано
     */
    boolean attemptLock(String jobId, UUID uuid);

    /**
     * Обновить ранее полученную блокировку, сбросив таймаут
     *
     * @param jobId Идентификатор задания
     * @param uuid Идентификатор клиента
     * @return Блокировка задания успешна обновлена
     */
    boolean updateLock(String jobId, UUID uuid);

    /**
     * Снимает ранее полученную блокировку
     *
     * @param jobId Идентификатор задания
     * @param uuid Идентификатор клиента
     */
    void releaseLock(String jobId, UUID uuid);

    /**
     * Снимает все блокировки с указанной работы
     *
     * @param jobId Идентификатор задания
     */
    void releaseAnyLock(String jobId);

    /**
     * Снять все блокировки со всех заданий
     */
    void releaseAllLocks();
}
