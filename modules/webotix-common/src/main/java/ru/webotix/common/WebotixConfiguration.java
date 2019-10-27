package ru.webotix.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import ru.webotix.datasource.database.DatabaseConfiguration;
import ru.webotix.exchange.ExchangeConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Базовая конфигурация приложения
 */
public class WebotixConfiguration extends Configuration {

    /**
     * Конфигурация базы данных. Если не предоставлено, приложение
     * будет использовать энергозависимое хранилище в памяти, которое,
     * очевидно, отлично подходит для тестирования, но быстро становится
     * бесполезным в реальной жизни.
     */
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();


    @Valid
    @NotNull
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    /**
     * Конфигурации бирж данных с которыми будет работать система
     */
    private Map<String, ExchangeConfiguration> exchanges;

    /**
     * Получить конфигурацию базы данных
     *
     * @return конфигурация базы данных
     */
    public DatabaseConfiguration getDatabase() {
        return database;
    }

    /**
     * Установить конфигурацию базы данных
     *
     * @param database конфигурация базы данных
     */
    public void setDatabase(DatabaseConfiguration database) {
        this.database = database;
    }

    /**
     * Получить конфигурацию веб RESTful клиента
     *
     * @return конфигурация веб клиента
     */
    public JerseyClientConfiguration getJerseyClient() {
        return jerseyClient;
    }

    /**
     * Установить конфигурацию веб RESTful клиента
     *
     * @param jerseyClient конфигурация веб клиента
     */
    public void setJerseyClient(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    /**
     * Получить конфигурации бирж
     *
     * @return конфигруации бирж по коду
     */
    public Map<String, ExchangeConfiguration> getExchanges() {
        return exchanges;
    }

    /**
     * Установить конфигурации бирж
     *
     * @param exchanges конфигурации бирж по коду
     */
    public void setExchanges(Map<String, ExchangeConfiguration> exchanges) {
        this.exchanges = exchanges;
    }
}
