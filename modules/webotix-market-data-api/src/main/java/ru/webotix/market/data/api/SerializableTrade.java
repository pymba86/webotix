package ru.webotix.market.data.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.UserTrade;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@AutoValue
@JsonDeserialize
public abstract class SerializableTrade {

    public static SerializableTrade create(String exchange, UserTrade userTrade) {
        return create(
                userTrade.getType(),
                userTrade.getOriginalAmount(),
                tickerFromTrade(exchange, userTrade),
                userTrade.getPrice(),
                userTrade.getTimestamp(),
                userTrade.getId(),
                userTrade.getOrderId(),
                userTrade.getFeeAmount(),
                userTrade.getFeeCurrency().getCurrencyCode());
    }

    public static SerializableTrade create(
            String exchange, org.knowm.xchange.dto.marketdata.Trade trade) {
        if (trade instanceof UserTrade) return create(exchange, (UserTrade) trade);
        return create(
                trade.getType(),
                trade.getOriginalAmount(),
                tickerFromTrade(exchange, trade),
                trade.getPrice(),
                trade.getTimestamp(),
                trade.getId(),
                null,
                null,
                null);
    }

    private static TickerSpec tickerFromTrade(
            String exchange, org.knowm.xchange.dto.marketdata.Trade trade) {
        return TickerSpec.builder()
                .exchange(exchange)
                .base(trade.getCurrencyPair().base.getCurrencyCode())
                .counter(trade.getCurrencyPair().counter.getCurrencyCode())
                .build();
    }

    @JsonCreator
    public static SerializableTrade create(
            @JsonProperty("t") Order.OrderType type,
            @JsonProperty("a") BigDecimal originalAmount,
            @JsonProperty("c") TickerSpec spec,
            @JsonProperty("p") BigDecimal price,
            @JsonProperty("d") Date timestamp,
            @JsonProperty("id") String id,
            @JsonProperty("oid") String orderId,
            @JsonProperty("fa") BigDecimal feeAmount,
            @JsonProperty("fc") String feeCurrency) {
        return new AutoValue_SerializableTrade(
                type,
                originalAmount,
                spec,
                price,
                timestamp,
                id,
                orderId,
                feeAmount,
                feeCurrency);
    }

    @JsonProperty("t")
    public abstract Order.OrderType type();

    @JsonProperty("a")
    public abstract BigDecimal originalAmount();

    @JsonProperty("c")
    public abstract TickerSpec spec();

    @JsonProperty("p")
    public abstract BigDecimal price();

    @JsonProperty("d")
    public abstract Date timestamp();

    @JsonProperty("id")
    @Nullable
    public abstract String id();

    @JsonProperty("oid")
    @Nullable
    public abstract String orderId();

    @JsonProperty("fa")
    @Nullable
    public abstract BigDecimal feeAmount();

    @JsonProperty("fc")
    @Nullable
    public abstract String feeCurrency();

    @JsonIgnore
    public Trade toTrade() {
        return new Trade.Builder()
                .currencyPair(spec().currencyPair())
                .type(type())
                .originalAmount(originalAmount())
                .price(price())
                .timestamp(timestamp())
                .id(id())
                .build();
    }

    @JsonIgnore
    public UserTrade toUserTrade() {
        return new UserTrade.Builder()
                .currencyPair(spec().currencyPair())
                .type(type())
                .originalAmount(originalAmount())
                .price(price())
                .timestamp(timestamp())
                .id(id())
                .orderId(orderId())
                .feeAmount(feeAmount())
                .feeCurrency(Currency.getInstance(Objects.requireNonNull(feeCurrency())))
                .build();
    }
}