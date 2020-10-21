package ru.webotix.market.data;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.*;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrencyPair;
import org.knowm.xchange.service.trade.params.TradeHistoryParamLimit;
import org.knowm.xchange.service.trade.params.TradeHistoryParamPaging;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import ru.webotix.datasource.wiring.BackgroundProcessingConfiguration;
import ru.webotix.exchange.AccountServiceFactory;
import ru.webotix.exchange.ExchangeConfiguration;
import ru.webotix.exchange.Exchanges;
import ru.webotix.exchange.TradeServiceFactory;
import ru.webotix.exchange.api.ExchangeService;
import ru.webotix.exchange.api.RateController;
import ru.webotix.market.data.api.*;
import ru.webotix.notification.api.NotificationService;
import ru.webotix.utils.CheckedExceptions;
import ru.webotix.utils.SafelyDispose;
import si.mazi.rescu.HttpStatusIOException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static jersey.repackaged.com.google.common.base.MoreObjects.firstNonNull;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

@Singleton
public class MarketDataSubscriptionController extends AbstractPollingController {

    private static final int MAX_TRADES = 20;
    private static final int ORDERBOOK_DEPTH = 20;
    private static final int MINUTES_BETWEEN_EXCEPTION_NOTIFICATIONS = 15;

    private final ExchangeService exchangeService;
    private final TradeServiceFactory tradeServiceFactory;
    private final AccountServiceFactory accountServiceFactory;
    private final NotificationService notificationService;
    private final Map<String, ExchangeConfiguration> exchangeConfiguration;

    private final Map<String, AtomicReference<Set<MarketDataSubscription>>> nextSubscriptions;
    private final ConcurrentMap<String, Set<MarketDataSubscription>> subscriptionsPerExchange =
            Maps.newConcurrentMap();
    private final ConcurrentMap<String, Set<MarketDataSubscription>> pollsPerExchange =
            Maps.newConcurrentMap();
    private final Multimap<String, Disposable> disposablesPerExchange = HashMultimap.create();
    private final Set<MarketDataSubscription> unavailableSubscriptions = Sets.newConcurrentHashSet();

    private final ConcurrentMap<TickerSpec, Instant> mostRecentTrades = Maps.newConcurrentMap();

    @Inject
    public MarketDataSubscriptionController(BackgroundProcessingConfiguration configuration,
                                            SubscriptionPublisher publisher,
                                            ExchangeService exchangeService,
                                            TradeServiceFactory tradeServiceFactory,
                                            AccountServiceFactory accountServiceFactory,
                                            NotificationService notificationService,
                                            Map<String, ExchangeConfiguration> exchangeConfiguration) {
        super(configuration, publisher);
        this.exchangeService = exchangeService;
        this.tradeServiceFactory = tradeServiceFactory;
        this.accountServiceFactory = accountServiceFactory;
        this.notificationService = notificationService;
        this.exchangeConfiguration = exchangeConfiguration;
        this.nextSubscriptions = exchangeService.getExchanges()
                .stream().collect(Collectors.toMap(Function.identity(), e -> new AtomicReference<>()));

        exchangeService.getExchanges()
                .forEach(e -> {
                    subscriptionsPerExchange.put(e, ImmutableSet.of());
                    pollsPerExchange.put(e, ImmutableSet.of());
                });
    }

    @Override
    protected void doRun() throws InterruptedException {

        ExecutorService threadPool = Executors.newFixedThreadPool(
                exchangeService.getExchanges().size());

        try {
            try {
                submitExchangesAndWaitForCompletion(threadPool);
                log.info("{} stopping; all exchanges have shut down", this);
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                log.error(this + " stopping due to uncaught exception", e);
            }
        } finally {
            threadPool.shutdownNow();
        }
    }

    /**
     * Запускает потоки для работы с биржами. Ожидаем результат выполнения потоков
     *
     * @param threadPool Пул потоков
     */
    private void submitExchangesAndWaitForCompletion(ExecutorService threadPool) throws InterruptedException {

        Map<String, Future<?>> futures = new HashMap<>();

        for (String exchange : exchangeService.getExchanges()) {
            if (exchangeConfiguration.getOrDefault(exchange, new ExchangeConfiguration()).isEnabled()) {
                futures.put(exchange, threadPool.submit(new Poller(exchange)));
            }
        }

        for (Map.Entry<String, Future<?>> entry : futures.entrySet()) {
            try {
                entry.getValue().get();
            } catch (ExecutionException e) {
                log.error(entry.getKey() + "failed with uncaught exception and will not restart", e);
            }
        }
    }

