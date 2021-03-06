package ru.webotix.job.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CUSTOM,
        property = "jobType"
)
@JsonTypeIdResolver(JobTypeResolver.class)
public interface Job {

    /**
     * Ид задачи в приложении
     *
     * @return Ид задачи
     */
    String id();

    /**
     * Создать дубликат задания с возможность создать измененную версию
     *
     * @return Построитель заданий
     */
    JobBuilder<? extends Job> toBuilder();

    /**
     * Класс фабрики, из которого могут быть созданы экземпляры класса процессора
     * для обработки задач
     *
     * @return Класс фабрики процессора обработки задач
     */
    Class<? extends JobProcessor.Factory<? extends Job>> processorFactory();
}
