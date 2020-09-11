package ru.webotix.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import ru.webotix.datasource.database.DatabaseConfiguration;
import ru.webotix.datasource.wiring.BackgroundProcessingConfiguration;
import ru.webotix.exchange.ExchangeConfiguration;
import ru.webotix.job.spi.JobRunConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

public class WebotixConfiguration extends Configuration
        implements BackgroundProcessingConfiguration {

    @Min(1L)
    @JsonProperty
    private int loopSeconds = 15;

    @Valid
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @Valid
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClient;

    @Valid
    @JsonProperty
    private Map<String, ExchangeConfiguration> exchanges = new HashMap<>();

    @Override
    public int getLoopSeconds() {
        return loopSeconds;
    }

    public void setLoopSeconds(int loopSeconds) {
        this.loopSeconds = loopSeconds;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfiguration database) {
        this.database = database;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

    public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    public Map<String, ExchangeConfiguration> getExchanges() {
        return exchanges;
    }

    public void setExchanges(Map<String, ExchangeConfiguration> exchanges) {
        this.exchanges = exchanges;
    }

    public void bind(Binder binder) {

        binder.bind(BackgroundProcessingConfiguration.class)
                .toInstance(this);

        // Конфигурация базы данных
        binder.bind(DatabaseConfiguration.class)
                .toProvider(Providers.of(database));

        // Конфигурация веб клиента
        binder.bind(JerseyClientConfiguration.class)
                .toProvider(Providers.of(jerseyClient));

        // Конфигурация бирж
        binder.bind(new TypeLiteral<Map<String, ExchangeConfiguration>>() {})
                .toProvider(Providers.of(exchanges));

        // Конфигурация менеджера задач
        JobRunConfiguration jobRunConfiguration = new JobRunConfiguration();

        jobRunConfiguration.setDatabaseLockSeconds(database.getLockSeconds());
        jobRunConfiguration.setGuardianLoopSeconds(loopSeconds);

        binder.bind(JobRunConfiguration.class)
                .toInstance(jobRunConfiguration);
    }
}
