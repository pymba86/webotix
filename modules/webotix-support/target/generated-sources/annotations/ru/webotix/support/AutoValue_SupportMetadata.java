package ru.webotix.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_SupportMetadata extends SupportMetadata {

  private final String version;

  AutoValue_SupportMetadata(
      String version) {
    if (version == null) {
      throw new NullPointerException("Null version");
    }
    this.version = version;
  }

  @JsonProperty
  @Override
  public String version() {
    return version;
  }

  @Override
  public String toString() {
    return "SupportMetadata{"
         + "version=" + version
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SupportMetadata) {
      SupportMetadata that = (SupportMetadata) o;
      return this.version.equals(that.version());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= version.hashCode();
    return h$;
  }

}
