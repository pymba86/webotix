package ru.webotix.exchange;

import org.knowm.xchange.service.account.AccountService;

public interface AccountServiceFactory extends ExchangeServiceFactory<AccountService> {

    @Override
    AccountService getForExchange(String exchange);
}
