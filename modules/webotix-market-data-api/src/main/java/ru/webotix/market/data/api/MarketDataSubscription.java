package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
@AutoValue
@JsonDeserialize
public abstract class MarketDataSubscription {

    public static MarketDataSubscription create(
            @JsonProperty("spec") TickerSpec spec,
            @JsonProperty("type") MarketDataType type) {
        return new AutoValue_MarketDataSubscription(spec, type);
    }

    @JsonProperty
    public abstract TickerSpec spec();

    @JsonProperty
    public abstract MarketDataType type();

    @JsonIgnore
    public final String key() {
        return spec().key() + "/" + type();
    }

    @Override
    public final String toString() {
        return key();
    }

}
