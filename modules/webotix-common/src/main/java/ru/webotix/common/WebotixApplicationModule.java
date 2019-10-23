package ru.webotix.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.ServletModule;
import io.dropwizard.setup.Environment;
import io.dropwizard.client.JerseyClientBuilder;
import ru.webotix.datasource.database.DatabaseConfiguration;

import javax.ws.rs.client.Client;

public class WebotixApplicationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new ServletModule());
        install(new CommonModule());
    }

    @Provides
    DatabaseConfiguration databaseConfiguration(WebotixConfiguration configuration) {
        return configuration.getDatabase();
    }

    @Provides
    ObjectMapper objectMapper(Environment environment) {
        return environment.getObjectMapper();
    }

    Client jerseyClient(Environment environment, WebotixConfiguration configuration) {
        return new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClient())
                .build("client");
    }
}
