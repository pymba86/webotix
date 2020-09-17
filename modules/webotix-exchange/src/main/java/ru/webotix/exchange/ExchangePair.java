package ru.webotix.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.knowm.xchange.currency.CurrencyPair;

public class ExchangePair {

    @JsonProperty
    public String counter;

    @JsonProperty
    public String base;

    public ExchangePair(String counter, String base) {
        this.counter = counter;
        this.base = base;
    }

    public ExchangePair(CurrencyPair currencyPair) {
        this.counter = currencyPair.counter.getCurrencyCode();
        this.base = currencyPair.base.getCurrencyCode();
    }
}
