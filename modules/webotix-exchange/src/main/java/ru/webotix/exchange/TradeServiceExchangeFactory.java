package ru.webotix.exchange;

import com.google.inject.Inject;
import org.knowm.xchange.service.trade.TradeService;
import ru.webotix.exchange.api.ExchangeService;

import java.util.Map;

public class TradeServiceExchangeFactory extends AbstractExchangeServiceFactory<TradeService>
        implements TradeServiceFactory {

    private final ExchangeService exchangeService;

    @Inject
    public TradeServiceExchangeFactory(Map<String, ExchangeConfiguration> configuration,
                                       ExchangeService exchangeService) {
        super(configuration);
        this.exchangeService = exchangeService;
    }

    @Override
    protected ExchangeServiceFactory<TradeService> getRealFactory() {
        return exchange -> exchangeService.get(exchange)
                .getTradeService();
    }

    @Override
    protected ExchangeServiceFactory<TradeService> getPaperFactory() {
        return null;
    }
}
