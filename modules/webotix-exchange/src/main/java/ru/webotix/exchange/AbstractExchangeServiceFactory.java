package ru.webotix.exchange;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public abstract class AbstractExchangeServiceFactory<T> {

    private final Map<String, ExchangeConfiguration> configuration;

    AbstractExchangeServiceFactory(Map<String, ExchangeConfiguration> configuration) {
        this.configuration = configuration;
    }

    public T getForExchange(String exchange) {

        if (configuration == null) {
            return getPaperFactory().getForExchange(exchange);
        }

        final ExchangeConfiguration exchangeConfiguration = configuration.get(exchange);

        if (exchangeConfiguration == null || StringUtils.isEmpty(exchangeConfiguration.getApiKey())) {
            return getPaperFactory().getForExchange(exchange);
        }

        return getRealFactory().getForExchange(exchange);
    }

    protected abstract ExchangeServiceFactory<T> getRealFactory();

    protected abstract ExchangeServiceFactory<T> getPaperFactory();
}
