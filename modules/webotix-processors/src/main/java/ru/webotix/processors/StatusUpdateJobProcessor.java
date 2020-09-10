package ru.webotix.processors;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import ru.webotix.job.spi.JobControl;
import ru.webotix.job.spi.Status;
import ru.webotix.notification.SynchronousStatusUpdateService;

public class StatusUpdateJobProcessor implements StatusUpdateJob.Processor {

    private final SynchronousStatusUpdateService statusUpdateService;

    private final StatusUpdateJob job;

    @AssistedInject
    public StatusUpdateJobProcessor(
            @Assisted StatusUpdateJob job,
            @Assisted JobControl jobControl,
            SynchronousStatusUpdateService statusUpdateService
    ) {
        this.job = job;
        this.statusUpdateService = statusUpdateService;
    }

    @Override
    public Status start() {
        statusUpdateService.send(job.statusUpdate());
        return Status.SUCCESS;
    }

    @Override
    public void stop() {
        // Задание не возможно остановить
    }

    @Override
    public void setReplacedJob(StatusUpdateJob job) {
        // Задание не возможно удалить
    }

    public static final class Module extends AbstractModule {

        @Override
        protected void configure() {
            install(
                    new FactoryModuleBuilder()
                            .implement(StatusUpdateJob.Processor.class, StatusUpdateJobProcessor.class)
                            .build(StatusUpdateJob.Processor.ProcessorFactory.class)
            );
        }
    }
}
