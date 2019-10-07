package ru.webotix.exchange;

public interface ExchangeServiceFactory<T> {

    public T getForExchange(String exchange);
}
