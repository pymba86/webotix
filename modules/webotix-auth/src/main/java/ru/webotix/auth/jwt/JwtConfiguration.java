package ru.webotix.auth.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import java.nio.charset.StandardCharsets;

public class JwtConfiguration {

    @JsonProperty
    private String secret;

    @JsonProperty
    private String username;

    @JsonProperty
    private String password;

    @JsonProperty
    private String passwordSalt;

    @JsonProperty
    private final int expirationMinutes = 60 * 24;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public byte[] getSecretBytes() {
        Preconditions.checkNotNull(secret);
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public int getExpirationMinutes() {
        return expirationMinutes;
    }

    public boolean isEnabled() {
        return StringUtils.isNoneEmpty(secret);
    }
}
