package ru.webotix.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.NewCookie;
import java.util.Arrays;
import java.util.Optional;

public enum CookieHandlers {
    ACCESS_TOKEN(AuthModule.BIND_ACCESS_TOKEN_KEY);

    private final String name;

    private CookieHandlers(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Optional<String> read(HttpServletRequest request) {
        if (request.getCookies() == null)
            return Optional.empty();

        return Arrays.stream(request.getCookies())
                .filter(cookie -> getName().equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue);
    }

    public NewCookie create(String token, AuthConfiguration authConfiguration) {
        return new NewCookie(
                getName(),
                token,
                "/",
                null,
                1,
                null,
                authConfiguration.getJwt().getExpirationMinutes() * 60,
                null,
                authConfiguration.isHttpsOnly(),
                true);
    }
}
