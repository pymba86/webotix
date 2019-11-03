package ru.webotix.app;

import com.google.inject.AbstractModule;
import com.gruelbox.tools.dropwizard.guice.Configured;
import ru.webotix.common.WebotixConfiguration;
import ru.webotix.websocket.WebSocketModule;

public class WebotixModule extends AbstractModule implements Configured<WebotixConfiguration> {

    private WebotixConfiguration configuration;

    @Override
    public void setConfiguration(WebotixConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        install(new WebSocketModule());
    }
}
