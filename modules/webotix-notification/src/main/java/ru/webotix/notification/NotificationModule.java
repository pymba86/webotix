package ru.webotix.notification;

import com.google.inject.AbstractModule;
import ru.webotix.job.spi.StatusUpdateService;

public class NotificationModule extends AbstractModule {

    private final SubmissionType submissionType;

    public NotificationModule(SubmissionType submissionType) {
        this.submissionType = submissionType;
    }

    @Override
    protected void configure() {
        if (submissionType == SubmissionType.ASYNC) {
            bind(NotificationService.class).to(AsynchronousNotificationService.class);
        } else {
            bind(NotificationService.class).to(SynchronousNotificationService.class);
        }

        bind(StatusUpdateService.class).to(SynchronousEventStatusUpdateService.class);
    }

    public enum SubmissionType {
        ASYNC,
        SYNC
    }
}
