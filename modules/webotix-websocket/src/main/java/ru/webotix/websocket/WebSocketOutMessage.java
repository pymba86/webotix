package ru.webotix.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
public abstract class WebSocketOutMessage {

    @JsonCreator
    static WebSocketOutMessage create(@JsonProperty("nature") WebSocketNatureMessage nature,
                                      @JsonProperty("data") Object data) {
        return new AutoValue_WebSocketOutMessage(nature, data);
    }

    @JsonProperty
    abstract WebSocketNatureMessage nature();

    @JsonProperty
    abstract Object data();
}
