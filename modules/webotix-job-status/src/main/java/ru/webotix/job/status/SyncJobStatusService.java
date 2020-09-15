package ru.webotix.job.status;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.webotix.job.status.api.JobStatus;
import ru.webotix.job.status.api.JobStatusService;

@Singleton
public class SyncJobStatusService implements JobStatusService {

    private final EventBus eventBus;

    @Inject
    public SyncJobStatusService(EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public void send(JobStatus statusUpdate) {
        eventBus.post(statusUpdate);
    }

}
