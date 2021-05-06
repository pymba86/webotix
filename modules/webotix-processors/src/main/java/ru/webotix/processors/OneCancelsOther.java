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
@JsonDeserialize(builder = OneCancelsOther.Builder.class)
public abstract class OneCancelsOther implements Job {

    public static final Builder builder() {
        return new AutoValue_OneCancelsOther.Builder();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder implements JobBuilder<OneCancelsOther> {
        @JsonCreator
        private static Builder create() {
            return OneCancelsOther.builder();
        }

        @Override
        public abstract Builder id(String value);

        public abstract Builder tickTrigger(TickerSpec tickTrigger);

        public abstract Builder low(ThresholdAndJob thresholdAndJob);

        public abstract Builder high(ThresholdAndJob thresholdAndJob);

        @Override
        public abstract OneCancelsOther build();
    }

    @Override
    @JsonIgnore
    public abstract Builder toBuilder();

    @Override
    @JsonProperty
    @Nullable
    public abstract String id();

    @Nullable
    @JsonProperty
    public abstract ThresholdAndJob low();

    @Nullable
    @JsonProperty
    public abstract ThresholdAndJob high();

    @JsonProperty
    public abstract TickerSpec tickTrigger();

    @Override
    public String toString() {
        if (high() == null) {
            return toStringLowOnly();
        } else {
            if (low() == null) {
                return toStringHighOnly();
            } else {
                return toStringHighOnly() + "; " + toStringLowOnly();
            }
        }
    }

    private String toStringHighOnly() {

        return high() != null ? String.format(
                "when price rises above %s on %s, execute: %s",
                high().threshold(), tickTrigger(), high().job())
                : String.format(
                "trigger price not specified on %s, execute: %s",
                tickTrigger(), high().job());
    }

    private String toStringLowOnly() {
        return low() != null ? String.format(
                "when price drops below %s on %s, execute: %s",
                low().threshold(), tickTrigger(), low().job())
                : String.format(
                "trigger price not specified on %s, execute: %s",
                tickTrigger(), low().job());
    }

    @JsonIgnore
    @Override
    public final Class<Processor.ProcessorFactory> processorFactory() {
        return Processor.ProcessorFactory.class;
    }

    public interface Processor extends JobProcessor<OneCancelsOther> {
        public interface ProcessorFactory extends JobProcessor.Factory<OneCancelsOther> {
            @Override
            Processor create(OneCancelsOther job, JobControl jobControl);
        }
    }

    @AutoValue
    public abstract static class ThresholdAndJob {

        public static ThresholdAndJob create(BigDecimal threshold, Job job) {
            return new AutoValue_OneCancelsOther_ThresholdAndJob(threshold, job);
        }

        @JsonCreator
        public static ThresholdAndJob createJson(
                @JsonProperty("thresholdAsString") String threshold, @JsonProperty("job") Job job) {
            return new AutoValue_OneCancelsOther_ThresholdAndJob(new BigDecimal(threshold), job);
        }

        @JsonProperty
        public final String thresholdAsString() {
            return threshold().toPlainString();
        }

        @JsonIgnore
        public abstract BigDecimal threshold();

        @JsonProperty
        public abstract Job job();
    }
}