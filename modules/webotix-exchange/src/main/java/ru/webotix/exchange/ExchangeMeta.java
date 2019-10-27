package ru.webotix.exchange;

/**
 * Информация о бирже, которая работает с системой в рантайме
 */
public final class ExchangeMeta {

    private final String code;
    private final String name;
    private final boolean authenticated;

    public ExchangeMeta(String code, String name, boolean authenticated) {
        this.code = code;
        this.name = name;
        this.authenticated = authenticated;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
