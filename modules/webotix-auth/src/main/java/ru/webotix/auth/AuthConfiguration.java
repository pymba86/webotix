package ru.webotix.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.webotix.auth.jwt.JwtConfiguration;

public class AuthConfiguration {

    @JsonProperty
    private JwtConfiguration jwt;

    @JsonProperty
    private boolean httpsOnly;

    private String authCachePolicy = "maximumSize=10000, expireAfterAccess=10m";

    public boolean isHttpsOnly() {
        return httpsOnly;
    }

    void setHttpsOnly(boolean httpsOnly) {
        this.httpsOnly = httpsOnly;
    }

    public String getAuthCachePolicy() {
        return authCachePolicy;
    }

    void setAuthCachePolicy(String authCachePolicy) {
        this.authCachePolicy = authCachePolicy;
    }

    public JwtConfiguration getJwt() {
        return jwt;
    }

    public void setJwt(JwtConfiguration jwt) {
        this.jwt = jwt;
    }
}
