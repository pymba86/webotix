package ru.webotix.datasource.database;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DatabaseType;
import org.alfasoftware.morf.jdbc.JdbcUrlElements;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;


public class DatabaseTypeRegistry {

    private static final Log log = LogFactory.getLog(DatabaseTypeRegistry.class);

    private static final ImmutableMap<String, DatabaseType> registeredTypes;

    private static final ConnectionResourceFactory factory = new ConnectionResourceFactory();

    /*
     * Inspects the classpath for implementations of {@link DatabaseType} and registers them automatically.
     */
    static {
        log.info("Loading database types...");
        registeredTypes = Maps.uniqueIndex(ServiceLoader.load(DatabaseType.class), input -> {
            log.info(" - Registering [" + input.identifier() + "] as [" + input.getClass().getCanonicalName() + "]");
            return input.identifier();
        });
    }

    /**
     * Returns the registered database type by its identifier.
     *
     * <p>It can be assumed that performance of this method will be <code>O(1)</code> so is
     * suitable for repeated calling in performance code.  There should be
     * few reasons for caching the response.</p>
     *
     * @param identifier The database type identifier
     * @return The {@link DatabaseTypeRegistry}.
     * @throws IllegalArgumentException If no such identifier is found.
     */
    public static DatabaseType findByIdentifier(String identifier) {
        DatabaseType result = registeredTypes.get(identifier);
        if (result == null) throw new IllegalArgumentException("Identifier [" + identifier + "] not known");
        return result;
    }

    /**
     * Extracts the database connection details from a JDBC URL.
     *
     * <p>Finds the first available {@link DatabaseTypeRegistry} with a matching protocol,
     * then uses that to parse out the connection details.</p>
     *
     * <p>If there are multiple matches for the protocol, {@link IllegalStateException}
     * will be thrown.</p>
     *
     * <p>No performance guarantees are made, but it will be <em>at best</em>
     * <code>O(n),</code>where <code>n</code> is the number of registered
     * database types.</p>
     *
     * @param url The JDBC URL.
     * @return The connection details.
     * @throws IllegalArgumentException If no database type matching the URL
     *                                  protocol is found or the matching database type fails to parse
     *                                  the URL.
     */
    public static JdbcUrlElements parseJdbcUrl(String url) {
        Optional<JdbcUrlElements> result = scanForMatchingType(databaseType -> databaseType.extractJdbcUrl(url));
        if (!result.isPresent()) throw new IllegalArgumentException("[" + url + "] is not a valid JDBC URL");
        return result.get();
    }

    /**
     * If the JDBC URL matches a registered database type, returns a
     * {@link ConnectionResources} for the specified JDBC URL which can be used
     * immediately to connect to the target database, assuming that it contains the
     * necessary credentials.
     *
     * <p>If the URL is not understood by any registered database type, returns
     * empty.</p>
     *
     * <p>If there are multiple matches for the protocol, {@link IllegalStateException}
     * will be thrown.</p>
     *
     * <p>No performance guarantees are made, but it will be <em>at best</em>
     * <code>O(n),</code>where <code>n</code> is the number of registered
     * database types.</p>
     *
     * @param url The JDBC URL.
     * @return the {@link ConnectionResources}, if it could be constructed.
     */
    public static UrlConnectionResourcesBean urlToConnectionResources(String url) {
        Optional<UrlConnectionResourcesBean> result = scanForMatchingType(
                databaseType -> factory.build(databaseType, url)
        );

        if (!result.isPresent())
            throw new IllegalArgumentException("[" + url + "] is not supported by any registered database types");
        return result.get();
    }

    private static <T> Optional<T> scanForMatchingType(Function<DatabaseType, Optional<T>> processor) {
        Optional<T> result = Optional.empty();
        for (DatabaseType databaseType : registeredTypes.values()) {
            Optional<T> connectionDetails = processor.apply(databaseType);
            if (connectionDetails.isPresent()) {
               // if (result.isPresent())
                 //   throw new IllegalArgumentException("Search matches more than one registered database type");
               // result = connectionDetails;
                return connectionDetails;
            }
        }
        return result;
    }
}
