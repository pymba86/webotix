package ru.webotix.datasource.database;

import org.alfasoftware.morf.jdbc.h2.H2;
import org.hibernate.dialect.H2Dialect;
import org.jooq.SQLDialect;

public class DatabaseDialectResolver {

    private DatabaseDialectResolver() {
        // Не инстанцируемый
    }

    static String hibernateDialect(String databaseType) {
        if (H2.IDENTIFIER.equals(databaseType)) {
            return H2Dialect.class.getName();
        }
        throw new UnsupportedOperationException("Unknown dialect");
    }

    static SQLDialect jooqDialect(String databaseType) {
        if (H2.IDENTIFIER.equals(databaseType)) {
            return SQLDialect.H2;
        }
        throw new UnsupportedOperationException("Unknown dialect");
    }
}
