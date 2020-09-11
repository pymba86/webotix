package ru.webotix.common;

import com.google.inject.Provider;
import io.dropwizard.lifecycle.ServerLifecycleListener;
import org.eclipse.jetty.server.Server;

class JerseyServerProvider implements ServerLifecycleListener, Provider<Server> {

    private Server server;

    @Override
    public Server get() {
        return server;
    }

    @Override
    public void serverStarted(Server server) {
        this.server = server;
    }
}
