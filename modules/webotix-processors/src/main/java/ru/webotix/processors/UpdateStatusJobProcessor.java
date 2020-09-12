package ru.webotix.processors;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import ru.webotix.job.api.JobControl;
import ru.webotix.job.status.api.JobStatusService;
import ru.webotix.job.status.api.Status;

public class UpdateStatusJobProcessor implements UpdateStatusJob.Processor {

    private final JobStatusService jobStatusService;

    private final UpdateStatusJob job;

    @AssistedInject
    public UpdateStatusJobProcessor(
            @Assisted UpdateStatusJob job,
            @Assisted JobControl jobControl,
            JobStatusService jobStatusService
    ) {
        this.job = job;
        this.jobStatusService = jobStatusService;
    }

    @Override
    public Status start() {
        jobStatusService.send(job.statusUpdate());
        return Status.SUCCESS;
    }

    @Override
    public void stop() {
        // Задание не возможно остановить
    }

    @Override
    public void setReplacedJob(UpdateStatusJob job) {
        // Задание не возможно удалить
    }

    public static final class Module extends AbstractModule {

        @Override
        protected void configure() {
            install(
                    new FactoryModuleBuilder()
                            .implement(UpdateStatusJob.Processor.class, UpdateStatusJobProcessor.class)
                            .build(UpdateStatusJob.Processor.ProcessorFactory.class)
            );
        }
    }
}
