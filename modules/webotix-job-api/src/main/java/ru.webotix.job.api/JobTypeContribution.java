package ru.webotix.job.api;

public interface JobTypeContribution {

    Iterable<Class<? extends Job>> jobTypes();
}
