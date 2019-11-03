package ru.webotix.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_WebSocketOutMessage extends WebSocketOutMessage {

  private final WebSocketNatureMessage nature;

  private final Object data;

  AutoValue_WebSocketOutMessage(
      WebSocketNatureMessage nature,
      Object data) {
    if (nature == null) {
      throw new NullPointerException("Null nature");
    }
    this.nature = nature;
    if (data == null) {
      throw new NullPointerException("Null data");
    }
    this.data = data;
  }

  @JsonProperty
  @Override
  WebSocketNatureMessage nature() {
    return nature;
  }

  @JsonProperty
  @Override
  Object data() {
    return data;
  }

  @Override
  public String toString() {
    return "WebSocketOutMessage{"
         + "nature=" + nature + ", "
         + "data=" + data
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof WebSocketOutMessage) {
      WebSocketOutMessage that = (WebSocketOutMessage) o;
      return this.nature.equals(that.nature())
          && this.data.equals(that.data());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= nature.hashCode();
    h$ *= 1000003;
    h$ ^= data.hashCode();
    return h$;
  }

}
