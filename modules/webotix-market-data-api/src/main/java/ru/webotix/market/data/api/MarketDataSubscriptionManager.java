package ru.webotix.market.data.api;

import io.reactivex.Flowable;

public interface MarketDataSubscriptionManager extends SubscriptionController {

    Flowable<TickerEvent> getTickers();

    Flowable<BalanceEvent> getBalances();
}
