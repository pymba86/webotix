package ru.webotix.app;

import com.google.inject.Inject;
import com.google.inject.Module;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.websockets.WebsocketBundle;
import ru.webotix.common.WebotixBaseApplication;
import ru.webotix.common.WebotixConfiguration;
import ru.webotix.websocket.WebSocketBundleInit;

public abstract class WebHostApplication extends WebotixBaseApplication {

    @Inject
    private WebSocketBundleInit webSocketBundleInit;

    private WebsocketBundle websocketBundle;

    @Override
    public void initialize(Bootstrap<WebotixConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
        super.initialize(bootstrap);
        websocketBundle = new WebsocketBundle(new Class[] {});
        bootstrap.addBundle(websocketBundle);
    }

    @Override
    protected abstract Module createApplicationModule();

    @Override
    public void run(WebotixConfiguration webotixConfiguration, Environment environment) throws Exception {
        super.run(webotixConfiguration, environment);
        webSocketBundleInit.init(websocketBundle);
    }
}