    @Override
    public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {

        ImmutableListMultimap<String, MarketDataSubscription> byExchange
                = Multimaps.index(subscriptions, s -> s.spec().exchange());

        for (String exchangeName : exchangeService.getExchanges()) {
            nextSubscriptions.get(exchangeName)
                    .set(ImmutableSet
                            .copyOf(byExchange.get(exchangeName))
                    );
        }

        wake();
    }

    /**
     * Обрабатывает опрос рыночных данных и цикл подписки для обмена.
     */
    private final class Poller implements Runnable {

        private final String exchangeName;
        private StreamingExchange streamingExchange;
        private AccountService accountService;
        private MarketDataService marketDataService;
        private TradeService tradeService;

        private int phase;
        private boolean subscriptionsFailed;
        private Exception lastPollException;
        private LocalDateTime lastPollErrorNotificationTime;

        private Poller(String exchangeName) {
            this.exchangeName = exchangeName;
        }

        @Override
        public void run() {

            Thread.currentThread().setName(exchangeName);

            log.info("{} starting", exchangeName);

            try {

                initialize();

                while (!isTerminated()) {

                    // Прежде чем проверять наличие опросов, определить, на каком этапе
                    // мы будем ждать, если нет работы, то есть
                    // следующее пробуждение.
                    phase = getPhase();
                    if (phase == -1)
                        break;

                    loop();
                }

                log.info("{} shutting down due to termination", exchangeName);
            } catch (InterruptedException e) {
                log.info("{} shutting down due to interrupt", exchangeName);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(exchangeName + " shutting down due to uncaught exception", e);
            } finally {
                subtaskStopped(exchangeName);
            }
        }

        /**
         * Это может привести к сбою, когда обмен недоступен, поэтому продолжайте попытки.
         */
        private void initialize() throws InterruptedException {

            while (isRunning()) {
                try {

                    Exchange exchange = exchangeService.get(exchangeName);

                    this.streamingExchange
                            = exchange instanceof StreamingExchange
                            ? (StreamingExchange) exchange : null;

                    this.accountService = accountServiceFactory
                            .getForExchange(exchangeName);

                    this.marketDataService = exchange
                            .getMarketDataService();

                    this.tradeService = tradeServiceFactory
                            .getForExchange(exchangeName);

                    break;

                } catch (Exception e) {
                    log.error(exchangeName + " - failing initialising. Will retry in one minute.", e);
                    Thread.sleep(60000);
                }
            }
        }

        private void loop() throws InterruptedException {
            // Проверьте, есть ли изменение подписки в очереди. Если так, примените это
            doSubscriptionChanges();

            // Проверяем, есть ли у нас опрос.
            // Если нет, переходите в режим сна, пока не разбудите изменения подписки,
            // если только мы не смогли обработать подписки, и в этом случае просыпаемся
            // через несколько секунд, чтобы повторить попытку
            Set<MarketDataSubscription> polls = activePolls();
            if (polls.isEmpty()) {
                suspend(exchangeName, phase, subscriptionsFailed);
                return;
            }

            log.debug("{} - start poll", exchangeName);
            Set<String> balanceCurrencies = new HashSet<>();
            for (MarketDataSubscription subscription : polls) {
                if (isTerminated())
                    break;
                if (subscription.type().equals(MarketDataType.Balance)) {
                    balanceCurrencies.add(subscription.spec().base());
                    balanceCurrencies.add(subscription.spec().counter());
                } else {
                    fetchAndBroadcast(subscription);
                }
            }

            if (isTerminated())
                return;

            if (!balanceCurrencies.isEmpty()) {
                manageExchangeExceptions("Balances",
                        () -> fetchBalances(balanceCurrencies)
                                .forEach(balance ->
                                        publisher.emit(
                                                BalanceEvent.create(
                                                        exchangeName,
                                                        balance
                                                ))),
                        () -> polls.stream()
                                .filter(s -> s.type()
                                        .equals(MarketDataType.Balance))
                                .collect(Collectors.toList())
                );
            }
        }


