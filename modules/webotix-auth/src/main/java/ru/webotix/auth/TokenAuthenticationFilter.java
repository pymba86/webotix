package ru.webotix.auth;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public abstract class TokenAuthenticationFilter extends AbstractHttpSecurityServletFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationFilter.class);

    protected abstract Optional<AuthenticatedUser> extractPrincipal(String token);

    @Inject
    @Named(AuthModule.BIND_ACCESS_TOKEN_KEY)
    private Provider<Optional<String>> accessToken;

    @Inject private AuthenticatedUserAuthorizer authenticatedUserAuthorizer;

    @Override
    protected final boolean filterHttpRequest(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

        Optional<String> token = accessToken.get();
        if (!token.isPresent()) {
            log.warn("{}: no access token", fullPath);
            response.sendError(401);
            return false;
        }

        Optional<AuthenticatedUser> principal = extractPrincipal(token.get());
        if (!principal.isPresent()) {
            response.sendError(401);
            return false;
        }

        if (!authenticatedUserAuthorizer.authorize(principal.get(), Roles.TRADER)) {
            log.warn("{}: user [{}] not authorised", fullPath, principal.get().getName());
            response.sendError(401);
            return false;
        }
        return true;
    }
}
