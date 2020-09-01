package ru.webotix.job.spi;

/**
 * Построитель задания
 *
 * @param <T>
 */
public interface JobBuilder<T extends Job> {

    /**
     * Установить идентификатор задания
     *
     * @param id Идентификатор задания
     * @return Себя для связывания методов
     */
    public JobBuilder<T> id(String id);

    /**
     * Построить задание
     *
     * @return Задание
     */
    public T build();
}