        private Wallet wallet() throws IOException {

            exchangeService.rateController(exchangeName).acquire();

            Wallet wallet;

            wallet = accountService.getAccountInfo()
                    .getWallet();

            if (wallet == null) {
                throw new IllegalStateException("No wallet returned");
            }
            return wallet;
        }

        private Iterable<Balance> fetchBalances(Collection<String> currencyCodes)
                throws IOException {

            Map<String, Balance> result = new HashMap<>();

            currencyCodes.stream()
                    .map(Currency::getInstance)
                    .map(Balance::zero)
                    .forEach(balance -> result.put(balance.getCurrency().getCurrencyCode(), balance));

            wallet().getBalances().values().stream()
                    .filter(balance -> currencyCodes.contains(balance.getCurrency().getCurrencyCode()))
                    .forEach(balance -> result.put(balance.getCurrency().getCurrencyCode(), balance));
            return result.values();
        }

        private void manageExchangeExceptions(String dataDescription,
                                              CheckedExceptions.ThrowingRunnable runnable,
                                              Supplier<Iterable<MarketDataSubscription>> toUnsubscribe)
                throws InterruptedException {
            try {
                runnable.run();
            } catch (InterruptedException e) {

                throw e;

            } catch (UnsupportedOperationException e) {

                log.warn("{} not available: {} ({})", dataDescription,
                        e.getClass().getSimpleName(), exceptionMessage(e));

                Iterables.addAll(unavailableSubscriptions, toUnsubscribe.get());

            } catch (SocketTimeoutException | SocketException
                    | ExchangeUnavailableException | NonceException e) {

                log.warn("Throttling {} - {} ({}) when fetching {}", exchangeName,
                        e.getClass().getSimpleName(), exceptionMessage(e), dataDescription);
                exchangeService.rateController(exchangeName).throttle();

            } catch (HttpStatusIOException e) {

                handleHttpStatusException(dataDescription, e);

            } catch (RateLimitExceededException | FrequencyLimitExceededException e) {

                log.error("Hit rate limiting on {} when fetching {}. Backing off", exchangeName, dataDescription);

                notificationService.error("Getting rate limiting errors on " + exchangeName
                        + ". Pausing access and will resume at a lower rate.");

                RateController rateController = exchangeService.rateController(exchangeName);

                rateController.backoff();
                rateController.pause();

            } catch (ExchangeException e) {

                if (e.getCause() instanceof HttpStatusIOException) {
                    handleHttpStatusException(dataDescription, (HttpStatusIOException) e.getCause());
                } else {
                    handleUnknownPollException(e);
                }
            } catch (Exception e) {
                handleUnknownPollException(e);
            }
        }

        private void handleHttpStatusException(String dataDescription, HttpStatusIOException e) {
            if (e.getHttpStatusCode() == 408 || e.getHttpStatusCode() == 502
                    || e.getHttpStatusCode() == 504 || e.getHttpStatusCode() == 521) {
                log.warn("Throttling {} - failed at gateway ({} - {}) when fetching {}",
                        exchangeName, e.getHttpStatusCode(), exceptionMessage(e), dataDescription);
                exchangeService.rateController(exchangeName).throttle();
            } else {
                handleUnknownPollException(e);
            }
        }

        private void handleUnknownPollException(Exception e) {
            LocalDateTime now = now();
            String exceptionMessage = exceptionMessage(e);
            if (lastPollException == null ||
                    !lastPollException.getClass().equals(e.getClass()) ||
                    !firstNonNull(exceptionMessage(lastPollException), "").equals(exceptionMessage) ||
                    lastPollErrorNotificationTime.until(now, MINUTES) > MINUTES_BETWEEN_EXCEPTION_NOTIFICATIONS) {
                lastPollErrorNotificationTime = now;
                log.error("Error fetching data for " + exchangeName, e);
                notificationService.error("Throttling access to "
                        + exchangeName + " due to server error ("
                        + e.getClass().getSimpleName() + " - " + exceptionMessage + ")");
            } else {
                log.error("Repeated error fetching data for {} ({})", exchangeName, exceptionMessage);
            }
            lastPollException = e;
            exchangeService.rateController(exchangeName).throttle();
        }

