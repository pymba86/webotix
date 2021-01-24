package ru.webotix.auth;

import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.EnvironmentInitialiser;
import io.dropwizard.setup.Environment;
import ru.webotix.auth.jwt.JwtEnvironment;

import javax.inject.Inject;

@Singleton
class AuthEnvironment implements EnvironmentInitialiser {

    private final JwtEnvironment jwtEnvironment;
    private final ClientSecurityHeadersFilter clientSecurityHeadersFilter;

    @Inject
    AuthEnvironment(
            JwtEnvironment jwtEnvironment,
            ClientSecurityHeadersFilter clientSecurityHeadersFilter) {
        this.jwtEnvironment = jwtEnvironment;
        this.clientSecurityHeadersFilter = clientSecurityHeadersFilter;
    }

    @Override
    public void init(Environment environment) {
        jwtEnvironment.init(environment);
        environment
                .servlets()
                .addFilter(ClientSecurityHeadersFilter.class.getSimpleName(), clientSecurityHeadersFilter)
                .addMappingForUrlPatterns(null, true, "/*");
    }
}
