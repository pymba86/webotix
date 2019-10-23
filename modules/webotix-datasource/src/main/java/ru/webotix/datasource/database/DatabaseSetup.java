package ru.webotix.datasource.database;

import com.google.inject.Inject;
import org.alfasoftware.morf.jdbc.ConnectionResources;
import org.alfasoftware.morf.metadata.Schema;
import org.alfasoftware.morf.metadata.SchemaResource;
import org.alfasoftware.morf.upgrade.Deployment;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.Upgrade;
import org.alfasoftware.morf.upgrade.UpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class DatabaseSetup {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSetup.class);

    private final Schema targetSchema;
    private final ConnectionResources connectionResources;
    private final Set<TableContribution> tableContributions;

    @Inject
    public DatabaseSetup(Schema targetSchema,
                         ConnectionResources connectionResources,
                         Set<TableContribution> tableContributions) {
        this.targetSchema = targetSchema;
        this.connectionResources = connectionResources;
        this.tableContributions = tableContributions;
    }

    public void setup() {
        try (SchemaResource currentSchema = connectionResources.openSchemaResource()) {
            Set<Class<? extends UpgradeStep>> upgradeSteps = tableContributions.stream()
                    .flatMap(c -> c.schemaUpgradeClassses().stream())
                    .collect(toSet());
            if (currentSchema.isEmptyDatabase()) {
                log.info("Empty database. Deploying schema");
                Deployment.deploySchema(targetSchema, upgradeSteps, connectionResources);
            } else {
                log.info("Existing database. Checking and upgrading");
                Upgrade.performUpgrade(targetSchema, upgradeSteps, connectionResources);
            }
        }
    }
}
