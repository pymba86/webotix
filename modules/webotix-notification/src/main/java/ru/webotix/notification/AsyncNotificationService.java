package ru.webotix.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.job.api.JobSubmitter;
import ru.webotix.notification.api.Notification;
import ru.webotix.notification.api.NotificationService;
import ru.webotix.processors.AlertJob;

@Singleton
public class AsyncNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncNotificationService.class);

    private final JobSubmitter jobSubmitter;
    private final Transactionally transactionally;

    @Inject
    AsyncNotificationService(JobSubmitter jobSubmitter, Transactionally transactionally)
    {
        this.jobSubmitter = jobSubmitter;
        this.transactionally = transactionally;
    }

    @Override
    public void send(Notification notification) {
        transactionally
                .allowingNested()
                .run(
                        () -> jobSubmitter.submitNewUnchecked(
                                AlertJob.builder().notification(notification).build()
                        )
                );
    }

    @Override
    public void error(String message, Throwable cause) {
        log.error("Error notification: " + message, cause);
        error(message);
    }
}
