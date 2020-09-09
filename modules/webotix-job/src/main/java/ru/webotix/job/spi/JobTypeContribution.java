package ru.webotix.job.spi;

public interface JobTypeContribution {

    Iterable<Class<? extends Job>> jobTypes();
}
