package ru.webotix.websocket;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import ru.webotix.auth.AuthModule;

public class WebSocketModule extends AbstractModule {

    public static final String ENTRY_POINT = "/ws";

    @Provides
    @Named(AuthModule.BIND_WEBSOCKET_ENTRY_POINT)
    @Singleton
    String webSocketEntryPoint() {
        return WebSocketModule.ENTRY_POINT;
    }
}
