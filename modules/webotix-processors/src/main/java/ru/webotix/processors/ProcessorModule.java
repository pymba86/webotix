package ru.webotix.processors;

import com.google.inject.AbstractModule;

public class ProcessorModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new AlertProcessor.Module());
        install(new StatusUpdateJobProcessor.Module());
    }
}
