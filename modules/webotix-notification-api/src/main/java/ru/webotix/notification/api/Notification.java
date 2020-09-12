package ru.webotix.notification.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

/**
 * Уведомление наступает, когда пользователь должен быть предупрежден о чем-либо.
 */
@AutoValue
@JsonDeserialize
public abstract class Notification {

    public static Notification create(@JsonProperty("message") String message,
                                      @JsonProperty("level") NotificationLevel level) {
        return new AutoValue_Notification(message, level);
    }

    @JsonProperty
    public abstract String message();

    @JsonProperty
    public abstract NotificationLevel level();
}
