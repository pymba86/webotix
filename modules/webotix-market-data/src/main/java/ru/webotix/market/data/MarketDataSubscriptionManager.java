package ru.webotix.market.data;

import com.google.common.collect.*;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import info.bitrich.xchangestream.core.StreamingExchange;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.AccountServiceFactory;
import ru.webotix.exchange.ExchangeService;
import ru.webotix.exchange.TradeServiceFactory;
import ru.webotix.exchange.info.TickerSpec;
import ru.webotix.utils.SafelyDispose;
import com.google.common.collect.ImmutableSet.Builder;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class MarketDataSubscriptionManager extends AbstractExecutionThreadService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeEventBus.class);

    private final ExchangeService exchangeService;
    private final AccountServiceFactory accountServiceFactory;
    private final TradeServiceFactory tradeServiceFactory;

    private final Map<String, AtomicReference<Set<MarketDataSubscription>>> nextSubscriptions;
    private final ConcurrentMap<String, Set<MarketDataSubscription>> subscriptionsPerExchange = Maps.newConcurrentMap();
    private final ConcurrentMap<String, Set<MarketDataSubscription>> pollsPerExchange = Maps.newConcurrentMap();
    private final Multimap<String, Disposable> disposablesPerExchange = HashMultimap.create();

    private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;

    private final ConcurrentMap<TickerSpec, Instant> mostRecentTrades = Maps.newConcurrentMap();

    private final Phaser phaser = new Phaser(1);

    private LifecycleListener lifecycleListener = new LifecycleListener() {
    };

    @Inject
    public MarketDataSubscriptionManager(ExchangeService exchangeService,
                                         AccountServiceFactory accountServiceFactory,
                                         TradeServiceFactory tradeServiceFactory) {
        this.exchangeService = exchangeService;
        this.accountServiceFactory = accountServiceFactory;
        this.tradeServiceFactory = tradeServiceFactory;

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

        private void loop() {
            // Проверьте, есть ли изменение подписки в очереди. Если так, примените это
            doSubscriptionChanges();

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
         * @param subscriptions
         */
        private void subscribe(Set<MarketDataSubscription> subscriptions) {
            Builder<MarketDataSubscription> pollingBuilder = ImmutableSet.builder();

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
