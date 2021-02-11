package ru.webotix.app;

import com.google.inject.Module;
import com.gruelbox.tools.dropwizard.guice.hibernate.GuiceHibernateModule;
import com.gruelbox.tools.dropwizard.guice.hibernate.HibernateBundleFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.web.WebBundle;
import io.dropwizard.web.conf.WebConfiguration;

public class WebotixApplication extends WebApplication<WebotixConfiguration> {

    private WebotixModule webotixModule;

    public static void main(String... args) throws Exception {
        new WebotixApplication().run(args);
    }

    @Override
    public String getName() {
        return "Webotix";
    }

    @Override
    public void initialize(Bootstrap<WebotixConfiguration> bootstrap) {

        HibernateBundleFactory<WebotixConfiguration> hibernateBundleFactory
                = new HibernateBundleFactory<>(
                configuration -> configuration.getDatabase().toDataSourceFactory());

        webotixModule = new WebotixModule(new GuiceHibernateModule(hibernateBundleFactory));

        super.initialize(bootstrap);

        bootstrap.addBundle(new WebBundle<WebotixConfiguration>() {
            @Override
            public WebConfiguration getWebConfiguration(final WebotixConfiguration configuration) {
                return configuration.getWeb();
            }
        });

        bootstrap.addBundle(hibernateBundleFactory.bundle());
    }

    @Override
    protected Module createApplicationModule() {
        return webotixModule;
    }

    @Override
    protected void addDefaultCommands(Bootstrap<WebotixConfiguration> bootstrap) {
        super.addDefaultCommands(bootstrap);

        bootstrap.addCommand(new HashCommand());
    }
}
