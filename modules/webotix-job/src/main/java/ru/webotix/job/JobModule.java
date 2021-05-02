package ru.webotix.job;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.lifecycle.Managed;
import org.alfasoftware.morf.upgrade.TableContribution;
import ru.webotix.job.api.JobAccess;
import ru.webotix.job.api.JobLocker;
import ru.webotix.job.api.JobSubmitter;

/**
 * Модуль управления заданиями
 */
public class JobModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(JobSubmitter.class)
                .to(ProcessJobSubmitter.class);

        bind(JobAccess.class)
                .to(RecordJobAccess.class);

        bind(JobLocker.class)
                .to(RecordJobLocker.class);

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

        // Управление блокировками заданий
        Multibinder.newSetBinder(binder(), Managed.class)
                .addBinding().to(RecordJobLocker.class);

        // Сервис запуска заданий
        Multibinder.newSetBinder(binder(), Service.class)
                .addBinding().to(GuardianLoop.class);

        // Привязка веб API
        Multibinder.newSetBinder(binder(), WebResource.class)
                .addBinding().to(JobResource.class);
    }
}
