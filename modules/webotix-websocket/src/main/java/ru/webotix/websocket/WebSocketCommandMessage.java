package ru.webotix.websocket;

public enum  WebSocketCommandMessage {
    CHANGE_TICKERS,
    CHANGE_OPEN_ORDERS,
    CHANGE_ORDER_BOOK,
    CHANGE_TRADES,
    CHANGE_USER_TRADES,
    CHANGE_BALANCE,
    CHANGE_ORDER_STATUS_CHANGE,
    UPDATE_SUBSCRIPTIONS,

    /**
     * The client should send this every 5 seconds to confirm it is keeping up with the
     * incoming data.  If the server doesn't receive this it will stop sending. This
     * may cause the connection to drop in extreme cases, but that's fine, the browser
     * will reconnect when it's able.
     */
    READY
}
