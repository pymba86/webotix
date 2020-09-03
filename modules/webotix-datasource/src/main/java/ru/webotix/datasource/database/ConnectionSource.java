package ru.webotix.datasource.database;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.hibernate.SessionFactory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;

@Singleton
public class ConnectionSource {

    private final ThreadLocal<Connection> currentConnection = ThreadLocal
            .withInitial(() -> null);

    private final Provider<ConnectionResources> connectionResources;
    private final Provider<SessionFactory> sessionFactory;

    @Inject
    ConnectionSource(Provider<SessionFactory> sessionFactory,
                     Provider<ConnectionResources> connectionResources) {
        this.sessionFactory = sessionFactory;
        this.connectionResources = connectionResources;
    }

    public void withCurrentConnection(Runnable runnable) {
        withCurrentConnection(dsl -> runnable.run());
    }

    public void withCurrentConnection(Work work) {
        getWithCurrentConnection(
                dsl -> {
                    work.work(dsl);
                    return null;
                });
    }

    public <T> T getWithCurrentConnection(ReturningWork<T> supplier) {
        return sessionFactory.get()
                .getCurrentSession()
                .doReturningWork(
                        connection -> {
                            DSLContext dsl = DSL.using(connection,
                                    DatabaseDialectResolver.jooqDialect(
                                            connectionResources.get().getDatabaseType()
                                    )
                            );

                            return supplier.work(dsl);
                        });
    }

    public interface ReturningWork<T> {
        T work(DSLContext dsl) throws SQLException;
    }

    public interface Work {
        void work(DSLContext dsl) throws SQLException;
    }

    public boolean isConnectionOpen() {
        return currentConnection.get() != null;
    }

    public static final class RuntimeSqlException extends RuntimeException {

        public RuntimeSqlException(SQLException cause) {
            super(cause);
        }

        public RuntimeSqlException(String message, SQLException cause) {
            super(message, cause);
        }
    }
}
