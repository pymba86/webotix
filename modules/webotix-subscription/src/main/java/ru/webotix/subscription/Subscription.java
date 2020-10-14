package ru.webotix.subscription;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.webotix.market.data.api.TickerSpec;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity(name = Subscription.TABLE_NAME)
public final class Subscription {

    static final String TABLE_NAME = "Subscription";
    static final String TICKER_FIELD = "ticker";
    static final String REFERENCE_PRICE_FIELD = "referencePrice";

    @Id
    @Column(name = TICKER_FIELD, nullable = false)
    @NotNull
    @JsonProperty
    private String ticker;

    @Column(name = REFERENCE_PRICE_FIELD)
    @JsonProperty
    private BigDecimal referencePrice;

    public Subscription() {
        // Nothing to do
    }

    public Subscription(TickerSpec ticker, BigDecimal referencePrice) {
        super();
        this.ticker = ticker.key();
        this.referencePrice = referencePrice;
    }

    TickerSpec getTicker() {
        return TickerSpec.fromKey(ticker);
    }

    BigDecimal getReferencePrice() {
        return referencePrice;
    }

    void setReferencePrice(BigDecimal referencePrice) {
        this.referencePrice = referencePrice;
    }

}
