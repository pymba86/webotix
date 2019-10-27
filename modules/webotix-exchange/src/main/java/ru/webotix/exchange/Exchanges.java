package ru.webotix.exchange;

import com.google.common.base.Suppliers;
import info.bitrich.xchangestream.core.StreamingExchange;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.reflections.Reflections;

public final class Exchanges {

    private Exchanges() {
    }

    public static final String BINANCE = "binance";
    public static final String GDAX = "gdax";
    public static final String KUCOIN = "kucoin";
    public static final String BITTREX = "bittrex";
    public static final String BITFINEX = "bitfinex";
    public static final String CRYPTOPIA = "cryptopia";
    public static final String BITMEX = "bitmex";
    public static final String KRAKEN = "kraken";
    public static final String SIMULATED = "simulated";

    public static final Supplier<List<Class<? extends Exchange>>> EXCHANGE_TYPES = Suppliers.memoize(
            () -> new Reflections("org.knowm.exchange")
                    .getSubTypesOf(Exchange.class)
                    .stream()
                    .filter(c -> !c.equals(BaseExchange.class))
                    .collect(Collectors.toList())
    );

    static final Supplier<List<Class<? extends StreamingExchange>>> STREAMING_EXHANGE_TYPES = Suppliers.memoize(
            () -> new ArrayList<>(new Reflections("info.bitrich.xchangestream")
                    .getSubTypesOf(StreamingExchange.class)
            )
    );

    /**
     * Получить полное название биржы по коду
     *
     * @param exchange код биржы
     * @return полное название биржы
     */
    public static String name(String exchange) {
        switch (exchange) {
            case GDAX:
                return "Coinbase Pro";
            case SIMULATED:
                return "Simulator";
            case BITMEX:
            case KRAKEN:
                return StringUtils.capitalize(exchange) + " (beta)";
            default:
                return StringUtils.capitalize(exchange);
        }
    }


}
