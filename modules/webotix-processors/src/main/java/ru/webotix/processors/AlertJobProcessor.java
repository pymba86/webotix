package ru.webotix.processors;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import ru.webotix.job.api.JobControl;
import ru.webotix.job.status.api.Status;
import ru.webotix.notification.api.NotificationService;

public class AlertJobProcessor implements AlertJob.Processor {

    private final NotificationService notificationService;
    private final AlertJob job;

    @AssistedInject
    public AlertJobProcessor(
            @Assisted AlertJob job,
            @Assisted JobControl jobControl,
            NotificationService notificationService) {
        this.job = job;
        this.notificationService = notificationService;
    }

    @Override
    public Status start() {

        notificationService.send(job.notification());

        return Status.SUCCESS;
    }

    @Override
    public void stop() {
        // Остановка данного задания не возможна
    }

    @Override
    public void setReplacedJob(AlertJob job) {
        // Удаление данного задания не возможно
    }

    public static final class Module extends AbstractModule {

        @Override
        protected void configure() {
            install(
                    new FactoryModuleBuilder()
                            .implement(AlertJob.Processor.class, AlertJobProcessor.class)
                            .build(AlertJob.Processor.ProcessorFactory.class)
            );
        }
    }
}
