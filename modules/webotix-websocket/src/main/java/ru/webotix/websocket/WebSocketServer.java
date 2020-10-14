package ru.webotix.websocket;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.job.status.api.JobStatus;
import ru.webotix.market.data.api.*;
import ru.webotix.notification.api.Notification;
import ru.webotix.utils.SafelyClose;
import ru.webotix.utils.SafelyDispose;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Metered
@Timed
@ExceptionMetered
@ServerEndpoint(WebSocketModule.ENTRY_POINT)
public final class WebSocketServer {

    private static final int READY_TIMEOUT = 5000;

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);

    private Session session;
    private Disposable disposable;

    private ObjectMapper objectMapper;
    private EventBus eventBus;
    private ExchangeEventRegistry exchangeEventRegistry;

    private final AtomicLong lastReadyTime = new AtomicLong();

    private final AtomicReference<ImmutableSet<MarketDataSubscription>> marketDataSubscriptions
            = new AtomicReference<>(ImmutableSet.of());

    private ExchangeEventRegistry.ExchangeEventSubscription subscription;

    @Inject
    void inject(ExchangeEventRegistry exchangeEventRegistry, ObjectMapper objectMapper, EventBus eventBus) {
        this.exchangeEventRegistry = exchangeEventRegistry;
        this.objectMapper = objectMapper;
        this.eventBus = eventBus;
    }

    @OnOpen
    public synchronized void myOnOpen(final Session session) {
        log.info("Opening socket");
        markReady();
        Injector injector = (Injector) session.getUserProperties().get(Injector.class.getName());
        injector.injectMembers(this);
        this.session = session;
        eventBus.register(this);

    }

    @OnMessage
    public void myOnMsg(final Session session, String message) {
        try {

            log.debug("Received websocket message: {}", message);

            WebSocketInMessage request = decodeRequest(message);

            switch (request.command()) {
                case READY:
                    markReady();
                    break;
                case CHANGE_TICKERS:
                    mutateSubscriptions(MarketDataType.Ticker,
                            Objects.requireNonNull(request.tickers()));
                    break;
                case CHANGE_OPEN_ORDERS:
                    mutateSubscriptions(MarketDataType.OpenOrders,
                            Objects.requireNonNull(request.tickers()));
                    break;
                case CHANGE_ORDER_BOOK:
                    mutateSubscriptions(MarketDataType.OrderBook,
                            Objects.requireNonNull(request.tickers()));
                    break;
                case CHANGE_TRADES:
                    mutateSubscriptions(MarketDataType.Trades,
                            Objects.requireNonNull(request.tickers()));
                    break;
                case CHANGE_USER_TRADES:
                    mutateSubscriptions(MarketDataType.UserTrade,
                            Objects.requireNonNull(request.tickers()));
                    break;
                case CHANGE_BALANCE:
                    mutateSubscriptions(MarketDataType.Balance,
                            Objects.requireNonNull(request.tickers()));
                    break;
                case CHANGE_ORDER_STATUS_CHANGE:
                    mutateSubscriptions(MarketDataType.Order,
                            Objects.requireNonNull(request.tickers()));
                    break;
                case UPDATE_SUBSCRIPTIONS:
                    updateSubscriptions();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid command: " + request.command());
            }

        } catch (Exception e) {
            log.error("Error processing message: " + message, e);
            send("Error processing message", WebSocketNatureMessage.ERROR);
        }
    }

    private boolean isReady() {
        boolean result = (System.currentTimeMillis() - lastReadyTime.get()) < READY_TIMEOUT;
        if (!result) log.debug("Suppressing outgoing message as client is not ready");
        return result;
    }

    private synchronized void updateSubscriptions() {
        Set<MarketDataSubscription> target = marketDataSubscriptions.get();

        log.debug("Updating subscriptions to {}", target);

        SafelyDispose.of(disposable);

        if (subscription == null) {
            subscription = exchangeEventRegistry.subscribe(target);
        } else {
            subscription = subscription.replace(target);
        }

        disposable = new Disposable() {

            private final List<Disposable> tickers =
                    StreamSupport.stream(subscription.getTickersSplit().spliterator(), false)
                            .map(f -> f.filter(e -> isReady())
                                    .throttleLast(1, TimeUnit.SECONDS)
                                    .subscribe(e -> send(e, WebSocketNatureMessage.TICKER)))
                            .collect(Collectors.toList());

            private final Disposable orderBook =
                    subscription
                            .getOrderBooks()
                            .filter(o -> isReady())
                            .throttleLast(1, TimeUnit.SECONDS)
                            .subscribe(e -> send(e, WebSocketNatureMessage.ORDERBOOK));

            private final Disposable trades =
                    subscription
                            .getTrades()
                            .filter(o -> isReady())
                            .map(WebSocketServer.this::serialiseTradeEvent)
                            .subscribe(e -> send(e, WebSocketNatureMessage.TRADE));

            private final Disposable orders =
                    subscription
                            .getOrderChanges()
                            .filter(o -> isReady())
                            .subscribe(e -> send(e, WebSocketNatureMessage.ORDER_STATUS_CHANGE));

            private final Disposable userTrades =
                    subscription
                            .getUserTrades()
                            .filter(o -> isReady())
                            .map(WebSocketServer.this::serialiseUserTradeEvent)
                            .subscribe(e -> send(e, WebSocketNatureMessage.USER_TRADE));

            private final Disposable balance =
                    subscription
                            .getBalances()
                            .filter(o -> isReady())
                            .subscribe(e -> send(e, WebSocketNatureMessage.BALANCE));

            private final Disposable openOrders =
                    subscription
                            .getOrderSnapshots()
                            .filter(o -> isReady())
                            .subscribe(e -> send(e, WebSocketNatureMessage.OPEN_ORDERS));

            @Override
            public void dispose() {
                SafelyDispose.of(openOrders, orderBook, trades, orders, userTrades, balance);
                SafelyDispose.of(tickers);
            }

            @Override
            public boolean isDisposed() {
                return openOrders.isDisposed()
                        && orderBook.isDisposed()
                        && tickers.stream().allMatch(Disposable::isDisposed)
                        && trades.isDisposed()
                        && orders.isDisposed()
                        && userTrades.isDisposed()
                        && balance.isDisposed();
            }
        };
    }

    private Object serialiseTradeEvent(TradeEvent e) {
        return SerializableTradeEvent.create(
                e.spec(), SerializableTrade.create(e.spec().exchange(), e.trade()));
    }

    private Object serialiseUserTradeEvent(UserTradeEvent e) {
        return SerializableTradeEvent.create(
                e.spec(), SerializableTrade.create(e.spec().exchange(), e.trade()));
    }

    private WebSocketInMessage decodeRequest(String message) {

        WebSocketInMessage request;

        try {
            request = objectMapper.readValue(message, WebSocketInMessage.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid request", e);
        }
        return request;
    }

    private void mutateSubscriptions(MarketDataType marketDataType, Collection<TickerSpec> tickers) {

        marketDataSubscriptions.set(ImmutableSet.<MarketDataSubscription>builder()
                .addAll(marketDataSubscriptions.get()
                        .stream()
                        .filter(sub -> !sub.type().equals(marketDataType))
                        .collect(Collectors.toList()))
                .addAll(tickers.stream()
                        .map(spec -> MarketDataSubscription.create(spec, marketDataType))
                        .collect(Collectors.toList())
                )
                .build());

    }

    private void markReady() {
        log.debug("Client is ready");
        lastReadyTime.set(System.currentTimeMillis());
    }

    @OnClose
    public synchronized void myOnClose(final Session session, CloseReason cr) {
        log.info("Closing socket ({})", cr);
        SafelyDispose.of(disposable);
        disposable = null;
        marketDataSubscriptions.set(ImmutableSet.of());
        SafelyClose.the(subscription);
        subscription = null;
    }

    @OnError
    public void onError(Throwable error) {
        log.error("Socket error", error);
    }

    private synchronized void send(Object object, WebSocketNatureMessage nature) {
        log.debug("{}: {}", nature, object);
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message(nature, object));
            }
        } catch (Exception e) {
            log.warn("Failed to send {} to socket ({})", nature, e.getMessage());
        }
    }

    private String message(WebSocketNatureMessage nature, Object data) {
        try {
            return objectMapper.writeValueAsString(WebSocketOutMessage.create(nature, data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    void onNotification(Notification notification) {
        send(notification, WebSocketNatureMessage.NOTIFICATION);
    }

    @Subscribe
    void onStatusUpdate(JobStatus status) {
        send(status, WebSocketNatureMessage.STATUS_UPDATE);
    }

}
