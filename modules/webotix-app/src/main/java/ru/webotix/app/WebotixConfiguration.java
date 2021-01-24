package ru.webotix.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Providers;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.server.AbstractServerFactory;
import ru.webotix.auth.AuthConfiguration;
import ru.webotix.datasource.database.DatabaseConfiguration;
import ru.webotix.datasource.wiring.BackgroundProcessingConfiguration;
import ru.webotix.exchange.ExchangeConfiguration;
import ru.webotix.job.JobRunConfiguration;
import ru.webotix.script.ScriptConfiguration;
import ru.webotix.telegram.TelegramConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

public class WebotixConfiguration extends Configuration
        implements BackgroundProcessingConfiguration, ScriptConfiguration {

    @Min(1L)
    @JsonProperty
    private int loopSeconds = 15;

    @Valid
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @Valid
    @JsonProperty
    private TelegramConfiguration telegram;

    @Valid
    @JsonProperty
    private AuthConfiguration auth;

    @Valid
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClient;

    @JsonProperty
    private String scriptSigningKey;

    @Valid
    @JsonProperty
    private Map<String, ExchangeConfiguration> exchanges = new HashMap<>();

    @Override
    public int getLoopSeconds() {
        return loopSeconds;
    }

    @Override
    public String getScriptSigningKey() {
        return scriptSigningKey;
    }

    public void setScriptSigningKey(String scriptSigningKey) {
        this.scriptSigningKey = scriptSigningKey;
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

    public TelegramConfiguration getTelegram() {
        return telegram;
    }

    public void setTelegram(TelegramConfiguration telegram) {
        this.telegram = telegram;
    }

    public AuthConfiguration getAuth() {
        return auth;
    }

    public void setAuth(AuthConfiguration auth) {
        this.auth = auth;
    }

    public String getRootPath() {
        AbstractServerFactory serverFactory = (AbstractServerFactory) getServerFactory();
        return serverFactory.getJerseyRootPath().orElse("/") + "*";
    }

    public void bind(Binder binder) {

        // Конфигурация для частоты опросов в внутрениих процессах
        binder.bind(BackgroundProcessingConfiguration.class)
                .toInstance(this);

        // Конфигурация скриптового движка
        binder.bind(ScriptConfiguration.class)
                .toInstance(this);

        // Конфигурация базы данных
        binder.bind(DatabaseConfiguration.class)
                .toProvider(Providers.of(database));

        // Конфигурация веб клиента
        binder.bind(JerseyClientConfiguration.class)
                .toProvider(Providers.of(jerseyClient));

        binder.bind(AuthConfiguration.class)
                .toProvider(Providers.of(auth));

        // Конфигурация телеграм
        binder.bind(TelegramConfiguration.class)
                .toProvider(Providers.of(telegram));

        // Конфигурация бирж
        binder.bind(new TypeLiteral<Map<String, ExchangeConfiguration>>() {
        })
                .toProvider(Providers.of(exchanges));

        // Конфигурация менеджера задач
        JobRunConfiguration jobRunConfiguration = new JobRunConfiguration();

        jobRunConfiguration.setDatabaseLockSeconds(database.getLockSeconds());
        jobRunConfiguration.setGuardianLoopSeconds(loopSeconds);

        binder.bind(JobRunConfiguration.class)
                .toInstance(jobRunConfiguration);
    }
}
