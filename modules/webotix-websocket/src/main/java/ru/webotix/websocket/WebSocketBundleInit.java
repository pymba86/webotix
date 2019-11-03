package ru.webotix.websocket;

import com.google.inject.Inject;
import com.google.inject.Injector;
import io.dropwizard.websockets.WebsocketBundle;

import javax.websocket.server.ServerEndpointConfig;

public class WebSocketBundleInit {

    private final Injector injector;

    @Inject
    WebSocketBundleInit(Injector injector) {
        this.injector = injector;
    }

    public void init(WebsocketBundle websocketBundle) {
        final ServerEndpointConfig config = ServerEndpointConfig.Builder
                .create(WebSocketServer.class, WebSocketModule.ENTRY_POINT)
                .build();
        config.getUserProperties().put(Injector.class.getName(), injector);
        websocketBundle.addEndpoint(config);
    }
}
