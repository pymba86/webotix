package ru.webotix.job;

import ru.webotix.job.spi.Job;
import ru.webotix.job.spi.JobControl;

public interface JobSubmitter {

    /**
     * Отправить задание
     *
     * @param job Задание
     * @return Задание с присвоенным id
     * @throws Exception Исключение
     */
    Job submitNew(Job job) throws Exception;

    /**
     * Проверить задание
     *
     * @param job        Задание
     * @param jobControl Управление заданием
     */
    void validate(Job job, JobControl jobControl);
}
