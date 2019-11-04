package ru.webotix.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import ru.webotix.exchange.info.TickerSpec;

import java.util.Collection;

@AutoValue
@JsonDeserialize
public abstract class WebSocketInMessage {

    @JsonCreator
    static WebSocketInMessage create(@JsonProperty("command") WebSocketCommandMessage command,
                                     @JsonProperty("tickers") Collection<TickerSpec> tickers) {
        return new AutoValue_WebSocketInMessage(command, tickers);
    }

    @JsonProperty
    abstract WebSocketCommandMessage command();

    @JsonProperty
    abstract Collection<TickerSpec> tickers();
}
