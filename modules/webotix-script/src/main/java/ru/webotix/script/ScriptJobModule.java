package ru.webotix.script;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.gruelbox.tools.dropwizard.guice.hibernate.EntityContribution;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import org.alfasoftware.morf.upgrade.TableContribution;

public class ScriptJobModule extends AbstractModule {
    @Override
    protected void configure() {

        install(new ScriptJobProcessor.Module());

        Multibinder.newSetBinder(binder(), WebResource.class)
                .addBinding().to(ScriptResource.class);

        Multibinder.newSetBinder(binder(), EntityContribution.class)
                .addBinding()
                .to(ScriptContribution.class);

        Multibinder.newSetBinder(binder(), TableContribution.class)
                .addBinding()
                .to(ScriptContribution.class);
    }
}