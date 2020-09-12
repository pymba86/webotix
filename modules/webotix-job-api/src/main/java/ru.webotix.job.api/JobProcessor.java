package ru.webotix.job.api;

import com.google.inject.Injector;
import ru.webotix.job.status.api.Status;

public interface JobProcessor<T extends Job> {

    Status start();

    void stop();

    void setReplacedJob(T job);

    // Фабрика для построение экземпляров процессов для обработки задания
    interface Factory<T extends Job> {

        /**
         * Создать новый экземпляр процесса
         *
         * @param job        задани
         * @param jobControl Управление заданием в процессе обработки
         * @return Экземпляр процесса
         */
        JobProcessor<T> create(T job, JobControl jobControl);
    }

    @SuppressWarnings("unchecked")
    static JobProcessor<Job> createProcessor(
            Job job, JobControl jobControl, Injector injector
    ) {
        return ((JobProcessor.Factory<Job>) injector.getInstance(job.processorFactory()))
                .create(job, jobControl);
    }
}
