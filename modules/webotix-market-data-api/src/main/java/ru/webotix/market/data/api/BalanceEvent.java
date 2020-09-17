package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.knowm.xchange.dto.account.Balance;

@AutoValue
@JsonDeserialize
public abstract class BalanceEvent {

    public static BalanceEvent create(
            @JsonProperty("exchange") String exchange,
            @JsonProperty("balance") Balance balance) {
        return new AutoValue_BalanceEvent(exchange, balance);
    }

    @JsonProperty
    public abstract String exchange();

    @JsonProperty
    public abstract Balance balance();
}
