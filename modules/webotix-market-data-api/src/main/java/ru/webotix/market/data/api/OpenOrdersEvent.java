package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.knowm.xchange.dto.trade.OpenOrders;

import java.util.Date;

@AutoValue
@JsonDeserialize
public abstract class OpenOrdersEvent {

    @JsonCreator
    public static OpenOrdersEvent create(
            @JsonProperty("spec") TickerSpec spec,
            @JsonProperty("openOrders") OpenOrders openOrders,
            @JsonProperty("timestamp") Date timestamp) {
        return new AutoValue_OpenOrdersEvent(spec, openOrders, timestamp);
    }

    @JsonProperty
    public abstract TickerSpec spec();

    @JsonProperty
    public abstract OpenOrders openOrders();

    @JsonProperty
    public abstract Date timestamp();
}