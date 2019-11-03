package ru.webotix.datasource.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.xml.crypto.Data;

import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.DataSourceAdapter;
import org.alfasoftware.morf.jdbc.SchemaResourceImpl;
import org.alfasoftware.morf.jdbc.SqlDialect;
import org.alfasoftware.morf.metadata.SchemaResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Preconditions;

/**
 * A {@link ConnectionResources} implementation which uses only the JDBC URL,
 * where possible.
 *
 * <p>Create using
 * {@link DatabaseType.Registry#urlToConnectionResources(String)}.</p>
 */
public class UrlConnectionResourcesBean implements ConnectionResources {

    private static final Log log = LogFactory.getLog(UrlConnectionResourcesBean.class);

    private static final Pattern REMOVE_CREDENTIALS = Pattern.compile("//(.*)@");

    private final String url;
    private final String databaseType;
    private final String databaseName;

    private String schemaName;
    private int transactionIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;
    private int statementPoolingMaxStatements;

    /**
     * Constructor intended for use by implementations of {@link DatabaseType}.
     *
     * @param url The JDBC URL.
     * @param databaseType The database type identifier.
     * @param databaseName The name of the database. Used for tagging metadata. Can be anything.
     */
    public UrlConnectionResourcesBean(String url, String databaseType, String databaseName) {
        this.url = url;
        this.databaseType = databaseType;
        this.databaseName = databaseName;
    }

    /**
     * @see org.alfasoftware.morf.jdbc.ConnectionResources#sqlDialect()
     */
    @Override
    public final SqlDialect sqlDialect() {
        return findDatabaseType().sqlDialect(getSchemaName());
    }


    private DatabaseType findDatabaseType() {
        return DatabaseType.Registry.findByIdentifier(getDatabaseType());
    }


    /**
     * {@inheritDoc}
     *
     * @see org.alfasoftware.morf.jdbc.ConnectionResources#getDataSource()
     */
    @Override
    public DataSource getDataSource() {
        return new ConnectionDetailsDataSource();
    }


    /**
     * @return {@link XADataSource} created for this {@link ConnectionResources}
     */
    public final XADataSource getXADataSource() {
        Preconditions.checkNotNull(getDatabaseType(), "Cannot create XADataSource without defined DatabaseType");
        return findDatabaseType().getXADataSource(getJdbcUrl(), null, null);
    }


    /**
     * @see org.alfasoftware.morf.jdbc.ConnectionResources#openSchemaResource()
     */
    @Override
    public final SchemaResource openSchemaResource() {
        return openSchemaResource(getDataSource());
    }


    /**
     * @see org.alfasoftware.morf.jdbc.ConnectionResources#openSchemaResource(DataSource)
     */
    @Override
    public final SchemaResource openSchemaResource(DataSource dataSource) {
        return SchemaResourceImpl.create(dataSource, this);
    }


    /**
     * @return a formatted jdbc url string.
     */
    public String getJdbcUrl() {
        return url;
    }


    @Override
    public String getDatabaseType() {
        return databaseType;
    }


    @Override
    public String getSchemaName() {
        return schemaName;
    }


    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }


    @Override
    public String getDatabaseName() {
        return databaseName;
    }


    /**
     * @see org.alfasoftware.morf.jdbc.ConnectionResources#getStatementPoolingMaxStatements()
     */
    @Override
    public int getStatementPoolingMaxStatements() {
        return statementPoolingMaxStatements;
    }


    /**
     * @see org.alfasoftware.morf.jdbc.AbstractConnectionResources#setStatementPoolingMaxStatements(int)
     */
    public void setStatementPoolingMaxStatements(int statementPoolingMaxStatements) {
        this.statementPoolingMaxStatements = statementPoolingMaxStatements;
    }


    /**
     * Gets the transaction isolation level to use (e.g. {@link Connection#TRANSACTION_READ_COMMITTED}).
     *
     * <p>Defaults to {@link Connection#TRANSACTION_READ_COMMITTED} if not specified.</p>
     *
     * @return The isolation level.
     */
    public int getTransactionIsolationLevel() {
        return transactionIsolationLevel;
    }


    /**
     * Sets the transaction isolation level to use (e.g. {@link Connection#TRANSACTION_READ_COMMITTED}).
     *
     * <p>Defaults to {@link Connection#TRANSACTION_READ_COMMITTED} if not specified.</p>
     *
     * @param transactionIsolationLevel The isolation level.
     */
    public void setTransactionIsolationLevel(int transactionIsolationLevel) {
        this.transactionIsolationLevel = transactionIsolationLevel;
    }


    /**
     * Implementation of data source based on this {@link ConnectionResources}.
     *
     * @author Copyright (c) Alfa Financial Software 2010
     */
    private final class ConnectionDetailsDataSource extends DataSourceAdapter {

        /**
         * @see javax.sql.DataSource#getConnection()
         */
        @Override
        public Connection getConnection() throws SQLException {
            return getConnection(null, null);
        }

        /**
         * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
         */
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            log.info("Opening new database connection to [" + maskUserCredentials(UrlConnectionResourcesBean.this.getJdbcUrl()) + "]");
            loadJdbcDriver();
            return openConnection(username, password);
        }


        private String maskUserCredentials(String url) {
            return REMOVE_CREDENTIALS.matcher(url).replaceAll("//");
        }


        private void loadJdbcDriver() {
            String driverClassName = findDatabaseType().driverClassName();
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load JDBC driver [" + driverClassName + "] for DatabaseType [" + UrlConnectionResourcesBean.this.getDatabaseType() + "]", e);
            }
        }


        private Connection openConnection(String username, String password) throws SQLException {
            Connection connection;
            try {
                connection = username == null
                        ? DriverManager.getConnection(UrlConnectionResourcesBean.this.getJdbcUrl())
                        : DriverManager.getConnection(UrlConnectionResourcesBean.this.getJdbcUrl(), username, password);
            } catch (SQLException se) {
                log.error(String.format("Unable to connect to URL: %s, with user: %s", maskUserCredentials(UrlConnectionResourcesBean.this.getJdbcUrl()), username));
                throw se;
            }
            try {
                connection.setTransactionIsolation(transactionIsolationLevel);
                return connection;
            } catch (Exception e) {
                connection.close();
                throw e;
            }
        }
    }
}
