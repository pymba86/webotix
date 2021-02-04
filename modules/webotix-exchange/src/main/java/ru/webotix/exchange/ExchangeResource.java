package ru.webotix.exchange;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.binance.service.BinanceCancelOrderParams;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.DefaultCancelOrderParamId;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.api.ExchangeService;
import ru.webotix.exchange.api.PairMetaData;
import ru.webotix.market.data.api.MarketDataSubscriptionManager;
import ru.webotix.market.data.api.TickerSpec;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ExchangeResource implements WebResource {

    private static final Logger log = LoggerFactory.getLogger(ExchangeResource.class);

    private final ExchangeService exchanges;
    private final TradeServiceFactory tradeServiceFactory;
    private final MaxTradeAmountCalculator.Factory calculatorFactory;
    private final Map<String, ExchangeConfiguration> configuration;
    private final MarketDataSubscriptionManager subscriptionManager;

    @Inject
    ExchangeResource(ExchangeService exchanges,
                     TradeServiceFactory tradeServiceFactory,
                     MarketDataSubscriptionManager subscriptionManager,
                     MaxTradeAmountCalculator.Factory calculatorFactory,
                     Map<String, ExchangeConfiguration> configuration) {
        this.exchanges = exchanges;
        this.configuration = configuration;
        this.tradeServiceFactory = tradeServiceFactory;
        this.calculatorFactory = calculatorFactory;
        this.subscriptionManager = subscriptionManager;
    }

    /**
     * Получить список доступных бирж
     *
     * @return Список бирж
     */
    @GET
    @Timed
    public Collection<ExchangeMeta> list() {
        return this.exchanges.getExchanges()
                .stream()
                .map(code -> {
                    ExchangeConfiguration exchangeConfig = configuration.get(code);
                    return new ExchangeMeta(
                            code,
                            Exchanges.name(code),
                            exchangeConfig != null
                                    && StringUtils.isNotBlank(exchangeConfig.getApiKey())
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Получить текущий тике для указанной биржи и пары
     *
     * @param exchange Биржда
     * @param counter  Контр валюта
     * @param base     Базовая валюта
     * @return Тикер
     * @throws IOException Если выбрасывается при обмене
     */
    @GET
    @Path("{exchange}/markets/{base}-{counter}/ticker")
    @Timed
    public Ticker ticker(@PathParam("exchange") String exchange,
                         @PathParam("counter") String counter,
                         @PathParam("base") String base) throws IOException {

        return exchanges.get(exchange)
                .getMarketDataService()
                .getTicker(new CurrencyPair(base, counter));
    }

    /**
     * Список валютных пар на указанной бирже
     *
     * @param exchange Биржа
     * @return Поддерживаемые валютные пары
     */
    @GET
    @Timed
    @Path("{exchange}/pairs")
    public Collection<ExchangePair> pairs(@PathParam("exchange") String exchange) {

        return exchanges.get(exchange)
                .getExchangeMetaData()
                .getCurrencyPairs()
                .keySet()
                .stream()
                .map(ExchangePair::new)
                .collect(Collectors.toSet());
    }

    @GET
    @Timed
    @Path("{exchange}/pairs/{base}-{counter}")
    public PairMetaData metadata(
            @PathParam("exchange") String exchangeName,
            @PathParam("counter") String counter,
            @PathParam("base") String base) {
        Exchange exchange = exchanges.get(exchangeName);
        CurrencyPair currencyPair = new CurrencyPair(base, counter);
        return new PairMetaData(exchange.getExchangeMetaData().getCurrencyPairs().get(currencyPair));
    }

    @GET
    @Path("{exchange}/orders")
    @Timed
    public Response orders(@PathParam("exchange") String exchange) throws IOException {
        try {
            return Response.ok()
                    .entity(tradeServiceFactory.getForExchange(exchange).getOpenOrders())
                    .build();
        } catch (NotAvailableFromExchangeException e) {
            return Response.status(503).build();
        }
    }

    @POST
    @Path("{exchange}/orders/calc")
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response calculateOrder(@PathParam("exchange") String exchange, OrderPrototype order) {
        if (!order.isLimit()) {
            return Response.status(400).entity(new ErrorResponse("Limit price required")).build();
        }
        TickerSpec tickerSpec =
                TickerSpec.builder()
                        .exchange(exchange)
                        .base(order.getBase())
                        .counter(order.getCounter())
                        .build();
        BigDecimal orderAmount =
                calculatorFactory
                        .create(tickerSpec)
                        .validOrderAmount(order.getLimitPrice(), order.getType());
        order.setAmount(orderAmount);
        return Response.ok().entity(order).build();
    }

    @POST
    @Path("{exchange}/orders")
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postOrder(@PathParam("exchange") String exchange, OrderPrototype order) {
        Optional<Response> error = checkOrderPreconditions(exchange, order);
        if (error.isPresent()) return error.get();

        try {
            TradeService tradeService = tradeServiceFactory.getForExchange(exchange);
            Order result =
                    order.isStop() ? postStopOrder(order, tradeService) : postLimitOrder(order, tradeService);
            postOrderToSubscribers(exchange, result);
            return Response.ok().entity(result).build();
        } catch (NotAvailableFromExchangeException e) {
            return Response.status(503)
                    .entity(new ErrorResponse("Order type not currently supported by exchange."))
                    .build();
        } catch (FundsExceededException e) {
            return Response.status(400).entity(new ErrorResponse(e.getMessage())).build();
        } catch (Exception e) {
            log.error("Failed to submit order: {}", order, e);
            return Response.status(500)
                    .entity(new ErrorResponse("Failed to submit order. " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("{exchange}/currencies/{currency}/orders")
    @Timed
    public Response orders(
            @PathParam("exchange") String exchangeCode, @PathParam("currency") String currency)
            throws IOException {

        try {

            log.info("Thorough orders search...");
            Exchange exchange = exchanges.get(exchangeCode);
            return Response.ok()
                    .entity(
                            exchange.getExchangeMetaData().getCurrencyPairs().keySet().stream()
                                    .filter(
                                            p ->
                                                    p.base.getCurrencyCode().equals(currency)
                                                            || p.counter.getCurrencyCode().equals(currency))
                                    .flatMap(
                                            p -> {
                                                try {
                                                    Thread.sleep(200);
                                                } catch (InterruptedException e) {
                                                    Thread.currentThread().interrupt();
                                                    throw new RuntimeException(e);
                                                }
                                                try {
                                                    return exchange
                                                            .getTradeService()
                                                            .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(p))
                                                            .getOpenOrders()
                                                            .stream();
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            })
                                    .collect(Collectors.toList()))
                    .build();
        } catch (NotAvailableFromExchangeException e) {
            return Response.status(503).build();
        }
    }

    @GET
    @Path("{exchange}/markets/{base}-{counter}/orders")
    @Timed
    public Response orders(
            @PathParam("exchange") String exchange,
            @PathParam("counter") String counter,
            @PathParam("base") String base)
            throws IOException {
        try {

            CurrencyPair currencyPair = new CurrencyPair(base, counter);

            OpenOrders unfiltered =
                    tradeServiceFactory
                            .getForExchange(exchange)
                            .getOpenOrders(new DefaultOpenOrdersParamCurrencyPair(currencyPair));

            OpenOrders filtered =
                    new OpenOrders(
                            unfiltered.getOpenOrders().stream()
                                    .filter(o -> o.getCurrencyPair().equals(currencyPair))
                                    .collect(Collectors.toList()),
                            unfiltered.getHiddenOrders().stream()
                                    .filter(o -> o.getCurrencyPair().equals(currencyPair))
                                    .collect(Collectors.toList()));

            return Response.ok().entity(filtered).build();

        } catch (NotAvailableFromExchangeException e) {
            return Response.status(503).build();
        }
    }

    @DELETE
    @Path("{exchange}/markets/{base}-{counter}/orders/{id}")
    @Timed
    public Response cancelOrder(
            @PathParam("exchange") String exchange,
            @PathParam("counter") String counter,
            @PathParam("base") String base,
            @PathParam("id") String id)
            throws IOException {
        try {
            // BinanceCancelOrderParams is the superset - pair and id. Should work with pretty much any
            // exchange,
            // except Bitmex
            // TODO PR to fix bitmex
            CancelOrderParams cancelOrderParams =
                    exchange.equals(Exchanges.BITMEX)
                            ? new DefaultCancelOrderParamId(id)
                            : new BinanceCancelOrderParams(new CurrencyPair(base, counter), id);
            Date now = new Date();
            if (!tradeServiceFactory.getForExchange(exchange).cancelOrder(cancelOrderParams)) {
                throw new IllegalStateException("Order could not be cancelled");
            }
            return Response.ok().entity(now).build();
        } catch (NotAvailableFromExchangeException e) {
            return Response.status(503).build();
        }
    }

    @GET
    @Path("{exchange}/orders/{id}")
    @Timed
    public Response order(@PathParam("exchange") String exchange, @PathParam("id") String id)
            throws IOException {
        try {
            return Response.ok()
                    .entity(tradeServiceFactory.getForExchange(exchange).getOrder(id))
                    .build();
        } catch (NotAvailableFromExchangeException e) {
            return Response.status(503).build();
        }
    }

    private void postOrderToSubscribers(String exchange, Order order) {
        CurrencyPair currencyPair = order.getCurrencyPair();
        subscriptionManager.postOrder(
                TickerSpec.builder()
                        .exchange(exchange)
                        .base(currencyPair.base.getCurrencyCode())
                        .counter(currencyPair.counter.getCurrencyCode())
                        .build(),
                order);
    }

    private LimitOrder postLimitOrder(OrderPrototype order, TradeService tradeService)
            throws IOException {
        LimitOrder limitOrder =
                new LimitOrder(
                        order.getType(),
                        order.getAmount(),
                        new CurrencyPair(order.getBase(), order.getCounter()),
                        null,
                        new Date(),
                        order.getLimitPrice());
        String id = tradeService.placeLimitOrder(limitOrder);
        return LimitOrder.Builder.from(limitOrder).id(id).orderStatus(Order.OrderStatus.NEW).build();
    }

    private StopOrder postStopOrder(OrderPrototype order, TradeService tradeService)
            throws IOException {
        StopOrder stopOrder =
                new StopOrder(
                        order.getType(),
                        order.getAmount(),
                        new CurrencyPair(order.getBase(), order.getCounter()),
                        null,
                        new Date(),
                        order.getStopPrice(),
                        order.getLimitPrice(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        Order.OrderStatus.PENDING_NEW);
        String id = tradeService.placeStopOrder(stopOrder);
        return StopOrder.Builder.from(stopOrder).id(id).orderStatus(Order.OrderStatus.NEW).build();
    }

    private Optional<Response> checkOrderPreconditions(String exchange, OrderPrototype order) {
        if (!order.isStop() && !order.isLimit())
            return Optional.of(
                    Response.status(400)
                            .entity(new ErrorResponse("Market orders not supported at the moment."))
                            .build());

        if (order.isStop()) {
            if (order.isLimit()) {
                if (exchange.equals(Exchanges.BITFINEX)) {
                    return Optional.of(
                            Response.status(400)
                                    .entity(
                                            new ErrorResponse(
                                                    "Stop limit orders not supported for Bitfinex at the moment."))
                                    .build());
                }
            } else {
                if (exchange.equals(Exchanges.BINANCE)) {
                    return Optional.of(
                            Response.status(400)
                                    .entity(
                                            new ErrorResponse(
                                                    "Stop market orders not supported for Binance at the moment. Specify a limit price."))
                                    .build());
                }
            }
        }

        return Optional.empty();
    }


    public static final class OrderPrototype {

        @JsonProperty private String counter;
        @JsonProperty private String base;
        @JsonProperty @Nullable
        private BigDecimal stopPrice;
        @JsonProperty @Nullable private BigDecimal limitPrice;
        @JsonProperty private Order.OrderType type;
        @JsonProperty private BigDecimal amount;

        public String getCounter() {
            return counter;
        }

        public String getBase() {
            return base;
        }

        public BigDecimal getStopPrice() {
            return stopPrice;
        }

        public BigDecimal getLimitPrice() {
            return limitPrice;
        }

        public Order.OrderType getType() {
            return type;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        @JsonIgnore
        boolean isStop() {
            return stopPrice != null;
        }

        @JsonIgnore
        boolean isLimit() {
            return limitPrice != null;
        }

        public void setCounter(String counter) {
            this.counter = counter;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public void setStopPrice(BigDecimal stopPrice) {
            this.stopPrice = stopPrice;
        }

        public void setLimitPrice(BigDecimal limitPrice) {
            this.limitPrice = limitPrice;
        }

        public void setType(Order.OrderType type) {
            this.type = type;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "OrderPrototype{"
                    + "counter='"
                    + counter
                    + '\''
                    + ", base='"
                    + base
                    + '\''
                    + ", stopPrice="
                    + stopPrice
                    + ", limitPrice="
                    + limitPrice
                    + ", type="
                    + type
                    + ", amount="
                    + amount
                    + '}';
        }
    }

    public static final class ErrorResponse {

        @JsonProperty
        private String message;

        ErrorResponse() {}

        ErrorResponse(String message) {
            this.message = message;
        }

        String getMessage() {
            return message;
        }

        void setMessage(String message) {
            this.message = message;
        }
    }
}
