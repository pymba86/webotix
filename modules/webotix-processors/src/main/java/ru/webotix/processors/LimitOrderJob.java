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
@JsonDeserialize(builder = LimitOrderJob.Builder.class)
public abstract class LimitOrderJob implements Job {

    public static Builder builder() {
        return new AutoValue_LimitOrderJob.Builder().balanceState(BalanceState.SUFFICIENT_BALANCE);
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder implements JobBuilder<LimitOrderJob> {

        @JsonCreator
        private static Builder create() {
            return LimitOrderJob.builder();
        }

        @Override
        public abstract Builder id(String value);

        public abstract Builder tickTrigger(TickerSpec tickTrigger);

        public abstract Builder amount(BigDecimal amount);

        public abstract Builder limitPrice(BigDecimal value);

        public abstract Builder direction(Direction direction);

        abstract Builder balanceState(BalanceState balanceState);

        @Override
        public abstract LimitOrderJob build();
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
    public abstract Direction direction();

    @JsonProperty
    public abstract BigDecimal amount();

    @JsonProperty
    public abstract BigDecimal limitPrice();

    @JsonProperty
    abstract BalanceState balanceState();

    @Override
    public String toString() {
        return String.format(
                "%s order: %s %s at %s on %s",
                direction(), amount(), tickTrigger().base(), limitPrice(), tickTrigger());
    }

    @JsonIgnore
    @Override
    public final Class<Processor.ProcessorFactory> processorFactory() {
        return Processor.ProcessorFactory.class;
    }

    public enum Direction {
        BUY,
        SELL
    }

    public enum BalanceState {
        SUFFICIENT_BALANCE,
        INSUFFICIENT_BALANCE
    }

    public interface Processor extends JobProcessor<LimitOrderJob> {
        public interface ProcessorFactory extends JobProcessor.Factory<LimitOrderJob> {
            @Override
            Processor create(LimitOrderJob job, JobControl jobControl);
        }
    }
}
