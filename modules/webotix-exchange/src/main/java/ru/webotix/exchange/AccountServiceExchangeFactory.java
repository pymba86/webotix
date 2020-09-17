package ru.webotix.exchange;

import com.google.inject.Inject;
import org.knowm.xchange.service.account.AccountService;
import ru.webotix.exchange.PaperAccountService.Factory;
import ru.webotix.exchange.api.ExchangeService;

import java.util.Map;

public class AccountServiceExchangeFactory extends AbstractExchangeServiceFactory<AccountService>
        implements AccountServiceFactory {

    private final ExchangeService exchangeService;
    private final Factory paperAccountServiceFactory;

    @Inject
    public AccountServiceExchangeFactory(Map<String, ExchangeConfiguration> configuration,
                                         ExchangeService exchangeService,
                                         Factory paperAccountServiceFactory) {
        super(configuration);
        this.exchangeService = exchangeService;
        this.paperAccountServiceFactory = paperAccountServiceFactory;
    }

    @Override
    protected ExchangeServiceFactory<AccountService> getRealFactory() {
        return exchange -> exchangeService.get(exchange)
                .getAccountService();
    }

    @Override
    protected ExchangeServiceFactory<AccountService> getPaperFactory() {
        return paperAccountServiceFactory;
    }
}
