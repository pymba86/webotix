package ru.webotix.job;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.lifecycle.Managed;
import org.alfasoftware.morf.upgrade.TableContribution;

/**
 * Модуль управления заданиями
 */
public class JobModule extends AbstractModule {

    @Override
    protected void configure() {

        // Привязка классов миграций базы данных
        Multibinder<TableContribution> tableContributions =
                Multibinder.newSetBinder(
                        binder(), TableContribution.class);

        tableContributions.addBinding().to(JobRecordContribution.class);
        tableContributions.addBinding().to(JobLockContribution.class);

        // Привязка сущностей
        Multibinder<EntityContribution> entityContributions =
                Multibinder.newSetBinder(
                        binder(), EntityContribution.class);

        entityContributions.addBinding().to(JobRecordContribution.class);

        // Привязка веб API
        Multibinder.newSetBinder(binder(), WebResource.class)
                .addBinding().to(JobResource.class);

        // Управление блокировками заданий
        Multibinder.newSetBinder(binder(), Managed.class)
                .addBinding().to(RecordJobLocker.class);

    }
}
