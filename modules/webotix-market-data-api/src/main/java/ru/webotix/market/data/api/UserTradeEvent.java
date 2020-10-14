package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.knowm.xchange.dto.trade.UserTrade;

@AutoValue
@JsonDeserialize
public abstract class UserTradeEvent {

    @JsonCreator
    public static UserTradeEvent create(
            @JsonProperty("spec") TickerSpec spec, @JsonProperty("trade") UserTrade trade) {
        return new AutoValue_UserTradeEvent(spec, trade);
    }

    @JsonProperty
    public abstract TickerSpec spec();

    @JsonProperty
    public abstract UserTrade trade();
}