package ru.webotix.exchange;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.service.account.AccountService;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaperAccountService implements AccountService {

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal(1000);

    private final ConcurrentMap<Currency, AtomicReference<Balance>> balances;
    private final String exchange;

    PaperAccountService(String exchange, Set<Currency> currencies) {
        this.exchange = exchange;
        this.balances = new ConcurrentHashMap<>(currencies.stream().collect(Collectors.toMap(
                Function.identity(),
                k -> new AtomicReference<>(new Balance(k, INITIAL_BALANCE))
        )));
    }

    public static class Factory implements AccountServiceFactory {

        private final ExchangeService exchangeService;

        private final LoadingCache<String, PaperAccountService> services = CacheBuilder
                .newBuilder()
                .initialCapacity(1000)
                .build(new CacheLoader<String, PaperAccountService>() {
                    @Override
                    public PaperAccountService load(String exchange) throws Exception {
                        return new PaperAccountService(exchange, exchangeService.get(exchange)
                                .getExchangeMetaData()
                                .getCurrencies()
                                .keySet());
                    }
                });

        Factory(ExchangeService exchangeService) {
            this.exchangeService = exchangeService;
        }

        @Override
        public AccountService getForExchange(String exchange) {
           return services.getUnchecked(exchange);
        }
    }
}
