package ru.webotix.common;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.ServletModule;

public class WebotixApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new ServletModule());
    }
}
