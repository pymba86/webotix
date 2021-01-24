package ru.webotix.auth;

import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticatedUserAuthorizer implements Authorizer<AuthenticatedUser> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatedUserAuthorizer.class);

    @Override
    public boolean authorize(AuthenticatedUser authenticatedUser, String role) {
        if (authenticatedUser == null) {
            log.warn("No user provided for authorization");
            return false;
        }
        return authenticatedUser.getRoles().contains(role);
    }
}
