package ru.webotix.auth.jwt.login;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.jersey.caching.CacheControl;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import ru.webotix.auth.AuthConfiguration;
import ru.webotix.auth.CookieHandlers;
import ru.webotix.auth.Roles;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("auth")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Singleton
public class LoginResource implements WebResource {

    private final AuthConfiguration authConfiguration;

    private final JwtLoginVerifier jwtLoginVerifier;

    private final TokenIssuer tokenIssuer;

    @Inject
    public LoginResource(AuthConfiguration authConfiguration,
                         JwtLoginVerifier jwtLoginVerifier,
                         TokenIssuer tokenIssuer) {
        this.authConfiguration = authConfiguration;
        this.jwtLoginVerifier = jwtLoginVerifier;
        this.tokenIssuer = tokenIssuer;
    }

    @GET
    @Path("/login")
    @CacheControl(noCache = true, noStore = true, maxAge = 0)
    public final Response getLogin(
            @QueryParam("username") String username,
            @QueryParam("password") String password,
            @QueryParam("secondfactor") int secondFactor)
            throws AuthenticationException, JoseException {
        return doLogin(new LoginRequest(username, password));
    }

    @POST
    @Path("/login")
    @CacheControl(noCache = true, noStore = true, maxAge = 0)
    public final Response doLogin(LoginRequest loginRequest)
            throws AuthenticationException, JoseException {


        Optional<PrincipalImpl> principal = jwtLoginVerifier.authenticate(loginRequest);
        if (!principal.isPresent()) {
            return Response.status(Response.Status.FORBIDDEN).entity(new LoginResponse()).build();
        }


        JwtClaims claims = tokenIssuer.buildClaims(principal.get(), Roles.TRADER);
        String token = tokenIssuer.claimsToToken(claims).getCompactSerialization();
        String xsrf = (String) claims.getClaimValue(TokenIssuer.XSRF_CLAIM);
        return Response.ok()
                .cookie(CookieHandlers.ACCESS_TOKEN.create(token, authConfiguration))
                .entity(new LoginResponse(authConfiguration.getJwt().getExpirationMinutes(), xsrf))
                .build();
    }
}
