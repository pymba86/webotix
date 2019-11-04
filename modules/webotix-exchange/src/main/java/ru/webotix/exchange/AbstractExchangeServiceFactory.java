package ru.webotix.exchange;

import org.apache.commons.lang3.StringUtils;
import ru.webotix.common.api.WebotixConfiguration;
import ru.webotix.exchange.api.ExchangeConfiguration;

import java.util.Map;

public abstract class AbstractExchangeServiceFactory<T> {

    private final WebotixConfiguration configuration;

    AbstractExchangeServiceFactory(WebotixConfiguration configuration) {
        this.configuration = configuration;
    }

    public T getForExchange(String exchange) {
        Map<String, ExchangeConfiguration> exchangesConfig = configuration.getExchanges();

        if (exchangesConfig == null) {
            return getPaperFactory().getForExchange(exchange);
        }

        final ExchangeConfiguration exchangeConfiguration = configuration.getExchanges().get(exchange);

        if (exchangeConfiguration == null || StringUtils.isEmpty(exchangeConfiguration.getApiKey())) {
            return getPaperFactory().getForExchange(exchange);
        }

        return getRealFactory().getForExchange(exchange);
    }

    protected abstract ExchangeServiceFactory<T> getRealFactory();

    protected abstract ExchangeServiceFactory<T> getPaperFactory();
}
