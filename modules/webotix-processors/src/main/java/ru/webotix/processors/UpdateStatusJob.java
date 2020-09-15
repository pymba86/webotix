package ru.webotix.processors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import ru.webotix.job.api.*;
import ru.webotix.job.status.api.JobStatus;

import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = UpdateStatusJob.Builder.class)
public abstract class UpdateStatusJob implements Job {

    public static Builder builder() {
        return new AutoValue_UpdateStatusJob.Builder();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder implements JobBuilder<UpdateStatusJob> {

        @JsonCreator
        private static Builder create() {
            return UpdateStatusJob.builder();
        }

        @Override
        public abstract Builder id(String id);

        public abstract Builder statusUpdate(JobStatus statusUpdate);

        @Override
        public abstract UpdateStatusJob build();

    }

    @Override
    @JsonIgnore
    public abstract Builder toBuilder();

    @Override
    @JsonProperty
    @Nullable
    public abstract String id();

    @JsonProperty
    public abstract JobStatus statusUpdate();

    @Override
    public String toString() {
        return String.format("send status update: %s", statusUpdate());
    }

    @JsonIgnore
    @Override
    public final Class<Processor.ProcessorFactory> processorFactory() {
        return Processor.ProcessorFactory.class;
    }

    public interface Processor extends JobProcessor<UpdateStatusJob> {
        interface ProcessorFactory extends JobProcessor.Factory<UpdateStatusJob> {
            @Override
            Processor create(UpdateStatusJob job, JobControl jobControl);
        }
    }
}