        private Set<MarketDataSubscription> activePolls() {
            return pollsPerExchange.get(exchangeName).stream()
                    .filter(s -> !unavailableSubscriptions.contains(s))
                    .collect(Collectors.toSet());
        }

        private String exceptionMessage(Throwable e) {
            if (e.getMessage() == null) {
                if (e.getCause() == null) {
                    return "No description";
                } else {
                    return exceptionMessage(e.getCause());
                }
            } else {
                return e.getMessage();
            }
        }

        /**
         * Получить данные и поделится ими
         *
         * @param subscription слушатель
         * @throws InterruptedException данный код может прерваться
         */
        private void fetchAndBroadcast(MarketDataSubscription subscription) throws InterruptedException {
            exchangeService.rateController(exchangeName).acquire();
            TickerSpec spec = subscription.spec();
            manageExchangeExceptions(subscription.key(),
                    () -> {
                        switch (subscription.type()) {
                            case Ticker:
                                pollAndEmitTicker(spec);
                                break;
                            case OrderBook:
                                pollAndEmitOrderBook(spec);
                                break;
                            case Trades:
                                pollAndEmitTrades(subscription);
                                break;
                            case OpenOrders:
                                pollAndEmitOpenOrders(subscription);
                                break;
                            case UserTrade:
                                pollAndEmitUserTradeHistory(subscription);
                                break;
                            case Order:
                                // TODO В настоящее время не поддерживается опросом
                                break;
                            default:
                                throw new IllegalStateException("Market data type "
                                        + subscription.type() + " not supported in this way");
                        }
                    },
                    () -> ImmutableSet.of(subscription));
        }

        private void pollAndEmitTicker(TickerSpec spec) throws IOException {
            publisher.emit(TickerEvent.create(
                    spec, marketDataService.getTicker(spec.currencyPair())));
        }

        private void pollAndEmitUserTradeHistory(MarketDataSubscription subscription)
                throws IOException {
            TradeHistoryParams tradeHistoryParams = tradeHistoryParams(subscription);
            tradeService
                    .getTradeHistory(tradeHistoryParams)
                    .getUserTrades()
                    .forEach(trade -> publisher.emit(UserTradeEvent.create(subscription.spec(), trade)));
        }

