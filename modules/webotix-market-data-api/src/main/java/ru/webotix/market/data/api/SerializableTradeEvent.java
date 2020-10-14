package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
public abstract class SerializableTradeEvent {

    @JsonCreator
    public static SerializableTradeEvent create(
            @JsonProperty("spec") TickerSpec spec, @JsonProperty("trade") SerializableTrade trade) {
        return new AutoValue_SerializableTradeEvent(spec, trade);
    }

    @JsonProperty
    public abstract TickerSpec spec();

    @JsonProperty
    public abstract SerializableTrade trade();

    @JsonIgnore
    public TradeEvent toTradeEvent() {
        return TradeEvent.create(spec(), trade().toTrade());
    }

    @JsonIgnore
    public UserTradeEvent toUserTradeEvent() {
        return UserTradeEvent.create(spec(), trade().toUserTrade());
    }
}