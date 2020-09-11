package ru.webotix.common;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.gruelbox.tools.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;

public abstract class JerseyBaseApplication<T extends Configuration>
        extends Application<T> implements Module {

    private final JerseyServerProvider serverProvider
            = new JerseyServerProvider();

    @Override
    public void initialize(final Bootstrap<T> bootstrap) {

        bootstrap.addBundle(
                new GuiceBundle<>(this, this,
                        createApplicationModule()));
    }

    @Override
    public void configure(Binder binder) {

        binder.install(new JerseySupportModule());

        binder.bind(Server.class).toProvider(serverProvider);

    }

    protected abstract Module createApplicationModule();

    @Override
    public void run(T webotixConfiguration, Environment environment)  {
        environment.lifecycle().addServerLifecycleListener(serverProvider);
    }
}
