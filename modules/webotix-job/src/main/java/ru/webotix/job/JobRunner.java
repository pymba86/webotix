package ru.webotix.job;

import com.google.inject.Inject;
import ru.webotix.job.spi.Job;
import ru.webotix.job.spi.JobControl;
import ru.webotix.job.spi.JobProcessor;
import ru.webotix.job.spi.Status;

import java.util.UUID;

public class JobRunner {

    private final JobLocker jobLocker;
    private final JobAccess jobAccess;
    private final UUID uuid;

    @Inject
    JobRunner(
            JobLocker jobLocker,
            JobAccess jobAccess
    ) {
        this.jobLocker = jobLocker;
        this.jobAccess = jobAccess;
        this.uuid = UUID.randomUUID();
    }

    public boolean submitExisting(Job job) {
        if (jobLocker.attemptLock(job.id(), uuid)) {
            Job loadJob = jobAccess.load(job.id());
            startAfterCommit(loadJob);
            return true;
        }
        return false;
    }

    private void startAfterCommit(Job job) {
    }

    /**
     * Состояние задания
     */
    private enum JobStatus {
        CREATED,
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }

    private final class JobLifeTimeManager implements JobControl {

        private final JobProcessor<Job> processor;

        private volatile Job job;

        @Override
        public void replace(Job job) {

        }

        @Override
        public void finish(Status status) {

        }
    }
}
