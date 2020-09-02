package ru.webotix.job;

import static ru.webotix.job.JobRecord.TABLE_NAME;
import static ru.webotix.job.JobRecord.CONTENT_FILED;
import static ru.webotix.job.JobRecord.ID_FILED;
import static ru.webotix.job.JobRecord.PROCESSED_FILED;

import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.index;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import org.alfasoftware.morf.metadata.DataType;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import java.util.Collection;

@Singleton
class JobRecordContribution
        implements TableContribution, EntityContribution {

    @Override
    public Collection<Table> tables() {
        return ImmutableList.of(table(TABLE_NAME)
                .columns(
                        column(ID_FILED, DataType.STRING, 45).primaryKey(),
                        column(CONTENT_FILED, DataType.CLOB).nullable(),
                        column(PROCESSED_FILED, DataType.BOOLEAN)
                )
                .indexes(
                        index(TABLE_NAME + "_1").columns(PROCESSED_FILED)
                )
        );
    }

    @Override
    public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
        return ImmutableList.of();
    }

    @Override
    public Iterable<Class<?>> getEntities() {
        return ImmutableList.of(JobRecord.class);
    }
}
