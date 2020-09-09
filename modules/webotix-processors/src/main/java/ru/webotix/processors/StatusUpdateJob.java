package ru.webotix.processors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import ru.webotix.job.spi.*;

import javax.annotation.Nullable;

@AutoValue
public abstract class StatusUpdateJob implements Job {


    public static final Builder builder() {
        return new AutoValue_StatusUpdateJob.Builder();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder implements JobBuilder<StatusUpdateJob> {

        @JsonCreator
        private static Builder create() {
            return StatusUpdateJob.builder();
        }

        @Override
        public abstract Builder id(String id);

        public abstract Builder statusUpdate(StatusUpdate statusUpdate);

        @Override
        public abstract StatusUpdateJob build();

    }

    @Override
    @JsonIgnore
    public abstract Builder toBuilder();

    @Override
    @JsonProperty
    @Nullable
    public abstract String id();

    @JsonProperty
    public abstract StatusUpdate statusUpdate();

    @Override
    public String toString() {
        return String.format("send status update: %s", statusUpdate());
    }

    @JsonIgnore
    @Override
    public final Class<Processor.ProcessorFactory> processorFactory() {
        return Processor.ProcessorFactory.class;
    }

    public interface Processor extends JobProcessor<StatusUpdateJob> {
        interface ProcessorFactory extends JobProcessor.Factory<StatusUpdateJob> {
            @Override
            Processor create(StatusUpdateJob job, JobControl jobControl);
        }
    }
}
