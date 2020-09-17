package ru.webotix.market.data.api;

/**
 * Типы данных с помошью которых происходит взаимодействие
 * системы с биржами
 */
public enum MarketDataType {
    Ticker,
    OrderBook,
    Trades,
    OpenOrders,
    Order,
    UserTrade,
    Balance
}
