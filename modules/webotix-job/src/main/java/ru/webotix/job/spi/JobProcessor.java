package ru.webotix.job.spi;

import com.google.inject.Injector;

public interface JobProcessor<T extends Job> {

    public Status start();

    public void stop();

    public void setReplacedJob(T job);

    // Фабрика для построение экземпляров процессов для обработки задания
    public interface Factory<T extends Job> {

        /**
         * Создать новый экземпляр процесса
         *
         * @param job задани
         * @param jobControl Управление заданием в процессе обработки
         * @return Экземпляр процесса
         */
        public JobProcessor<T> create(T job, JobControl jobControl);
    }

    @SuppressWarnings("unchecked")
    public static JobProcessor<Job> createProcessor(
            Job job, JobControl jobControl, Injector injector
    ) {
        return ((JobProcessor.Factory<Job>) injector.getInstance(job.processorFactory()))
                .create(job, jobControl);
    }
}
