package ru.webotix.exchange;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.knowm.xchange.dto.marketdata.Ticker;

public class ExchangeAccessHealthCheck extends HealthCheck {

    private final ExchangeResource exchangeResource;

    @Inject
    public ExchangeAccessHealthCheck(ExchangeResource exchangeResource) {
        this.exchangeResource = exchangeResource;
    }

    @Override
    protected Result check() {
        ResultBuilder result = Result.builder().healthy();

        exchangeResource.list()
                .forEach(
                        exchange -> {
                            try {
                                ExchangePair pair = Iterables.getFirst(
                                        exchangeResource.pairs(exchange.getCode()), null);

                                if (pair == null) {
                                    result.withDetail(exchange.getCode(), "No pairs");
                                    result.unhealthy();
                                } else {
                                    Ticker ticker = exchangeResource
                                            .ticker(exchange.getCode(), pair.counter, pair.base);

                                    if (ticker.getLast() == null) {
                                        result.withDetail(
                                                exchange + "/" + pair.counter + "/", "Nothing returned"
                                        );
                                        result.unhealthy();
                                    } else {
                                        result.withDetail(
                                                exchange + "/" + pair.counter + "/" + pair.base,
                                                "Last price: " + ticker.getLast()
                                        );
                                    }
                                }
                            } catch (Exception e) {
                                result.withDetail(exchange.getCode(), "Exception: " + e.getMessage());
                                result.unhealthy(e);
                            }
                        }
                );

        return result.build();
    }
}
