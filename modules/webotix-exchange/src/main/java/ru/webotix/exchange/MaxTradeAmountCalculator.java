package ru.webotix.exchange;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.exchange.api.ExchangeService;
import ru.webotix.market.data.api.MarketDataSubscription;
import ru.webotix.market.data.api.MarketDataType;
import ru.webotix.market.data.api.TickerSpec;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.DOWN;

public class MaxTradeAmountCalculator {

    private final ExchangeEventRegistry exchangeEventRegistry;

    private final TickerSpec tickerSpec;
    private final BigDecimal amountStepSize;
    private final Integer priceScale;

    @Inject
    MaxTradeAmountCalculator(
            @Assisted final TickerSpec tickerSpec,
            final ExchangeEventRegistry exchangeEventRegistry,
            final ExchangeService exchangeService) {
        this.tickerSpec = tickerSpec;
        this.exchangeEventRegistry = exchangeEventRegistry;

        CurrencyPairMetaData currencyPairMetaData =
                exchangeService
                        .get(tickerSpec.exchange())
                        .getExchangeMetaData()
                        .getCurrencyPairs()
                        .get(tickerSpec.currencyPair());

        this.amountStepSize = currencyPairMetaData.getAmountStepSize();
        this.priceScale = MoreObjects.firstNonNull(currencyPairMetaData.getPriceScale(), 0);
    }

    public BigDecimal adjustAmountForLotSize(BigDecimal amount) {
        if (amountStepSize != null) {
            BigDecimal remainder = amount.remainder(amountStepSize);
            if (remainder.compareTo(ZERO) != 0) {
                return amount.subtract(remainder);
            }
        }
        return amount;
    }

    public BigDecimal validOrderAmount(BigDecimal limitPrice, Order.OrderType direction) {
        BigDecimal result;
        try (ExchangeEventRegistry.ExchangeEventSubscription subscription =
                     exchangeEventRegistry.subscribe(MarketDataSubscription.create(tickerSpec, MarketDataType.Balance))) {
            if (direction.equals(Order.OrderType.ASK)) {
                result = blockingBalance(subscription, tickerSpec.base()).setScale(priceScale, DOWN);
            } else {
                BigDecimal available = blockingBalance(subscription, tickerSpec.counter());
                result = available.divide(limitPrice, priceScale, DOWN);
            }
        }
        return adjustAmountForLotSize(result);
    }

    private BigDecimal blockingBalance(ExchangeEventRegistry.ExchangeEventSubscription subscription,
                                       String currency) {
        return subscription
                .getBalances()
                .filter(b -> b.balance().getCurrency().getCurrencyCode().equals(currency))
                .blockingFirst()
                .balance()
                .getAvailable();
    }

    public static class Factory {

        private final ExchangeEventRegistry exchangeEventRegistry;
        private final ExchangeService exchangeService;

        @Inject
        public Factory(ExchangeEventRegistry exchangeEventRegistry, ExchangeService exchangeService) {
            this.exchangeEventRegistry = exchangeEventRegistry;
            this.exchangeService = exchangeService;
        }

        public MaxTradeAmountCalculator create(TickerSpec tickerSpec) {
            return new MaxTradeAmountCalculator(tickerSpec, exchangeEventRegistry, exchangeService);
        }
    }
}
