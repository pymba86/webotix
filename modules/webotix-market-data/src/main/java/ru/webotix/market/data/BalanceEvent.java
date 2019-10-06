package ru.webotix.market.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize
public abstract class BalanceEvent {

    public static BalanceEvent create(
            @JsonProperty("exchange") String exchange,
            @JsonProperty("currency") String currency,
            @JsonProperty("balance") Balance balance) {
        return new AutoValue_BalanceEvent(exchange, currency, balance);
    }

    @JsonProperty
    public abstract String exchange();

    @JsonProperty
    public abstract String currency();

    @JsonProperty
    public abstract Balance balance();
}
