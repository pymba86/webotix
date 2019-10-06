package ru.webotix.market.data;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.ExchangeService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class MarketDataSubscriptionManager extends AbstractExecutionThreadService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeEventBus.class);

    private final ExchangeService exchangeService;

    private final Map<String, AtomicReference<Set<MarketDataSubscription>>> nextSubscriptions;

    private final CachingPersistentPublisher<BalanceEvent, String> balanceOut;

    private final Phaser phaser = new Phaser(1);

    private LifecycleListener lifecycleListener = new LifecycleListener() {
    };

    @Inject
    public MarketDataSubscriptionManager(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;

        this.nextSubscriptions = exchangeService.getExchanges()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(), e -> new AtomicReference<>()
                ));

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
                exchangeService.getExchanges().size()
        );

        try {
            try {

            }
        } finally {
            threadPool.shutdownNow();
            updateSubscriptions(Collections.emptySet());
            log.info("{} stopped", this);
            lifecycleListener.onStopMain();
        }

    }

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
