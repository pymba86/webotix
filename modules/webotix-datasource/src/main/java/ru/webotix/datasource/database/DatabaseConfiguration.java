package ru.webotix.datasource.database;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.util.Duration;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.hibernate.cfg.AvailableSettings;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Driver;

/**
 * Конфигурация хранилища данных
 */
public class DatabaseConfiguration {

    /**
     * Строка подключения к базе данных
     */
    @NotNull
    @JsonProperty
    private String connectionString =
            "h2:file:./webotix.db;DB_CLOSE_DELAY=-1;MVCC=TRUE;DEFAULT_LOCK_TIMEOUT=60000";

    /**
     * Как долго должны сохраняться блокировки базы данных в секундах.
     */
    @NotNull
    @Min(10L)
    @JsonProperty
    private int lockSeconds = 45;

    /**
     * ZIP-файл, содержащий снимок базы данных Morf для загрузки в базу данных
     * при запуске. Извлечь из запущенного экземпляра с помощью.
     */
    @JsonProperty
    private String startPositionFile;

    /**
     * Установить строку подключения к базе
     *
     * @param connectionString строка подключения
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Получить строку подключения к базе
     *
     * @return строка подключения
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Получить время блокировки в секундах
     *
     * @return время блокировки к базе в секундах
     */
    public int getLockSeconds() {
        return lockSeconds;
    }

    /**
     * Установить время блокировки в секундах
     *
     * @param lockSeconds время блокировки к базе в секундах
     */
    public void setLockSeconds(int lockSeconds) {
        this.lockSeconds = lockSeconds;
    }

    /**
     * Получить смещение в базе данных
     *
     * @return смещение в базе данных
     */
    public String getStartPositionFile() {
        return startPositionFile;
    }

    /**
     * Установить смещение в базе данных
     *
     * @param startPositionFile смещение в базе данных
     */
    public void setStartPositionFile(String startPositionFile) {
        this.startPositionFile = startPositionFile;
    }

    /**
     * Получить параметры доступа подключения к базе данных
     *
     * @return параметры доступа
     */
    public ConnectionResources toConnectionResources() {
        return DatabaseTypeRegistry.urlToConnectionResources(
                "jdbc:" + connectionString
        );
    }

    /**
     * Получить название класса драйвера подключения к базе
     *
     * @return класс драйвера к базе
     */
    public String getDriverClassName() {
        return DatabaseTypeRegistry
                .findByIdentifier(toConnectionResources()
                        .getDatabaseType()).driverClassName();
    }

    /**
     * Получить драйвер подключения
     *
     * @return драйвер подключения
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Driver> getDriver() {
        try {
            return (Class<? extends Driver>) Class.forName(getDriverClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получить Url к базе данных
     *
     * @return url подключения к базе
     */
    public String getJdbcUrl() {
        return "jdbc:" + connectionString;
    }

    /**
     * Получить DataSource к базе данных
     *
     * @return DataSource базы данных
     */
    public DataSourceFactory toDataSourceFactory() {
        DataSourceFactory dsf = new DataSourceFactory();
        dsf.setDriverClass(getDriverClassName());
        dsf.setUrl(getJdbcUrl());
        dsf.setProperties(ImmutableMap.of(
                "charset", "UTF-8",
                "hibernate.dialect", DatabaseDialectResolver.hibernateDialect(
                        toConnectionResources().getDatabaseType()),
                AvailableSettings.LOG_SESSION_METRICS, "false"
        ));
        dsf.setMaxWaitForConnection(Duration.seconds(1));
        dsf.setValidationQuery("SELECT 1");
        dsf.setMinSize(1);
        dsf.setMaxSize(4);
        dsf.setCheckConnectionWhileIdle(false);
        return dsf;
    }
}