        private TradeHistoryParams tradeHistoryParams(MarketDataSubscription subscription) {
            TradeHistoryParams params;

            if (subscription.spec().exchange().equals(Exchanges.BITMEX)
                    || subscription.spec().exchange().equals(Exchanges.GDAX)) {
                params =
                        new TradeHistoryParamCurrencyPair() {

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
            } else {
                params = tradeService.createTradeHistoryParams();
            }

            if (params instanceof TradeHistoryParamCurrencyPair) {
                ((TradeHistoryParamCurrencyPair) params)
                        .setCurrencyPair(subscription.spec().currencyPair());
            } else {
                throw new UnsupportedOperationException(
                        "Don't know how to read user trades on this exchange: "
                                + subscription.spec().exchange());
            }
            if (params instanceof TradeHistoryParamLimit) {
                ((TradeHistoryParamLimit) params).setLimit(MAX_TRADES);
            }
            if (params instanceof TradeHistoryParamPaging) {
                ((TradeHistoryParamPaging) params).setPageLength(MAX_TRADES);
                ((TradeHistoryParamPaging) params).setPageNumber(0);
            }
            return params;
        }

        private void pollAndEmitTrades(MarketDataSubscription subscription) throws IOException {
            marketDataService
                    .getTrades(subscription.spec().currencyPair())
                    .getTrades()
                    .forEach(
                            t -> mostRecentTrades.compute(
                                    subscription.spec(),
                                    (k, previousTiming) -> {
                                        Instant thisTradeTiming = t.getTimestamp().toInstant();
                                        Instant newMostRecent = previousTiming;
                                        if (previousTiming == null) {
                                            newMostRecent = thisTradeTiming;
                                        } else if (thisTradeTiming.isAfter(previousTiming)) {
                                            newMostRecent = thisTradeTiming;
                                            publisher.emit(TradeEvent.create(subscription.spec(), t));
                                        }
                                        return newMostRecent;
                                    }));
        }

        private OpenOrdersParams openOrdersParams(MarketDataSubscription subscription) {
            OpenOrdersParams params = null;
            try {
                params = tradeService.createOpenOrdersParams();
            } catch (NotYetImplementedForExchangeException e) {
                // Fiiiiine Bitmex
            }
            if (params == null) {
                // Bitfinex & Bitmex
                params = new DefaultOpenOrdersParamCurrencyPair(subscription.spec().currencyPair());
            } else if (params instanceof OpenOrdersParamCurrencyPair) {
                ((OpenOrdersParamCurrencyPair) params)
                        .setCurrencyPair(subscription.spec().currencyPair());
            } else {
                throw new UnsupportedOperationException(
                        "Don't know how to read open orders on this exchange: "
                                + subscription.spec().exchange());
            }
            return params;
        }

        private void pollAndEmitOpenOrders(MarketDataSubscription subscription) throws IOException {
            OpenOrdersParams openOrdersParams = openOrdersParams(subscription);

            Date originatingTimestamp = new Date();
            OpenOrders fetched = tradeService.getOpenOrders(openOrdersParams);

            publisher.emit(OpenOrdersEvent.create(subscription.spec(), fetched, originatingTimestamp));
        }

        private void pollAndEmitOrderBook(TickerSpec spec) throws IOException {
            OrderBook orderBook =
                    marketDataService.getOrderBook(spec.currencyPair(), exchangeOrderBookArgs(spec));
            publisher.emit(OrderBookEvent.create(spec, orderBook));
        }

        private Object[] exchangeOrderBookArgs(TickerSpec spec) {
            if (spec.exchange().equals(Exchanges.BITMEX)) {
                return new Object[]{};
            } else {
                return new Object[]{ORDERBOOK_DEPTH, ORDERBOOK_DEPTH};
            }
        }

        /**
         * На самом деле выполняет изменения подписки.
         * Происходит синхронно в цикле опроса.
         */
        private void doSubscriptionChanges() {

            log.debug("{} - start subscription check", exchangeName);
            subscriptionsFailed = false;

            // Вытащить изменение подписки из очереди. Если нет, мы сделали
            Set<MarketDataSubscription> subscriptions = nextSubscriptions
                    .get(exchangeName)
                    .getAndSet(null);

            if (subscriptions == null)
                return;

            try {

                // Получить текущие подписки
                Set<MarketDataSubscription> oldSubscriptions = StreamSupport.stream(
                        Iterables.concat(
                                subscriptionsPerExchange.get(exchangeName),
                                pollsPerExchange.get(exchangeName)
                        ).spliterator(), false).collect(Collectors.toSet());

                // Если нет разницы, мы хорошо, готово
                if (subscriptions.equals(oldSubscriptions)) {
                    return;
                }

                log.info("{} - updating subscriptions to: {} from {}",
                        exchangeName, subscriptions, oldSubscriptions);

                // Отключите любые потоковые обмены,
                // на которых подписанные в настоящее время тикеры не соответствуют
                // тем, которые мы хотим.

                if (!oldSubscriptions.isEmpty()) {
                    disconnect();
                }

                // Очистите кэшированные тикеры и книги заказов для всего,
                // что мы отписались, чтобы мы не передавали устаревшие данные
                Sets.difference(oldSubscriptions, subscriptions)
                        .forEach(publisher::clearCacheForSubscription);

                // Добавить новые подписки, если у нас есть
                if (subscriptions.isEmpty()) {
                    pollsPerExchange.put(exchangeName, ImmutableSet.of());
                    log.debug("{} - polls cleared", exchangeName);
                } else {
                    subscribe(subscriptions);
                }
            } catch (Exception e) {
                subscriptionsFailed = true;
                log.error("Error updating subscriptions", e);
                if (nextSubscriptions.get(exchangeName).compareAndSet(null, subscriptions)) {
                    wake();
                }
                throw e;
            }
        }

        /**
         * Подписать слушателей к бирже
         * <p>
         * Поитогу получаем новый массив слушателей которые должны
         * быть подключены при следующей попытке
         *
         * @param subscriptions Подписки
         */
        private void subscribe(Set<MarketDataSubscription> subscriptions) {

            ImmutableSet.Builder<MarketDataSubscription> pollingBuilder = ImmutableSet.builder();

            if (streamingExchange != null) {
                Set<MarketDataSubscription> remainingSubscriptions = openSubscriptionsWherePossible(subscriptions);
                pollingBuilder.addAll(remainingSubscriptions);
            } else {
                pollingBuilder.addAll(subscriptions);
            }

            Set<MarketDataSubscription> polls = pollingBuilder.build();
            pollsPerExchange.put(exchangeName, polls);
            log.debug("{} - polls now set to: {}", exchangeName, polls);
        }

        /**
         * Добавить слушателей которые могут подписаться к потоку
         *
         * @param subscriptions Подписчики
         * @return Активные подписки
         */
        private Set<MarketDataSubscription> openSubscriptionsWherePossible(
                Set<MarketDataSubscription> subscriptions) {

            connectExchange(subscriptions);

            HashSet<MarketDataSubscription> connected = new HashSet<>(subscriptions);
            ImmutableSet.Builder<MarketDataSubscription> remainder = ImmutableSet.builder();
            List<Disposable> disposables = new ArrayList<>();

            Consumer<MarketDataSubscription> marketAsNotSubscribed = subscription -> {
                remainder.add(subscription);
                connected.remove(subscription);
            };

            Set<String> balanceCurrencies = new HashSet<>();

            for (MarketDataSubscription subscription : subscriptions) {

                // Пользовательские торговые и балансовые подписки, на данный момент мы будем опрашивать,
                // даже если мы уже получаем их из сокета.
                // Это будет продолжаться до тех пор, пока мы не сможем безопасно обнаруживать
                // и исправлять упорядоченные / пропущенные сообщения в потоках сокетов.
                if (subscription.type().equals(MarketDataType.UserTrade)
                        || subscription.type().equals(MarketDataType.Balance)) {
                    remainder.add(subscription);
                }

                if (subscription.type().equals(MarketDataType.Balance)) {
                    balanceCurrencies.add(subscription.spec().base());
                    balanceCurrencies.add(subscription.spec().counter());
                } else {
                    try {
                        disposables.add(connectSubscription(subscription));
                    } catch (UnsupportedOperationException | ExchangeSecurityException e) {
                        log.debug("Not subscribing to {} on socket due to {}: {}",
                                subscription.key(),
                                e.getClass().getSimpleName(),
                                e.getMessage());
                        marketAsNotSubscribed.accept(subscription);
                    }
                }
            }

            try {
                for (String currency : balanceCurrencies) {
                    disposables.add(
                            streamingExchange
                                    .getStreamingAccountService()
                                    .getBalanceChanges(
                                            Currency.getInstance(currency), "exchange")
                                    .map(balance -> BalanceEvent.create(exchangeName, balance))
                                    .subscribe(publisher::emit,
                                            e -> log.error("Error in balance stream for "
                                                    + exchangeName + "/" + currency, e))
                    );
                }
            } catch (NotAvailableFromExchangeException e) {
                subscriptions.stream()
                        .filter(subscription -> subscription.type().equals(MarketDataType.Balance))
                        .forEach(marketAsNotSubscribed);
            } catch (ExchangeSecurityException | NotYetImplementedForExchangeException e) {
                log.debug("Not subscribing to {}/{} on socket due to {}: {}", exchangeName,
                        "Balances", e.getClass().getSimpleName(), e.getMessage());
                subscriptions.stream()
                        .filter(subscription -> subscription.type().equals(MarketDataType.Balance))
                        .forEach(marketAsNotSubscribed);
            }
            subscriptionsPerExchange.put(exchangeName, Collections.unmodifiableSet(connected));
            disposablesPerExchange.putAll(exchangeName, disposables);
            return remainder.build();
        }

        private void connectExchange(Collection<MarketDataSubscription> subscriptionsForExchange) {
            if (subscriptionsPerExchange.isEmpty()) {
                return;
            }
            log.info("Connecting to exchange: {}", exchangeName);
            ProductSubscription.ProductSubscriptionBuilder builder = ProductSubscription.create();
            boolean authenticated = exchangeService.isAuthenticated(exchangeName);
            subscriptionsForExchange
                    .forEach(subscription -> {
                        if (subscription.type().equals(MarketDataType.Ticker)) {
                            builder.addTicker(subscription.spec().currencyPair());
                        }
                        if (subscription.type().equals(MarketDataType.OrderBook)) {
                            builder.addOrderbook(subscription.spec().currencyPair());
                        }
                        if (subscription.type().equals(MarketDataType.Trades)) {
                            builder.addTrades(subscription.spec().currencyPair());
                        }
                        if (authenticated && subscription.type().equals(MarketDataType.UserTrade)) {
                            builder.addUserTrades(subscription.spec().currencyPair());
                        }
                        if (authenticated && subscription.type().equals(MarketDataType.Order)) {
                            builder.addOrders(subscription.spec().currencyPair());
                        }
                        if (authenticated && subscription.type().equals(MarketDataType.Balance)) {
                            builder.addBalances(subscription.spec().currencyPair().base);
                            builder.addBalances(subscription.spec().currencyPair().counter);
                        }
                    });
            exchangeService.rateController(exchangeName).acquire();
            streamingExchange.connect(builder.build()).blockingAwait();
            log.info("Connected to exchange: {}", exchangeName);
        }

        private Disposable connectSubscription(MarketDataSubscription sub) {
            switch (sub.type()) {
                case OrderBook:
                    return streamingExchange
                            .getStreamingMarketDataService()
                            .getOrderBook(sub.spec().currencyPair())
                            .map(t -> OrderBookEvent.create(sub.spec(), t))
                            .subscribe(
                                    publisher::emit, e -> log.error("Error in order book stream for " + sub, e));
                case Ticker:
                    log.debug("Subscribing to {}", sub.spec());
                    return streamingExchange
                            .getStreamingMarketDataService()
                            .getTicker(sub.spec().currencyPair())
                            .map(t -> TickerEvent.create(sub.spec(), t))
                            .subscribe(publisher::emit,
                                    e -> log.error("Error in ticker stream for " + sub, e));
                case Trades:
                    return streamingExchange
                            .getStreamingMarketDataService()
                            .getTrades(sub.spec().currencyPair())
                            .map(t -> convertBinanceOrderType(sub, t))
                            .map(t -> TradeEvent.create(sub.spec(), t))
                            .subscribe(publisher::emit, e -> log.error("Error in trade stream for " + sub, e));
                case UserTrade:
                    return streamingExchange
                            .getStreamingTradeService()
                            .getUserTrades(sub.spec().currencyPair())
                            .map(t -> UserTradeEvent.create(sub.spec(), t))
                            .subscribe(publisher::emit, e -> log.error("Error in trade stream for " + sub, e));
                case Order:
                    return streamingExchange
                            .getStreamingTradeService()
                            .getOrderChanges(sub.spec().currencyPair())
                            .map(t ->
                                    OrderChangeEvent.create(
                                            sub.spec(), t, new Date()))
                            .subscribe(publisher::emit, e -> log.error("Error in order stream for " + sub, e));
                default:
                    throw new NotAvailableFromExchangeException();
            }
        }

        /**
         * Отключить слушателей от биржы
         */
        private void disconnect() {
            if (streamingExchange != null) {
                SafelyDispose.of(disposablesPerExchange.removeAll(exchangeName));
                try {
                    streamingExchange.disconnect().blockingAwait();
                } catch (Exception e) {
                    log.error("Error disconnecting from " + exchangeName, e);
                }
            } else {
                mostRecentTrades.entrySet()
                        .removeIf(tickerSpecInstantEntry ->
                                tickerSpecInstantEntry.getKey().exchange().equals(exchangeName));
            }
        }

        private Trade convertBinanceOrderType(MarketDataSubscription sub, Trade t) {
            if (sub.spec().exchange().equals(Exchanges.BINANCE)) {
                return Trade.Builder.from(t).type(t.getType() == BID ? ASK : BID).build();
            } else {
                return t;
            }
        }
    }
}
