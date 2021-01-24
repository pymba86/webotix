package ru.webotix.auth.jwt;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.auth.AbstractHttpSecurityServletFilter;
import ru.webotix.auth.Headers;

@Singleton
class JwtXsrfProtectionFilter extends AbstractHttpSecurityServletFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtXsrfProtectionFilter.class);

    private final Provider<Optional<JwtContext>> jwtContext;

    @Inject
    JwtXsrfProtectionFilter(Provider<Optional<JwtContext>> jwtContext) {
        this.jwtContext = jwtContext;
    }

    @Override
    protected final boolean filterHttpRequest(
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        String fullPath = request.getContextPath() + request.getServletPath() + request.getPathInfo();

        // TODO Slightly ugly. We want to let the DB dump API through our XSRF controls
        // since this is normally user-initiated. This should really be modular on
        // a plugin basis
        if (fullPath.equals("/api/db.zip")) {
            return true;
        }

        Optional<String> claim =
                jwtContext
                        .get()
                        .map(JwtContext::getJwtClaims)
                        .map(
                                claims -> {
                                    try {
                                        return claims.getClaimValue("xsrf", String.class);
                                    } catch (MalformedClaimException e) {
                                        LOGGER.warn("{}: malformed XSRF claim", fullPath);
                                        return null;
                                    }
                                });

        if (!claim.isPresent()) {
            LOGGER.warn("{}: failed cross-site scripting check (no claim)", fullPath);
            response.sendError(401);
            return false;
        }

        String xsrf = request.getHeader(Headers.X_XSRF_TOKEN);

        if (xsrf == null) {
            LOGGER.warn("{}: failed cross-site scripting check (no xsrf header)", fullPath);
            response.sendError(401);
            return false;
        }

        if (!claim.get().equals(xsrf)) {
            LOGGER.warn("{}: failed cross-site scripting check (mismatch)", fullPath);
            response.sendError(401);
            return false;
        }

        return true;
    }
}
