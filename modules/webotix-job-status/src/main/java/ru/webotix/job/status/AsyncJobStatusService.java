package ru.webotix.job.status;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.job.JobSubmitter;
import ru.webotix.job.status.api.JobStatus;
import ru.webotix.job.status.api.JobStatusService;
import ru.webotix.processors.UpdateStatusJob;

@Singleton
public class AsyncJobStatusService implements JobStatusService {

    private final JobSubmitter jobSubmitter;
    private final Transactionally transactionally;

    @Inject
    AsyncJobStatusService(JobSubmitter jobSubmitter, Transactionally transactionally) {
        this.jobSubmitter = jobSubmitter;
        this.transactionally = transactionally;
    }

    @Override
    public void send(JobStatus statusUpdate) {
        transactionally
                .allowingNested()
                .run(
                        () -> jobSubmitter.submitNewUnchecked(
                                UpdateStatusJob.builder().statusUpdate(statusUpdate).build()
                        ));
    }
}
