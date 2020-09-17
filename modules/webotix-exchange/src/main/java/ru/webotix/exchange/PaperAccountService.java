package ru.webotix.exchange;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;
import ru.webotix.exchange.api.ExchangeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
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

    @Override
    public AccountInfo getAccountInfo() {

        Collection<Balance> balanceList = Collections2.transform(
                balances.values(), AtomicReference::get);

        return new AccountInfo(Wallet.Builder.from(balanceList).build());
    }

    @Override
    public String withdrawFunds(Currency currency, BigDecimal amount, String address) {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public String withdrawFunds(WithdrawFundsParams params) {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public String requestDepositAddress(Currency currency, String... args) {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public TradeHistoryParams createFundingHistoryParams() {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public List<FundingRecord> getFundingHistory(TradeHistoryParams params) {
        throw new NotAvailableFromExchangeException();
    }

    @Singleton
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

        @Inject
        Factory(ExchangeService exchangeService) {
            this.exchangeService = exchangeService;
        }

        @Override
        public PaperAccountService getForExchange(String exchange) {
            return services.getUnchecked(exchange);
        }
    }
}
