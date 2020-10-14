package ru.webotix.exchange.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;

import java.math.BigDecimal;

public class PairMetaData {

    @JsonProperty
    public BigDecimal maximumAmount;
    @JsonProperty
    public BigDecimal minimumAmount;
    @JsonProperty
    public Integer priceScale;

    public PairMetaData(CurrencyPairMetaData currencyPairMetaData) {
        this.minimumAmount = currencyPairMetaData.getMinimumAmount();
        this.maximumAmount = currencyPairMetaData.getMaximumAmount();
        this.priceScale = currencyPairMetaData.getPriceScale();
    }
}
