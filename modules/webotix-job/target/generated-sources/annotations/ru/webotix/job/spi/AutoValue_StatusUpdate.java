package ru.webotix.job.spi;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;
import javax.annotation.Nullable;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_StatusUpdate extends StatusUpdate {

  private final String requestId;

  private final Status status;

  private final Object payload;

  AutoValue_StatusUpdate(
      String requestId,
      Status status,
      @Nullable Object payload) {
    if (requestId == null) {
      throw new NullPointerException("Null requestId");
    }
    this.requestId = requestId;
    if (status == null) {
      throw new NullPointerException("Null status");
    }
    this.status = status;
    this.payload = payload;
  }

  @JsonProperty
  @Override
  public String requestId() {
    return requestId;
  }

  @JsonProperty
  @Override
  public Status status() {
    return status;
  }

  @Nullable
  @JsonProperty
  @Override
  public Object payload() {
    return payload;
  }

  @Override
  public String toString() {
    return "StatusUpdate{"
         + "requestId=" + requestId + ", "
         + "status=" + status + ", "
         + "payload=" + payload
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof StatusUpdate) {
      StatusUpdate that = (StatusUpdate) o;
      return this.requestId.equals(that.requestId())
          && this.status.equals(that.status())
          && (this.payload == null ? that.payload() == null : this.payload.equals(that.payload()));
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= requestId.hashCode();
    h$ *= 1000003;
    h$ ^= status.hashCode();
    h$ *= 1000003;
    h$ ^= (payload == null) ? 0 : payload.hashCode();
    return h$;
  }

}
