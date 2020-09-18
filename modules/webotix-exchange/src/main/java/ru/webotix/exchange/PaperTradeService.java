package ru.webotix.exchange;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderByIdParams;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.exchange.api.ExchangeEventRegistry.ExchangeEventSubscription;
import ru.webotix.market.data.api.MarketDataSubscription;
import ru.webotix.market.data.api.MarketDataType;
import ru.webotix.market.data.api.TickerEvent;
import ru.webotix.market.data.api.TickerSpec;
import ru.webotix.utils.SafelyDispose;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PaperTradeService implements TradeService {

    private final AtomicLong orderCounter = new AtomicLong();

    private final AtomicLong tradeCounter = new AtomicLong();

    private final List<UserTrade> tradeHistory = new CopyOnWriteArrayList<>();

    private final ConcurrentMap<Long, LimitOrder> openOrders = new ConcurrentHashMap<>();

    private final ConcurrentMap<Long, Date> placedDates = new ConcurrentHashMap<>();

    private final PaperAccountService paperAccountService;

    private ExchangeEventSubscription subscription;

    private Disposable disposable;

    private final String exchange;

    private final Random random = new Random();

    private PaperTradeService(String exchange,
                              ExchangeEventRegistry exchangeEventRegistry,
                              PaperAccountService paperAccountService) {
        this.paperAccountService = paperAccountService;
        this.subscription = exchangeEventRegistry.subscribe();
        this.exchange = exchange;
    }

    @Override
    public OpenOrders getOpenOrders() {
        return new OpenOrders(openOrders.values()
                .stream()
                .filter(this::isOpen)
                .collect(Collectors.toList())
        );
    }

    @Override
    public String placeStopOrder(StopOrder stopOrder) {
        throw new NotAvailableFromExchangeException("Stop orders not supported yet for paper trading.");
    }

    @Override
    public boolean cancelOrder(String orderId) {

        final Long id = Long.valueOf(orderId);

        final LimitOrder limitOrder = openOrders.get(id);

        if (limitOrder == null) {
            throw new ExchangeException("No such order: " + orderId);
        }

        if (!isOpen(limitOrder)) {
            return false;
        }

        limitOrder.setOrderStatus(Order.OrderStatus.CANCELED);

        paperAccountService.releaseBalances(limitOrder);

        updateTickerRegistry();

        return true;
    }

    @Override
    public TradeHistoryParamCurrencyPair createTradeHistoryParams() {
        return new TradeHistoryParamCurrencyPair() {

            private CurrencyPair pair;

            @Override
            public void setCurrencyPair(CurrencyPair pair) {
                this.pair = pair;
            }

            @Override
            public CurrencyPair getCurrencyPair() {
                return pair;
            }
        };
    }

    @Override
    public boolean cancelOrder(CancelOrderParams orderParams) {
        if (!(orderParams instanceof CancelOrderByIdParams)) {
            throw new ExchangeException("You need to provide to order id to cancel an order");
        }

        CancelOrderByIdParams paramId = (CancelOrderByIdParams) orderParams;

        return cancelOrder(paramId.getOrderId());
    }

    @Override
    public OpenOrders getOpenOrders(OpenOrdersParams params) {
        if (!(params instanceof OpenOrdersParamCurrencyPair)) {
            throw new ExchangeException("Currency pair required to list open orders");
        }

        OpenOrders all = getOpenOrders();

        CurrencyPair pair = ((OpenOrdersParamCurrencyPair) params).getCurrencyPair();

        return new OpenOrders(
                all.getOpenOrders().stream()
                        .filter(o -> o.getCurrencyPair().equals(pair))
                        .collect(Collectors.toList()),
                all.getHiddenOrders().stream()
                        .filter(o -> o.getCurrencyPair().equals(pair))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public OpenOrdersParamCurrencyPair createOpenOrdersParams() {
        return new DefaultOpenOrdersParamCurrencyPair();
    }

    @Override
    public void verifyOrder(LimitOrder limitOrder) {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public void verifyOrder(MarketOrder marketOrder) {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public String placeMarketOrder(MarketOrder marketOrder) {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public Collection<Order> getOrder(String... orderIds) {
        final Set<Long> ids =
                Arrays.stream(orderIds).map(Long::valueOf).collect(Collectors.toSet());
        return openOrders.entrySet().stream()
                .filter(e -> ids.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public String placeLimitOrder(LimitOrder limitOrder) {

        randomDelay();

        final long id = orderCounter.incrementAndGet();
        String strId = String.valueOf(id);

        LimitOrder newOrder = new LimitOrder(
                limitOrder.getType(),
                limitOrder.getOriginalAmount(),
                limitOrder.getCurrencyPair(),
                strId,
                limitOrder.getTimestamp() == null ? new Date() : limitOrder.getTimestamp(),
                limitOrder.getLimitPrice(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Order.OrderStatus.NEW
        );

        paperAccountService.reserve(limitOrder);

        openOrders.put(id, newOrder);

        placedDates.put(id, new Date());

        updateTickerRegistry();

        return strId;
    }

    @Override
    public UserTrades getTradeHistory(TradeHistoryParams params) {
        if (!(params instanceof TradeHistoryParamCurrencyPair)) {
            throw new ExchangeException("Requires currency pair");
        }

        TradeHistoryParamCurrencyPair currencyPairParams = (TradeHistoryParamCurrencyPair) params;

        return new UserTrades(
                tradeHistory.stream()
                        .filter(t -> t.getCurrencyPair().equals(currencyPairParams.getCurrencyPair()))
                        .collect(Collectors.toList()),
                Trades.TradeSortType.SortByTimestamp
        );
    }

    private void updateTickerRegistry() {

        SafelyDispose.of(disposable);

        subscription = subscription.replace(
                openOrders.values().stream().map(o -> MarketDataSubscription.create(
                        TickerSpec.builder()
                                .exchange(exchange)
                                .counter(o.getCurrencyPair().counter.getCurrencyCode())
                                .base(o.getCurrencyPair().base.getCurrencyCode())
                                .build(),
                        MarketDataType.Ticker
                )).collect(Collectors.toSet())
        );

        disposable = subscription.getTickers()
                .subscribe(this::updateAgainstMarket);
    }

    private synchronized void updateAgainstMarket(TickerEvent tickerEvent) {
        Set<LimitOrder> filledOrders = new HashSet<>();

        openOrders.values().stream()
                .filter(order -> order.getCurrencyPair().counter
                        .getCurrencyCode().equals(tickerEvent.spec().counter())
                        && order.getCurrencyPair().base.getCurrencyCode().equals(tickerEvent.spec().base())
                )
                .forEach(order -> {
                    if (fillOder(tickerEvent.ticker(), order)) {
                        paperAccountService.fillLimitOrder(order);
                        filledOrders.add(order);
                    }
                });

        filledOrders.stream()
                .map(order -> Long.valueOf(order.getId()))
                .forEach(order -> {
                    openOrders.remove(order);
                    placedDates.remove(order);
                });
    }

    private boolean fillOder(Ticker ticker, LimitOrder order) {
        switch (order.getType()) {
            case ASK:
                if (ticker.getBid().compareTo(order.getLimitPrice()) >= 0) {
                    order.setCumulativeAmount(order.getOriginalAmount());
                    order.setAveragePrice(ticker.getBid());
                    order.setOrderStatus(Order.OrderStatus.FILLED);
                    addTradeHistory(order, ticker.getBid());
                    return true;
                }
                break;
            case BID:
                if (ticker.getAsk().compareTo(order.getLimitPrice()) <= 0) {
                    order.setCumulativeAmount(order.getOriginalAmount());
                    order.setAveragePrice(ticker.getAsk());
                    order.setOrderStatus(Order.OrderStatus.FILLED);
                    addTradeHistory(order, ticker.getAsk());
                    return true;
                }
                break;
            default:
                throw new NotAvailableFromExchangeException(
                        "Order type " + order.getType() + "not supported");
        }
        return false;
    }

    private void addTradeHistory(LimitOrder order, BigDecimal price) {
        tradeHistory.add(
                new UserTrade.Builder()
                        .type(order.getType())
                        .originalAmount(order.getOriginalAmount())
                        .currencyPair(order.getCurrencyPair())
                        .price(price)
                        .timestamp(new Date())
                        .id(Long.toString(tradeCounter.incrementAndGet()))
                        .orderId(order.getId())
                        .feeAmount(BigDecimal.ZERO)
                        .feeCurrency(order.getCurrencyPair().base)
                        .build()
        );
    }

    // Имитирует реальность задержки
    private void randomDelay() {
        try {
            Thread.sleep(random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private boolean isOpen(LimitOrder limitOrder) {
        return limitOrder.getStatus() != Order.OrderStatus.CANCELED
                && limitOrder.getStatus() != Order.OrderStatus.FILLED;
    }

    @Singleton
    public static class Factory implements TradeServiceFactory {

        private static final Logger log = LoggerFactory.getLogger(Factory.class);

        private final ExchangeEventRegistry exchangeEventRegistry;
        private final PaperAccountService.Factory accountServiceFactory;

        private final LoadingCache<String, TradeService> services =
                CacheBuilder.newBuilder()
                        .initialCapacity(1000)
                        .build(
                                new CacheLoader<String, TradeService>() {
                                    @Override
                                    public TradeService load(String exchange) {
                                        log.debug(
                                                "No API connection details for {}. Using paper trading.", exchange);
                                        return new PaperTradeService(
                                                exchange,
                                                exchangeEventRegistry,
                                                accountServiceFactory.getForExchange(exchange));
                                    }
                                });

        @Inject
        Factory(
                ExchangeEventRegistry exchangeEventRegistry,
                PaperAccountService.Factory accountServiceFactory) {
            this.exchangeEventRegistry = exchangeEventRegistry;
            this.accountServiceFactory = accountServiceFactory;
        }

        @Override
        public TradeService getForExchange(String exchange) {
            return services.getUnchecked(exchange);
        }
    }
}
