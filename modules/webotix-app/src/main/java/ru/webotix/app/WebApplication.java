package ru.webotix.app;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import io.dropwizard.Configuration;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import ru.webotix.common.JerseyBaseApplication;
import ru.webotix.common.UrlRewriteEnvironment;
import ru.webotix.websocket.WebSocketBundleInit;

public abstract class WebApplication<T extends Configuration>
        extends JerseyBaseApplication<T> implements Module {

    @Inject
    private WebSocketBundleInit webSocketBundleInit;

    @Inject private UrlRewriteEnvironment urlRewriteEnvironment;

    private WebsocketBundle websocketBundle;

    @Override
    public void initialize(final Bootstrap<T> bootstrap) {

        bootstrap.addBundle(
                new AssetsBundle("/assets/", "/", "index.html"));

        super.initialize(bootstrap);

        websocketBundle = new WebsocketBundle(new Class[]{});

        bootstrap.addBundle(websocketBundle);

    }

    @Override
    protected abstract Module createApplicationModule();

    @Override
    public void run(T webotixConfiguration, Environment environment) {
        urlRewriteEnvironment.init(environment);
        super.run(webotixConfiguration, environment);
        webSocketBundleInit.init(websocketBundle);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        binder.install(new ServletModule());
    }
}
