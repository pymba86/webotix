package ru.webotix.exchange;

public interface ExchangeServiceFactory<T> {

    T getForExchange(String exchange);
}
