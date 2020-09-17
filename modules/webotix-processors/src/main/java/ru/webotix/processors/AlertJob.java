package ru.webotix.processors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.auto.value.AutoValue;
import ru.webotix.job.api.*;
import ru.webotix.notification.api.Notification;

import javax.annotation.Nullable;

@AutoValue
@JsonDeserialize(builder = AlertJob.Builder.class)
public abstract class AlertJob implements Job {

    public static Builder builder() {
        return new AutoValue_AlertJob.Builder();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "")
    public abstract static class Builder implements JobBuilder<AlertJob> {

        @JsonCreator
        private static Builder create() {
            return AlertJob.builder();
        }

        @Override
        public abstract Builder id(String id);

        public abstract Builder notification(Notification notification);

        @Override
        public abstract AlertJob build();
    }

    @Override
    @JsonIgnore
    public abstract Builder toBuilder();

    @Override
    @JsonProperty
    @Nullable
    public abstract String id();

    @JsonProperty
    public abstract Notification notification();

    @Override
    public String toString() {
        return String.format(
                "send %s '%s'", notification()
                        .level().toString().toLowerCase(),
                notification().message());
    }

    public final Class<Processor.ProcessorFactory> processorFactory() {
        return Processor.ProcessorFactory.class;
    }

    public interface Processor extends JobProcessor<AlertJob> {
        interface ProcessorFactory extends JobProcessor.Factory<AlertJob> {

            @Override
            Processor create(AlertJob job, JobControl jobControl);
        }
    }
}
