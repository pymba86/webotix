package ru.webotix.job.status;

import com.google.inject.AbstractModule;
import ru.webotix.base.SubmissionType;
import ru.webotix.job.status.api.JobStatusService;

public class JobStatusModule extends AbstractModule {

    private final SubmissionType submissionType;

    public JobStatusModule(SubmissionType submissionType) {
        this.submissionType = submissionType;
    }

    @Override
    protected void configure() {

        if (submissionType == SubmissionType.ASYNC) {
            bind(JobStatusService.class)
                    .to(AsyncJobStatusService.class);
        } else {
            bind(JobStatusService.class)
                    .to(SyncJobStatusService.class);
        }
    }

}
