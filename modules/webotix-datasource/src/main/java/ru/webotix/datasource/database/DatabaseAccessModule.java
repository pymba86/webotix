package ru.webotix.datasource.database;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.jdbc.SqlDialect;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.SchemaUtils;
import org.alfasoftware.morf.metadata.View;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import javax.sql.DataSource;

import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.deployedViewsTable;
import static org.alfasoftware.morf.upgrade.db.DatabaseUpgradeTableContribution.upgradeAuditTable;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseAccessModule extends AbstractModule {

    protected void configure() {

        Multibinder.newSetBinder(binder(), UpgradeStep.class);
        Multibinder.newSetBinder(binder(), TableContribution.class);
        Multibinder.newSetBinder(binder(), View.class);

        requestInjection(new DatabaseInit());
    }

    @Provides
    @Singleton
    ConnectionResources connectionResources(DatabaseConfiguration databaseConfiguration) {
        return databaseConfiguration.toConnectionResources();
    }

    @Provides
    @Singleton
    Schema schema(Set<TableContribution> contributions, Set<View> views) {
        return SchemaUtils.schema(
                SchemaUtils.schema(deployedViewsTable(), upgradeAuditTable()),
                SchemaUtils.schema(contributions.stream()
                        .map(TableContribution::tables)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())),
                SchemaUtils.schema(views)
        );
    }

    @Provides
    SqlDialect sqlDialect(ConnectionResources connectionResources) {
        return connectionResources.sqlDialect();
    }

    @Provides
    DataSource dataSource(ConnectionResources connectionResources) {
        return connectionResources.getDataSource();
    }

    private static final class DatabaseInit {

        @Inject
        void start(DatabaseSetup databaseSetup) {
            databaseSetup.setup();
        }
    }
}
