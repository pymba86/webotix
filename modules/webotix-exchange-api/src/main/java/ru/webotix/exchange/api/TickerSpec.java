package ru.webotix.exchange.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import org.knowm.xchange.currency.CurrencyPair;

/**
 * Представляет конкретную пару монет на конкретном обмене.
 *
 * Тикер на криптобирже - это краткий набор букв латинского алфавита,
 * который используется для обозначения акций компаний (IBM, APPLE)
 */
@AutoValue
@JsonDeserialize(builder = TickerSpec.Builder.class)
public abstract class TickerSpec {

    public static Builder builder() {
        return new AutoValue_TickerSpec.Builder();
    }

    public static TickerSpec fromKey(String key) {

        String[] split = key.split("/");

        return builder()
                .exchange(split[0])
                .counter(split[1])
                .base(split[2])
                .build();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder {

        @JsonCreator
        private static Builder create() {
            return TickerSpec.builder();
        }

        public abstract Builder exchange(String value);

        public abstract Builder counter(String value);

        public abstract Builder base(String value);

        public abstract TickerSpec build();

    }

    @JsonIgnore
    public abstract Builder toBuilder();

    @JsonProperty
    public abstract String exchange();

    @JsonProperty
    public abstract String counter();

    @JsonProperty
    public abstract String base();

    @JsonIgnore
    public final String pairName() {
        return base() + "/" + counter();
    }

    @Override
    public final String toString() {
        return base() + "/" + counter() + "(" + exchange() + ")";
    }

    @JsonIgnore
    public final String key() {
        return exchange() + "/" + counter() + "/" + base();
    }

    @JsonIgnore
    public final CurrencyPair currencyPair() {
        return new CurrencyPair(base(), counter());
    }
}
