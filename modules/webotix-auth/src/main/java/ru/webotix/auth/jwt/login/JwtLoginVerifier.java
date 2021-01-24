package ru.webotix.auth.jwt.login;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.auth.Hasher;
import ru.webotix.auth.jwt.JwtConfiguration;

import java.util.Optional;

@Singleton
public class JwtLoginVerifier implements Authenticator<LoginRequest, PrincipalImpl> {

    private static final Logger log = LoggerFactory.getLogger(JwtLoginVerifier.class);

    private final JwtConfiguration config;

    private final Hasher hasher;

    @Inject
    public JwtLoginVerifier(JwtConfiguration config, Hasher hasher) {
        this.config = config;
        this.hasher = hasher;
    }

    @Override
    public Optional<PrincipalImpl> authenticate(LoginRequest credentials) throws AuthenticationException {

        Preconditions.checkNotNull(config, "No JWT auth configuration");
        Preconditions.checkNotNull(config, "No JWT auth username");
        Preconditions.checkNotNull(config, "No JWT auth username");

        if (valid(credentials)) {
            return Optional.of(new PrincipalImpl(credentials.getUsername()));
        }

        if (credentials == null) {
            log.warn("Invalid login attempt (no credentials)");
        } else {
            log.warn("Invalid login attempt by [{}]", credentials.getUsername());
        }

        return Optional.empty();
    }

    private boolean valid(LoginRequest credentials) {
        return credentials != null
                && userMatches(credentials)
                && passwordMatches(credentials);
    }

    private boolean userMatches(LoginRequest credentials) {
        return config.getUsername().equals(credentials.getUsername());
    }

    private boolean passwordMatches(LoginRequest credentials) {
        if (hasher.isHash(config.getPassword())) {
            return config
                    .getPassword()
                    .equals(hasher.hash(credentials.getPassword(), config.getPasswordSalt()));
        } else {
            return config.getPassword().equals(credentials.getPassword());
        }
    }
}
