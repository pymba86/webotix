package ru.webotix.datasource.database;

import com.google.inject.AbstractModule;

/**
 * Модуль источника данных (хранилище данных)
 */
public class DatabaseModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new DatabaseAccessModule());
    }
}
