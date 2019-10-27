package ru.webotix.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExchangeConfiguration {

    private String userName;
    private String secretKey;
    private String apiKey;
    private String passphrase;
    private boolean sandbox;
    private boolean loadRemoteData = true;

    @JsonProperty
    public String getUserName() {
        return userName;
    }

    @JsonProperty
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty
    public String getSecretKey() {
        return secretKey;
    }

    @JsonProperty
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @JsonProperty
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @JsonProperty
    public String getPassphrase() {
        return passphrase;
    }

    @JsonProperty
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    @JsonProperty
    public boolean isSandbox() {
        return sandbox;
    }

    @JsonProperty
    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    @JsonProperty
    public boolean isLoadRemoteData() {
        return loadRemoteData;
    }

    @JsonProperty
    public void setLoadRemoteData(boolean loadRemoteData) {
        this.loadRemoteData = loadRemoteData;
    }
}
