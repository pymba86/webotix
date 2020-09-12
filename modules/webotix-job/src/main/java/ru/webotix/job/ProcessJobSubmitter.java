package ru.webotix.job;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.job.api.Job;
import ru.webotix.job.api.JobControl;
import ru.webotix.job.api.JobProcessor;
import ru.webotix.job.api.Validatable;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ProcessJobSubmitter implements JobSubmitter {

    private static final Logger log = LoggerFactory.getLogger(ProcessJobSubmitter.class);

    private final JobRunner jobRunner;
    private final Injector injector;

    @Inject
    ProcessJobSubmitter(JobRunner jobRunner, Injector injector) {
        this.jobRunner = jobRunner;
        this.injector = injector;
    }


    @Override
    public Job submitNew(Job job) throws Exception {

        // Присваиваем новый ид заданию
        if (isEmpty(job.id())) {
            job = job.toBuilder().id(UUID.randomUUID().toString()).build();
        }

        String id = job.id();

        jobRunner.submitNew(job,
                () -> log.info("submit new job success: {}", id),
                () -> log.info("submit new job failed: {}", id)
        );

        return job;
    }

    @Override
    public void validate(Job job, JobControl jobControl) {
        JobProcessor<Job> processor = JobProcessor.createProcessor(job, jobControl, injector);
        if (processor instanceof Validatable) {
            ((Validatable) processor).validate();
        }
    }
}
