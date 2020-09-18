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
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;
import ru.webotix.exchange.api.ExchangeService;

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

    PaperAccountService(Set<Currency> currencies) {
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

    private AtomicReference<Balance> defaultBalance(Currency currency) {
        return new AtomicReference<>(new Balance(currency, INITIAL_BALANCE));
    }

    void reserve(LimitOrder order) {
        reserve(order, false);
    }

    void releaseBalances(LimitOrder order) {
        reserve(order, true);
    }

    private void reserve(LimitOrder order, boolean negate) {
        switch (order.getType()) {
            case ASK:
                BigDecimal askAmount =
                        negate ? order.getOriginalAmount().negate() : order.getOriginalAmount();
                balances
                        .computeIfAbsent(order.getCurrencyPair().base, this::defaultBalance)
                        .updateAndGet(
                                b -> {
                                    if (b.getAvailable().compareTo(askAmount) < 0) {
                                        throw new ExchangeException(
                                                "Insufficient balance: "
                                                        + askAmount.toPlainString()
                                                        + order.getCurrencyPair().base
                                                        + " required but only "
                                                        + b.getAvailable()
                                                        + " available");
                                    }
                                    return Balance.Builder.from(b)
                                            .available(b.getAvailable().subtract(askAmount))
                                            .frozen(b.getFrozen().add(askAmount))
                                            .build();
                                });
                break;
            case BID:
                BigDecimal bid = order.getOriginalAmount().multiply(order.getLimitPrice());
                BigDecimal bidAmount = negate ? bid.negate() : bid;
                balances
                        .computeIfAbsent(order.getCurrencyPair().counter, this::defaultBalance)
                        .updateAndGet(
                                b -> {
                                    if (b.getAvailable().compareTo(bidAmount) < 0) {
                                        throw new ExchangeException(
                                                "Insufficient balance: "
                                                        + bidAmount.toPlainString()
                                                        + order.getCurrencyPair().counter
                                                        + " required but only "
                                                        + b.getAvailable()
                                                        + " available");
                                    }
                                    return Balance.Builder.from(b)
                                            .available(b.getAvailable().subtract(bidAmount))
                                            .frozen(b.getFrozen().add(bidAmount))
                                            .build();
                                });
                break;
            default:
                throw new NotAvailableFromExchangeException(
                        "Order type " + order.getType() + " not supported");
        }
    }

    public void fillLimitOrder(LimitOrder order) {

        BigDecimal originalCounterAmount = order.getCumulativeAmount().multiply(order.getLimitPrice());
        BigDecimal counterAmount = order.getCumulativeAmount().multiply(order.getAveragePrice());
        switch (order.getType()) {
            case ASK:
                balances
                        .computeIfAbsent(order.getCurrencyPair().base, this::defaultBalance)
                        .updateAndGet(
                                b ->
                                        Balance.Builder.from(b)
                                                .total(b.getTotal().subtract(order.getCumulativeAmount()))
                                                .frozen(b.getFrozen().subtract(order.getCumulativeAmount()))
                                                .build());
                balances
                        .computeIfAbsent(order.getCurrencyPair().counter, this::defaultBalance)
                        .updateAndGet(
                                b ->
                                        Balance.Builder.from(b)
                                                .total(b.getTotal().add(counterAmount))
                                                .available(b.getAvailable().add(counterAmount))
                                                .build());
                break;
            case BID:
                balances
                        .computeIfAbsent(order.getCurrencyPair().base, this::defaultBalance)
                        .updateAndGet(
                                b ->
                                        Balance.Builder.from(b)
                                                .total(b.getTotal().add(order.getCumulativeAmount()))
                                                .available(b.getAvailable().add(order.getCumulativeAmount()))
                                                .build());
                balances
                        .computeIfAbsent(order.getCurrencyPair().counter, this::defaultBalance)
                        .updateAndGet(
                                b ->
                                        Balance.Builder.from(b)
                                                .available(
                                                        b.getAvailable().add(originalCounterAmount)
                                                                .subtract(counterAmount))
                                                .frozen(b.getFrozen().subtract(counterAmount))
                                                .total(b.getTotal().subtract(counterAmount))
                                                .build());
                break;
            default:
                throw new NotAvailableFromExchangeException(
                        "Order type " + order.getType() + " not supported");
        }
    }

    @Singleton
    public static class Factory implements AccountServiceFactory {

        private final ExchangeService exchangeService;

        private final LoadingCache<String, PaperAccountService> services = CacheBuilder
                .newBuilder()
                .initialCapacity(1000)
                .build(new CacheLoader<String, PaperAccountService>() {
                    @Override
                    public PaperAccountService load(String exchange) {
                        return new PaperAccountService(exchangeService.get(exchange)
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
