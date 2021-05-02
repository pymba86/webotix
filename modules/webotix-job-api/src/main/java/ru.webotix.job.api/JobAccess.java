package ru.webotix.job.api;

import ru.webotix.job.api.Job;

public interface JobAccess {

    /**
     * Добавить задание
     *
     * @param job Задание
     * @throws JobAlreadyExistsException Наличие задания в базе
     */
    void insert(Job job) throws JobAlreadyExistsException;

    /**
     * Обновить задание
     *
     * @param job Задание
     */
    void update(Job job);

    /**
     * Загрузить задание
     *
     * @param id Идентификатор задания
     * @return Задание
     */
    Job load(String id);

    /**
     * Список заданий
     *
     * @return Список заданий
     */
    Iterable<Job> list();

    /**
     * Удалить задание
     *
     * @param jobId Индентификатор задания
     */
    void delete(String jobId);

    /**
     * Удалить все задания
     */
    void deleteAll();

    /**
     * Исключение связанное с наличием задания в базе
     */
    public static final class JobAlreadyExistsException extends Exception {

        public JobAlreadyExistsException() {
            super();
        }

        public JobAlreadyExistsException(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Исключение связанное с отсутствием задания в базе
     */
    public static final class JobDoesNotExistException extends RuntimeException {

        public JobDoesNotExistException() {
            super();
        }
    }
}

