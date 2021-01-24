package ru.webotix.auth.jwt;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.Optional;
import javax.annotation.Priority;
import org.jose4j.jwt.consumer.JwtContext;
import ru.webotix.auth.AuthenticatedUser;
import ru.webotix.auth.TokenAuthenticationFilter;

@Singleton
@Priority(102)
class JwtAuthenticationFilter extends TokenAuthenticationFilter {

    private final JwtAuthenticatorAuthorizer authenticator;
    private final Provider<Optional<JwtContext>> jwtContext;

    @Inject
    JwtAuthenticationFilter(
            JwtAuthenticatorAuthorizer authenticator, Provider<Optional<JwtContext>> jwtContext) {
        this.authenticator = authenticator;
        this.jwtContext = jwtContext;
    }

    @Override
    protected Optional<AuthenticatedUser> extractPrincipal(String token) {
        return jwtContext.get().flatMap(authenticator::authenticate);
    }
}
