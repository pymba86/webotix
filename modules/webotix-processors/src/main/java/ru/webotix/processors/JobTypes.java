package ru.webotix.processors;

import com.google.common.collect.ImmutableList;
import ru.webotix.job.spi.Job;
import ru.webotix.job.spi.JobTypeContribution;

public class JobTypes implements JobTypeContribution {

    @Override
    public Iterable<Class<? extends Job>> jobTypes() {
        return ImmutableList.of(
                AutoValue_Alert.class
        );
    }
}
