package ru.webotix.auth.jwt.login;

import static org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import io.dropwizard.auth.PrincipalImpl;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import ru.webotix.auth.AuthConfiguration;

import java.util.Arrays;
import java.util.UUID;

public class TokenIssuer {

    public static final String XSRF_CLAIM = "xsrf";

    private final byte[] secret;
    private final int expiry;

    @Inject
    TokenIssuer(AuthConfiguration authConfiguration) {
        this.secret = authConfiguration.getJwt().getSecretBytes();
        this.expiry = authConfiguration.getJwt().getExpirationMinutes();
    }

    @VisibleForTesting
    public TokenIssuer(byte[] secret, int expiry) {
        this.secret = Arrays.copyOf(secret, secret.length);
        this.expiry = expiry;
    }

    public JsonWebSignature buildToken(PrincipalImpl user, String roles) {
        return claimsToToken(buildClaims(user, roles));
    }

    public JwtClaims buildClaims(PrincipalImpl user, String roles) {
        final JwtClaims claims = new JwtClaims();
        claims.setSubject(user.getName());
        claims.setStringClaim("roles", roles);
        claims.setStringClaim(XSRF_CLAIM, UUID.randomUUID().toString());
        claims.setExpirationTimeMinutesInTheFuture(expiry);
        claims.setIssuedAtToNow();
        claims.setGeneratedJwtId();
        return claims;
    }

    public JsonWebSignature claimsToToken(final JwtClaims claims) {
        final JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(HMAC_SHA256);
        jws.setKey(new HmacKey(secret));
        return jws;
    }
}
