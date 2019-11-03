package ru.webotix.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import javax.annotation.Generated;
import ru.webotix.exchange.info.TickerSpec;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_WebSocketInMessage extends WebSocketInMessage {

  private final WebSocketCommandMessage command;

  private final Collection<TickerSpec> tickers;

  AutoValue_WebSocketInMessage(
      WebSocketCommandMessage command,
      Collection<TickerSpec> tickers) {
    if (command == null) {
      throw new NullPointerException("Null command");
    }
    this.command = command;
    if (tickers == null) {
      throw new NullPointerException("Null tickers");
    }
    this.tickers = tickers;
  }

  @JsonProperty
  @Override
  WebSocketCommandMessage command() {
    return command;
  }

  @JsonProperty
  @Override
  Collection<TickerSpec> tickers() {
    return tickers;
  }

  @Override
  public String toString() {
    return "WebSocketInMessage{"
         + "command=" + command + ", "
         + "tickers=" + tickers
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof WebSocketInMessage) {
      WebSocketInMessage that = (WebSocketInMessage) o;
      return this.command.equals(that.command())
          && this.tickers.equals(that.tickers());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= command.hashCode();
    h$ *= 1000003;
    h$ ^= tickers.hashCode();
    return h$;
  }

}
