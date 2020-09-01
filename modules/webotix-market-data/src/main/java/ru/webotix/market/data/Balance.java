package ru.webotix.market.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.MoreObjects;

import java.math.BigDecimal;

/**
 * Баланс на бирже
 */
@AutoValue
public abstract class Balance {

    public static Balance create(org.knowm.xchange.dto.account.Balance balance) {
        return new AutoValue_Balance(
                balance.getCurrency().getCurrencyCode(),
                MoreObjects.firstNonNull(balance.getTotal(), BigDecimal.ZERO),
                MoreObjects.firstNonNull(balance.getAvailable(), BigDecimal.ZERO));
    }

    public static Balance zero(String currencyCode) {
        return new AutoValue_Balance(
                currencyCode,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
    }

    @JsonIgnore
    public abstract String currency();

    @JsonProperty
    public abstract BigDecimal total();

    @JsonProperty
    public abstract BigDecimal available();

}
