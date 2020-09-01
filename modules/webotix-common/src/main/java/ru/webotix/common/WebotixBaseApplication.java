package ru.webotix.common;

import com.google.inject.Inject;
import com.google.inject.Module;
import com.gruelbox.tools.dropwizard.guice.GuiceBundle;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import com.gruelbox.tools.dropwizard.guice.hibernate.HibernateBundleFactory;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.webotix.common.api.WebotixConfiguration;
import ru.webotix.datasource.database.DatabaseSetup;

public abstract class WebotixBaseApplication extends Application<WebotixConfiguration> {

    @Inject
    private DatabaseSetup databaseSetup;

    @Override
    public void initialize(Bootstrap<WebotixConfiguration> bootstrap) {
        HibernateBundleFactory<WebotixConfiguration> hibernateBundleFactory =
                new HibernateBundleFactory<>(configuration -> configuration
                        .getDatabase().toDataSourceFactory());

        bootstrap.addBundle(
                new GuiceBundle<>(this,
                        new WebotixApplicationModule(),
                        new GuiceHibernateModule(hibernateBundleFactory),
                        createApplicationModule()
                )
        );

        bootstrap.addBundle(hibernateBundleFactory.bundle());
    }

    protected abstract Module createApplicationModule();

    @Override
    public void run(WebotixConfiguration webotixConfiguration,
                    Environment environment) throws Exception {
        databaseSetup.setup();
    }
}
