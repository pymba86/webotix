package ru.webotix.subscription;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.lifecycle.Managed;
import org.alfasoftware.morf.upgrade.TableContribution;

public class SubscriptionModule extends AbstractModule {

    @Override
    protected void configure() {

        Multibinder.newSetBinder(binder(), WebResource.class)
                .addBinding()
                .to(SubscriptionResource.class);

        Multibinder.newSetBinder(binder(), Managed.class)
                .addBinding().to(SubscriptionManager.class);

        Multibinder.newSetBinder(binder(), TableContribution.class)
                .addBinding()
                .to(SubscriptionContribution.class);

        Multibinder.newSetBinder(binder(), EntityContribution.class)
                .addBinding()
                .to(SubscriptionContribution.class);
    }
}
