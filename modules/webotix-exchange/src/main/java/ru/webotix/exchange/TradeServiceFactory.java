package ru.webotix.exchange;

import org.knowm.xchange.service.trade.TradeService;

public interface TradeServiceFactory extends ExchangeServiceFactory<TradeService> {

    @Override
    TradeService getForExchange(String exchange);
}
