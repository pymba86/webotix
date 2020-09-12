package ru.webotix.notification;

import com.google.inject.AbstractModule;
import ru.webotix.base.SubmissionType;
import ru.webotix.notification.api.NotificationService;

public class NotificationModule extends AbstractModule {

    private final SubmissionType submissionType;

    public NotificationModule(SubmissionType submissionType) {
        this.submissionType = submissionType;
    }

    @Override
    protected void configure() {

        if (submissionType == SubmissionType.ASYNC) {
            bind(NotificationService.class)
                    .to(AsyncNotificationService.class);
        } else {
            bind(NotificationService.class)
                    .to(SyncNotificationService.class);
        }
    }


}
