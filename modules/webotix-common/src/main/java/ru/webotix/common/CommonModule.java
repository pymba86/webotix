package ru.webotix.common;

import com.google.inject.AbstractModule;
import ru.webotix.datasource.database.DatabaseModule;

public class CommonModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new DatabaseModule());
    }
}
