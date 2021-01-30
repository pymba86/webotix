package ru.webotix.processors;

import com.google.inject.AbstractModule;

public class ProcessorModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AlertJobProcessor.Module());
        install(new UpdateStatusJobProcessor.Module());
        install(new LimitOrderJobProcessor.Module());
    }
}
