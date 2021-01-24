package ru.webotix.auth;

public final class Headers {

    private Headers() {
        // Not constructable
    }

    public static final String SEC_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    public static final String X_XSRF_TOKEN = "x-xsrf-token";
    public static final String STRICT_CONTENT_SECURITY = "Strict-Transport-Security";
}
