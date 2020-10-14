package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.knowm.xchange.dto.Order;

import java.util.Date;

@AutoValue
@JsonDeserialize
public abstract class OrderChangeEvent {

    @JsonCreator
    public static OrderChangeEvent create(
            @JsonProperty("spec") TickerSpec spec,
            @JsonProperty("order") Order order,
            @JsonProperty("timestamp") Date timestamp) {
        return new AutoValue_OrderChangeEvent(spec, order, timestamp);
    }

    @JsonProperty
    public abstract TickerSpec spec();

    @JsonProperty
    public abstract Order order();

    @JsonProperty
    public abstract Date timestamp();
}
