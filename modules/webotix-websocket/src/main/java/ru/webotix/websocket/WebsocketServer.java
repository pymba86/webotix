package ru.webotix.websocket;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint(WebSocketModule.ENTRY_POINT)
public final class WebsocketServer {

    private static final Logger log = LoggerFactory.getLogger(WebsocketServer.class);

    private Session session;
    private Disposable disposable;

    @OnOpen
    public synchronized void myOnOpen(final Session session) {
        // TODO OnOpen websocket
    }

    @OnMessage
    public void myOnMsg(final Session session, String message) {
        // TODO OnMsg websocket
    }

    @OnClose
    public synchronized void myOnClose(final Session session, CloseReason cr) {
        // TODO OnClose websocket
    }

    @OnError
    public void onError(Throwable error) {
        // TODO OnError websocket
    }

}
