package ru.webotix.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

class AccessTokenProvider implements Provider<Optional<String>> {

    private final Provider<HttpServletRequest> request;

    @Inject
    AccessTokenProvider(Provider<HttpServletRequest> request) {
        this.request = request;
    }

    @Override
    public Optional<String> get() {
        return CookieHandlers.ACCESS_TOKEN.read(request.get());
    }
}
