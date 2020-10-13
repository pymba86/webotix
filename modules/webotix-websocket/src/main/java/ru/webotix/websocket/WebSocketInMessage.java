package ru.webotix.websocket;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import ru.webotix.market.data.api.TickerSpec;

import java.util.Collection;

@AutoValue
@JsonDeserialize
public abstract class WebSocketInMessage {

    @JsonCreator
    static WebSocketInMessage create(
            @JsonProperty("command") WebSocketCommandMessage command,
            @Nullable @JsonProperty("tickers") Collection<TickerSpec> tickers) {
        return new AutoValue_WebSocketInMessage(command, tickers);
    }

    @JsonProperty
    abstract WebSocketCommandMessage command();

    @JsonProperty
    @Nullable
    abstract Collection<TickerSpec> tickers();
}
