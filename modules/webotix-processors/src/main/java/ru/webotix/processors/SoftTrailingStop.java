package ru.webotix.processors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import ru.webotix.job.api.Job;
import ru.webotix.job.api.JobBuilder;
import ru.webotix.job.api.JobControl;
import ru.webotix.job.api.JobProcessor;
import ru.webotix.market.data.api.TickerSpec;

import javax.annotation.Nullable;
import java.math.BigDecimal;

@AutoValue
@JsonDeserialize(builder = SoftTrailingStop.Builder.class)
public abstract class SoftTrailingStop implements Job {

    public static Builder builder() {
        return new AutoValue_SoftTrailingStop.Builder()
                .balanceState(LimitOrderJob.BalanceState.SUFFICIENT_BALANCE);
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder implements JobBuilder<SoftTrailingStop> {

        @JsonCreator
        private static Builder create() {
            return SoftTrailingStop.builder();
        }

        @Override
        public abstract Builder id(String value);

        public abstract Builder tickTrigger(TickerSpec tickTrigger);

        public abstract Builder direction(LimitOrderJob.Direction direction);

        public abstract Builder amount(BigDecimal amount);

        public abstract Builder lastSyncPrice(BigDecimal value);

        public abstract Builder stopPrice(BigDecimal value);

        public abstract Builder stopPercentage(BigDecimal value);

        public abstract Builder limitPercentage(BigDecimal value);

        abstract Builder balanceState(LimitOrderJob.BalanceState balanceState);

        abstract BigDecimal lastSyncPrice();

        abstract BigDecimal stopPrice();

        @Override
        public abstract SoftTrailingStop build();
    }

    @Override
    @JsonIgnore
    public abstract Builder toBuilder();

    @Override
    @JsonProperty
    @Nullable
    public abstract String id();

    @JsonProperty
    public abstract TickerSpec tickTrigger();

    @JsonProperty
    public abstract LimitOrderJob.Direction direction();

    @JsonProperty
    public abstract BigDecimal amount();

    @JsonProperty
    public abstract BigDecimal lastSyncPrice();

    @JsonProperty
    public abstract BigDecimal stopPrice();

    @JsonProperty
    public abstract BigDecimal stopPercentage();

    @JsonProperty
    public abstract BigDecimal limitPercentage();

    @JsonProperty
    abstract LimitOrderJob.BalanceState balanceState();

    @Override
    public String toString() {
        return String.format(
                "soft trailing stop: %s %s at %s, stop %s, limit %s on %s",
                amount(), tickTrigger().base(), stopPrice(),
                stopPercentage(), limitPercentage(), tickTrigger());
    }

    @JsonIgnore
    @Override
    public final Class<Processor.ProcessorFactory> processorFactory() {
        return Processor.ProcessorFactory.class;
    }


    public interface Processor extends JobProcessor<SoftTrailingStop> {
        public interface ProcessorFactory extends JobProcessor.Factory<SoftTrailingStop> {
            @Override
            Processor create(SoftTrailingStop job, JobControl jobControl);
        }
    }
}
