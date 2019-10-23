package ru.webotix.market.data;

import com.google.common.collect.*;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.exceptions.*;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.AccountServiceFactory;
import ru.webotix.exchange.ExchangeService;
import ru.webotix.exchange.RateController;
import ru.webotix.exchange.TradeServiceFactory;
import ru.webotix.exchange.info.TickerSpec;
import ru.webotix.notification.NotificationService;
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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static info.bitrich.xchangestream.core.ProductSubscription.ProductSubscriptionBuilder;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static jersey.repackaged.com.google.common.base.MoreObjects.firstNonNull;

@Singleton
public class MarketDataSubscriptionManager extends AbstractExecutionThreadService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeEventBus.class);

    private static final int MINUTES_BETWEEN_EXCEPTION_NOTIFICATIONS = 15;

    private final ExchangeService exchangeService;
    private final AccountServiceFactory accountServiceFactory;
    private final TradeServiceFactory tradeServiceFactory;
    private final NotificationService notificationService;

    private final Map<String, AtomicReference<Set<MarketDataSubscription>>> nextSubscriptions;
    private final ConcurrentMap<String, Set<MarketDataSubscription>> subscriptionsPerExchange = Maps.newConcurrentMap();
    private final ConcurrentMap<String, Set<MarketDataSubscription>> pollsPerExchange = Maps.newConcurrentMap();
    private final Multimap<String, Disposable> disposablesPerExchange = HashMultimap.create();
    private final Set<MarketDataSubscription> unavailableSubscriptions = Sets.newConcurrentHashSet();

    private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;

    private final ConcurrentMap<TickerSpec, Instant> mostRecentTrades = Maps.newConcurrentMap();

    private final Phaser phaser = new Phaser(1);

    private LifecycleListener lifecycleListener = new LifecycleListener() {
    };

    @Inject
    public MarketDataSubscriptionManager(ExchangeService exchangeService,
                                         AccountServiceFactory accountServiceFactory,
                                         TradeServiceFactory tradeServiceFactory,
                                         NotificationService notificationService) {
        this.exchangeService = exchangeService;
        this.accountServiceFactory = accountServiceFactory;
        this.tradeServiceFactory = tradeServiceFactory;
        this.notificationService = notificationService;

        this.nextSubscriptions = exchangeService.getExchanges()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(), e -> new AtomicReference<>()
                ));

        exchangeService.getExchanges().forEach(e -> {
            subscriptionsPerExchange.put(e, ImmutableSet.of());
            pollsPerExchange.put(e, ImmutableSet.of());
        });

        this.balanceOut = new CachingPersistentPublisher<>(
                (BalanceEvent e) -> e.exchange() + "/" + e.currency()
        );
    }


    void setLifecycleListener(LifecycleListener listener) {
        this.lifecycleListener = listener;
    }


    public Flowable<BalanceEvent> getBalances() {
        return balanceOut.getAll();
    }

    @Override
    protected void run() throws Exception {
        Thread.currentThread()
                .setName(MarketDataSubscriptionManager.class.getSimpleName());

        log.info("{} started", this);

        ExecutorService threadPool = Executors.newFixedThreadPool(
                exchangeService.getExchanges().size());

        try {
            try {
                submitExchangesAndWaitForCompletion(threadPool);
                log.info("{} stopping; all exchanges have shut down", this);
            } catch (InterruptedException e) {
                log.info("{} stopping due to interrupt", this);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(this + " stopping due to uncaught exception", e);
            }
        } finally {
            threadPool.shutdownNow();
            updateSubscriptions(Collections.emptySet());
            log.info("{} stopped", this);
            lifecycleListener.onStopMain();
        }

    }

    /**
     * Запускает потоки для работы с биржами. Ожидаем результат выполнения потоков
     *
     * @param threadPool Пул потоков
     * @throws InterruptedException
     */
    private void submitExchangesAndWaitForCompletion(ExecutorService threadPool) throws InterruptedException {
        Map<String, Future<?>> futures = new HashMap<>();

        for (String exchange : exchangeService.getExchanges()) {
            futures.put(exchange, threadPool.submit(new Poller(exchange)));
        }

        for (Map.Entry<String, Future<?>> entry : futures.entrySet()) {
            try {
                entry.getValue().get();
            } catch (ExecutionException e) {
                log.error(entry.getKey() + "failed with uncaught exception and will not restart", e);
            }
        }
    }

    /**
     * Обновить подписчиков на тикер
     *
     * @param subscriptions Подписки на тип данных - определенный тикер
     */
    public void updateSubscriptions(Set<MarketDataSubscription> subscriptions) {
        ImmutableListMultimap<String, MarketDataSubscription> byExchange
                = Multimaps.index(subscriptions, s -> s.spec().exchange());

        for (String exchangeName : exchangeService.getExchanges()) {
            nextSubscriptions.get(exchangeName)
                    .set(ImmutableSet
                            .copyOf(byExchange.get(exchangeName))
                    );
        }

        int phase = phaser.arrive();
        log.debug("Progressing to phase {}", phase);
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

                while (!phaser.isTerminated()) {

                    // Прежде чем проверять наличие опросов, определить, на каком этапе
                    // мы будем ждать, если нет работы, то есть
                    // следующее пробуждение.
                    phase = phaser.getPhase();
                    if (phase == -1)
                        break;

                    loop();
                }

                log.info("{} shutting down due to termination", exchangeName);
            } catch (InterruptedException e) {
                log.info("{} shutting down due to inerrupt", exchangeName);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(exchangeName + " shutting down due to uncaught exception", e);
            } finally {
                lifecycleListener.onStop(exchangeName);
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
                            = exchange instanceof StreamingExchange ? (StreamingExchange) exchange : null;

                    this.accountService = accountServiceFactory.getForExchange(exchangeName);
                    this.marketDataService = exchange.getMarketDataService();
                    this.tradeService = tradeServiceFactory.getForExchange(exchangeName);
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
                suspend();
                return;
            }

            log.debug("{} - start poll", exchangeName);
            Set<String> balanceCurrencies = new HashSet<>();
            for (MarketDataSubscription subscription : polls) {
                if (phaser.isTerminated())
                    break;
                if (subscription.type().equals(MarketDataType.Balance)) {
                    balanceCurrencies.add(subscription.spec().base());
                    balanceCurrencies.add(subscription.spec().counter());
                } else {
                    fetchAndBroadcast(subscription);
                }
            }

            if (!phaser.isTerminated() && !balanceCurrencies.isEmpty()) {
                manageExchangeExceptions("Balances",
                        () -> featchBalances(balanceCurrencies)
                                .forEach(balance ->
                                        balanceOut.emit(
                                                BalanceEvent.create(
                                                        exchangeName,
                                                        balance.currency(),
                                                        balance
                                                ))),
                        () -> polls.stream()
                                .filter(s -> s.type()
                                        .equals(MarketDataType.Balance))
                                .collect(Collectors.toList())
                );
            }
        }

        private void suspend() throws InterruptedException {
            log.debug("{} - poll going to sleep", exchangeName);
            try {
                if (subscriptionsFailed) {
                    // FIXME awaitAdvanceInterruptibly - 1000
                    phaser.awaitAdvanceInterruptibly(phase, 1000, TimeUnit.MILLISECONDS);
                } else {
                    log.debug("{} - sleeping until phase {}", exchangeName, phase);
                    lifecycleListener.onBlocked(exchangeName);
                    phaser.awaitAdvanceInterruptibly(phase);
                    log.debug("{} - poll woken up on request", exchangeName);
                }
            } catch (TimeoutException e) {
                // fine
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failure in phaser wait for " + exchangeName, e);
            }
        }

        private Wallet wallet() throws IOException {
            exchangeService.rateController(exchangeName).acquire();
            Wallet wallet;
            wallet = accountService.getAccountInfo().getWallet();
            if (wallet == null) {
                throw new IllegalStateException("No wallet returned");
            }
            return wallet;
        }

        private Iterable<Balance> featchBalances(Collection<String> currencyCodes)
                throws IOException {
            Map<String, Balance> result = new HashMap<>();
            currencyCodes.stream().map(Balance::zero)
                    .forEach(balance -> result.put(balance.currency(), balance));
            wallet().getBalances().values().stream()
                    .filter(balance -> currencyCodes.contains(balance.getCurrency().getCurrencyCode()))
                    .map(Balance::create)
                    .forEach(balance -> result.put(balance.currency(), balance));
            return result.values();
        }

        private void manageExchangeExceptions(String dataDescription,
                                              CheckedExceptions.ThrowingRunnable runnable,
                                              Supplier<Iterable<MarketDataSubscription>> toUnsubscribe) {
            try {
                runnable.run();
            } catch (UnsupportedOperationException e) {
                log.warn("{} not available: {} ({})", dataDescription,
                        e.getClass().getSimpleName(), exceptionMessage(e));

                Iterables.addAll(unavailableSubscriptions, toUnsubscribe.get());

            } catch (SocketTimeoutException | SocketException
                    | ExchangeUnavailableException | SystemOverloadException | NonceException e) {
                log.warn("Throttling {} - {} ({}) when fetching {}", exchangeName,
                        e.getClass().getSimpleName(), exceptionMessage(e), dataDescription);
                exchangeService.rateController(exchangeName).throttle();
            } catch (HttpStatusIOException e) {
                handleHttpStatusException(dataDescription, e);
            } catch (RateLimitExceededException | FrequencyLimitExceededException e) {

                log.error("Hit rate limiting on {} when fetching {}. Backing off", exchangeName, dataDescription);
                notificationService.error("Getting rate limiting errors on " + exchangeName + ". Pausing access and will "
                        + "resume at a lower rate.");
                RateController rateController = exchangeService.rateController(exchangeName);
                rateController.backoff();
                rateController.pause();
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
                            case Order:
                                // TODO В настоящее время не поддерживается опросом
                                break;
                            default:
                                throw new IllegalStateException("Market data type " + subscription.type() + " not supported in this way");
                        }
                    },
                    () -> ImmutableSet.of(subscription));
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

                log.info("{} - updating subscriptions to: {} from {}", exchangeName, subscriptions, oldSubscriptions);

                // Отключите любые потоковые обмены,
                // на которых подписанные в настоящее время тикеры не соответствуют
                // тем, которые мы хотим.

                if (!oldSubscriptions.isEmpty()) {
                    disconnect();
                }

                // Очистите кэшированные тикеры и книги заказов для всего,
                // что мы отписались, чтобы мы не передавали устаревшие данные
                Sets.difference(oldSubscriptions, subscriptions)
                        .forEach(this::clearCacheForSubscription);

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
                    int arrivedPhase = phaser.arrive();
                    log.debug("Progressing to phase {}", arrivedPhase);
                }
                throw e;
            }
        }

        /**
         * Подписать слушателей к бирже
         *
         * Поитогу получаем новый массив слушателей которые должны
         * быть подключены при следующей попытке
         *
         * @param subscriptions Подписки
         */
        private void subscribe(Set<MarketDataSubscription> subscriptions) {

            Builder<MarketDataSubscription> pollingBuilder = ImmutableSet.builder();

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
            Builder<MarketDataSubscription> remainder = ImmutableSet.builder();
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
                                    .map(Balance::create)
                                    .map(balance -> BalanceEvent.create(exchangeName, balance.currency(), balance))
                                    .subscribe(balanceOut::emit,
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
            ProductSubscriptionBuilder builder = ProductSubscription.create();
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

        private Disposable connectSubscription(MarketDataSubscription subscription) {
            switch (subscription.type()) {
                // TODO Подписки к рынку данных
                default:
                    throw new NotAvailableFromExchangeException();
            }
        }

        /**
         * Очистить кэшированные тикеры и книги заказов
         *
         * @param subscription Описание слушателя
         */
        private void clearCacheForSubscription(MarketDataSubscription subscription) {
            balanceOut.removeFromCache(subscription.spec().exchange() + "/" + subscription.spec().base());
            balanceOut.removeFromCache(subscription.spec().exchange() + "/" + subscription.spec().counter());
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


    }

    /**
     * Решение проблемы с быстрым потоком данных
     * Сохранение поточной информации в случае если не успевает обработать слушатель
     *
     * @param <T>
     */
    private class PersistentPublisher<T> {
        private final Flowable<T> flowable;
        private final AtomicReference<FlowableEmitter<T>> emitter = new AtomicReference<>();

        public PersistentPublisher() {
            this.flowable = setup(
                    Flowable.create(
                            (FlowableEmitter<T> e) -> emitter.set(e.serialize()),
                            BackpressureStrategy.MISSING))
                    .share()
                    .onBackpressureLatest();
        }

        Flowable<T> setup(Flowable<T> base) {
            return base;
        }

        Flowable<T> getAll() {
            return flowable;
        }

        final void emit(T e) {
            if (emitter.get() != null) {
                emitter.get().onNext(e);
            }
        }
    }

    private final class CachingPersistentPublisher<T, U> extends PersistentPublisher<T> {
        private final ConcurrentMap<U, T> latest = Maps.newConcurrentMap();
        private final Function<T, U> keyFunction;
        private Function<Iterable<T>, Iterable<T>> initialSnapshotSortFunction;

        public CachingPersistentPublisher(Function<T, U> keyFunction) {
            super();
            this.keyFunction = keyFunction;
        }

        @Override
        Flowable<T> setup(Flowable<T> base) {
            return base.doOnNext(e -> latest.put(this.keyFunction.apply(e), e));
        }

        void removeFromCache(U key) {
            latest.remove(key);
        }

        void removeFromCache(Predicate<T> matcher) {
            Set<U> removals = new HashSet<>();
            latest.entrySet().stream()
                    .filter(e -> matcher.test(e.getValue()))
                    .map(Map.Entry::getKey)
                    .forEach(removals::add);
            removals.forEach(latest::remove);
        }
    }

    /**
     * Для тестирования.
     * Запускает сигналы в ключевых событиях, позволяя организовать тесты.
     */
    interface LifecycleListener {
        default void onBlocked(String exchange) {
        }

        default void onStop(String exchange) {
        }

        default void onStopMain() {
        }
    }
}
