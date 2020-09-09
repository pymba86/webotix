package ru.webotix.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.job.JobSubmitter;
import ru.webotix.job.spi.StatusUpdate;
import ru.webotix.job.spi.StatusUpdateService;

@Singleton
public class AsynchronousNotificationService implements NotificationService, StatusUpdateService {

    private static final Logger log = LoggerFactory.getLogger(AsynchronousNotificationService.class);

    private final JobSubmitter jobSubmitter;
    private final Transactionally transactionally;

    @Inject
    AsynchronousNotificationService(JobSubmitter jobSubmitter, Transactionally transactionally)
    {
        this.jobSubmitter = jobSubmitter;
        this.transactionally = transactionally;
    }

    @Override
    public void send(StatusUpdate statusUpdate) {
        /*transactionally
                .allowingNested()
                .run(() -> jobSubmitter.submitNewUnchecked(

                ));*/
    }

    @Override
    public void send(Notification notification) {

    }

    @Override
    public void error(String message, Throwable cause) {

    }
}
