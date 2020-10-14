package ru.webotix.subscription;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import org.alfasoftware.morf.metadata.Table;
import org.alfasoftware.morf.upgrade.TableContribution;
import org.alfasoftware.morf.upgrade.UpgradeStep;

import java.util.Collection;

import static org.alfasoftware.morf.metadata.DataType.DECIMAL;
import static org.alfasoftware.morf.metadata.DataType.STRING;
import static org.alfasoftware.morf.metadata.SchemaUtils.column;
import static org.alfasoftware.morf.metadata.SchemaUtils.table;
import static ru.webotix.subscription.Subscription.*;

@Singleton
public class SubscriptionContribution implements TableContribution, EntityContribution {

    @Override
    public Collection<Table> tables() {
        return ImmutableList.of(
                table(TABLE_NAME)
                        .columns(
                                column(TICKER_FIELD, STRING, 32).primaryKey(),
                                column(REFERENCE_PRICE_FIELD, DECIMAL, 13, 8).nullable()));
    }

    @Override
    public Collection<Class<? extends UpgradeStep>> schemaUpgradeClassses() {
        return ImmutableList.of();
    }

    @Override
    public Iterable<Class<?>> getEntities() {
        return ImmutableList.of(Subscription.class);
    }
}
