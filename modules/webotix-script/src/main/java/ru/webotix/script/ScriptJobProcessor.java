package ru.webotix.script;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import groovy.lang.Closure;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import jdk.nashorn.api.scripting.JSObject;
import org.codehaus.groovy.runtime.MethodClosure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.job.api.JobControl;
import ru.webotix.job.status.api.Status;
import ru.webotix.market.data.api.*;
import ru.webotix.notification.api.NotificationService;
import ru.webotix.utils.Hasher;
import ru.webotix.utils.SafelyClose;
import ru.webotix.utils.SafelyDispose;

import javax.script.*;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ru.webotix.job.status.api.Status.FAILURE_PERMANENT;
import static ru.webotix.job.status.api.Status.SUCCESS;

class ScriptJobProcessor implements ScriptJob.Processor {

    private static final String PERMANENTLY_FAILED = "' permanently failed: ";
    private static final String SCRIPT_JOB_PREFIX = "Script job '";

    private static final Logger log = LoggerFactory.getLogger(ScriptJobProcessor.class);

    private final JobControl jobControl;
    private ScriptEngine engine;

    private final ExchangeEventRegistry exchangeEventRegistry;
    private final NotificationService notificationService;
    private final Transactionally transactionally;
    private final Hasher hasher;
    private final ScriptConfiguration configuration;

    private volatile ScriptJob job;
    private volatile boolean done;

    @AssistedInject
    ScriptJobProcessor(
            @Assisted ScriptJob job,
            @Assisted JobControl jobControl,
            ExchangeEventRegistry exchangeEventRegistry,
            NotificationService notificationService,
            Transactionally transactionally,
            Hasher hasher,
            ScriptConfiguration configuration) {
        this.job = job;
        this.jobControl = jobControl;
        this.exchangeEventRegistry = exchangeEventRegistry;
        this.notificationService = notificationService;
        this.transactionally = transactionally;
        this.hasher = hasher;
        this.configuration = configuration;
    }

    @Override
    public Status start() {

        String hash = hasher.hashWithString(job.script(), configuration.getScriptSigningKey());
        if (!hash.equals(job.scriptHash())) {
            notifyAndLogError(SCRIPT_JOB_PREFIX + job.name() + "' has invalid hash. Failed permanently");
            return FAILURE_PERMANENT;
        }
        try {
            initialiseEngine();
        } catch (Exception e) {
            notificationService.error(
                    SCRIPT_JOB_PREFIX + job.name() + PERMANENTLY_FAILED + e.getMessage(), e);
            log.error("Failed script:\n{}", job.script());
            return FAILURE_PERMANENT;
        }
        try {
            Invocable invocable = (Invocable) engine;
            Status status = (Status) invocable.invokeFunction("start");

            if (Objects.isNull(status)) {
                return FAILURE_PERMANENT;
            } else {
                return status;
            }

        } catch (NoSuchMethodException e) {
            notificationService.error(
                    SCRIPT_JOB_PREFIX + job.name() + PERMANENTLY_FAILED + e.getMessage(), e);
            log.error("Failed script:\n{}", job.script());
            return FAILURE_PERMANENT;
        } catch (Exception e) {
            notifyAndLogError(
                    SCRIPT_JOB_PREFIX + job.name() + "' failed and will retry: " + e.getMessage(), e);

            return FAILURE_PERMANENT;
        }
    }

    private void initialiseEngine() throws ScriptException {
        engine = new ScriptEngineManager().getEngineByName("Groovy");

        Bindings bindings = engine.createBindings();

        bindings.put("SUCCESS", SUCCESS);
        bindings.put("FAILURE_PERMANENT", FAILURE_PERMANENT);
        bindings.put("RUNNING", Status.RUNNING);

        bindings.put("notifications", notificationService);

        bindings.put("events", new Events());
        bindings.put("control", new Control());
        bindings.put("console", new Console());
        bindings.put("state", new State());

        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        engine.eval(job.script());
    }

    @Override
    public void stop() {
        if (engine == null) return;
        Invocable invocable = (Invocable) engine;
        try {
            invocable.invokeFunction("stop");
        } catch (NoSuchMethodException e) {
            // Fine
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setReplacedJob(ScriptJob job) {
        this.job = job;
    }

    public final class Console {

        public void log(Object o) {
            log.info("{} - {}", job.name(), o);
        }

        public void log(Object o, Exception error) {
            log.error(job.name() + " - " + o, error);
        }

        public void log(Object o, Object value) {
            log.info("{} - {}, {}", job.name(), o, value);
        }

        @Override
        public String toString() {
            return "console";
        }
    }

    public interface StateManager<T> {
        T get(String key);

        void set(String key, T value);

        void remove(String key);

        void increment(String key);
    }

    public final class Control {

        public void fail() {
            done = true;
            throw new PermanentFailureException();
        }

        public void restart() {
            done = true;
            throw new TransientFailureException();
        }

        public void done() {
            done = true;
            jobControl.finish(SUCCESS);
            throw new ExitException();
        }

        @Override
        public String toString() {
            return "control";
        }
    }

    public final class Events {

        private final AtomicBoolean failing = new AtomicBoolean(false);

        public Disposable setTick(Closure<Void> callback, TickerSpec tickerSpec) {
            return onTick(
                    event -> processEvent(() -> callback.call(event)),
                    tickerSpec,
                    callback.toString());
        }

        public Disposable setBalance(MethodClosure callback, TickerSpec tickerSpec) {
            return onBalance(
                    event -> processEvent(() -> callback.call(event)),
                    tickerSpec,
                    callback.toString());
        }

        public Disposable setOpenOrders(MethodClosure callback, TickerSpec tickerSpec) {
            return onOpenOrders(
                    event -> processEvent(() -> callback.call(event)),
                    tickerSpec,
                    callback.toString());
        }

        public Disposable setOrderBook(MethodClosure callback, TickerSpec tickerSpec) {
            return onOrderBook(
                    event -> processEvent(() -> callback.call(event)),
                    tickerSpec,
                    callback.toString());
        }

        public Disposable setUserTrades(MethodClosure callback, TickerSpec tickerSpec) {
            return onUserTrades(
                    event -> processEvent(() -> callback.call(event)),
                    tickerSpec,
                    callback.toString());
        }

        public Disposable setInterval(Closure<Void> callback, Integer timeout) {
            return onInterval(
                    () -> processEvent(callback::call), timeout, callback.toString());
        }

        private void successfulPoll() {
            if (failing.compareAndSet(true, false)) {
                notificationService.alert(SCRIPT_JOB_PREFIX + job.name() + "' working again");
            }
        }

        private void failingPoll(Exception e) {
            if (failing.compareAndSet(false, true)) {
                notifyAndLogError(SCRIPT_JOB_PREFIX + job.name() + "' failing: " + e.getMessage(), e);
            } else {
                log.error("Script job '{}' failed again: {}", job.name(), e.getMessage());
            }
        }

        private synchronized void processEvent(Runnable runnable) {
            if (done) return;
            try {
                transactionally.run(
                        () -> {
                            try {
                                runnable.run();
                            } catch (ExitException e) {
                                // Fine. We're done
                            }
                        });
                successfulPoll();
            } catch (PermanentFailureException e) {
                notifyAndLogError(SCRIPT_JOB_PREFIX + job.name() + PERMANENTLY_FAILED + e.getMessage(), e);
                jobControl.finish(FAILURE_PERMANENT);
            } catch (Exception e) {
                failingPoll(e);
                jobControl.finish(FAILURE_PERMANENT);
            }
        }

        public void clearInterval(Disposable disposable) {
            dispose(disposable);
        }

        @Override
        public String toString() {
            return "events";
        }
    }

    void dispose(Disposable disposable) {
        SafelyDispose.of(disposable);
    }

    public final class State {

        public final StateManager<String> persistent =
                new StateManager<String>() {

                    @Override
                    public final void set(String key, String value) {
                        HashMap<String, String> newState = new HashMap<>(job.state());
                        newState.put(key, value);
                        jobControl.replace(job.toBuilder().state(newState).build());
                    }

                    @Override
                    public final String get(String key) {
                        return job.state().get(key);
                    }

                    @Override
                    public final void remove(String key) {
                        HashMap<String, String> newState = new HashMap<>(job.state());
                        newState.remove(key);
                        jobControl.replace(job.toBuilder().state(newState).build());
                    }

                    @Override
                    public final String toString() {
                        return job.state().toString();
                    }

                    @Override
                    public final void increment(String key) {
                        String value = get(key);
                        try {
                            long asLong = Long.parseLong(value);
                            set(key, Long.toString(asLong + 1));
                        } catch (NumberFormatException e) {
                            throw new IllegalStateException(
                                    key + " is not a precise numeric value, so cannot be incremented");
                        }
                    }
                };

        @Override
        public String toString() {
            return "state";
        }
    }

    Disposable onInterval(Runnable runnable, long timeout, String description) {

        Disposable result = Observable.interval(timeout, MILLISECONDS)
                .subscribe(x -> runnable.run(),
                        throwable -> log.error("Interval error: " + throwable.getMessage()));

        return new Disposable() {

            @Override
            public boolean isDisposed() {
                return result.isDisposed();
            }

            @Override
            public void dispose() {
                SafelyDispose.of(result);
            }

            @Override
            public String toString() {
                return description;
            }
        };
    }

    public static final class DisposableSubscription implements Disposable {
        private final ExchangeEventRegistry.ExchangeEventSubscription subscription;
        private final Disposable disposable;
        private final String description;

        public DisposableSubscription(
                ExchangeEventRegistry.ExchangeEventSubscription subscription, Disposable disposable, String description) {
            this.subscription = subscription;
            this.disposable = disposable;
            this.description = description;
        }

        @Override
        public boolean isDisposed() {
            return disposable.isDisposed();
        }

        @Override
        public void dispose() {
            SafelyDispose.of(disposable);
            SafelyClose.the(subscription);
        }

        @Override
        public String toString() {
            return description;
        }
    }

    public final DisposableSubscription onTick(
            io.reactivex.functions.Consumer<TickerEvent> handler,
            TickerSpec tickerSpec,
            String description) {

        ExchangeEventRegistry.ExchangeEventSubscription subscription =
                exchangeEventRegistry.subscribe(
                        MarketDataSubscription.create(tickerSpec, MarketDataType.Ticker));

        Disposable disposable = subscription.getTickers().subscribe(handler);

        return new DisposableSubscription(subscription, disposable, description);
    }

    public final DisposableSubscription onBalance(
            io.reactivex.functions.Consumer<BalanceEvent> handler,
            TickerSpec tickerSpec,
            String description) {

        ExchangeEventRegistry.ExchangeEventSubscription subscription =
                exchangeEventRegistry.subscribe(
                        MarketDataSubscription.create(tickerSpec, MarketDataType.Balance));

        Disposable disposable = subscription.getBalances().subscribe(handler);

        return new DisposableSubscription(subscription, disposable, description);
    }

    public final DisposableSubscription onOpenOrders(
            io.reactivex.functions.Consumer<OpenOrdersEvent> handler,
            TickerSpec tickerSpec,
            String description) {

        ExchangeEventRegistry.ExchangeEventSubscription subscription =
                exchangeEventRegistry.subscribe(
                        MarketDataSubscription.create(tickerSpec, MarketDataType.OpenOrders));

        Disposable disposable = subscription.getOrderSnapshots().subscribe(handler);

        return new DisposableSubscription(subscription, disposable, description);
    }

    public final DisposableSubscription onOrderBook(
            io.reactivex.functions.Consumer<OrderBookEvent> handler,
            TickerSpec tickerSpec,
            String description) {

        ExchangeEventRegistry.ExchangeEventSubscription subscription =
                exchangeEventRegistry.subscribe(
                        MarketDataSubscription.create(tickerSpec, MarketDataType.OrderBook));

        Disposable disposable = subscription.getOrderBooks().subscribe(handler);

        return new DisposableSubscription(subscription, disposable, description);
    }

    public final DisposableSubscription onUserTrades(
            io.reactivex.functions.Consumer<UserTradeEvent> handler,
            TickerSpec tickerSpec,
            String description) {

        ExchangeEventRegistry.ExchangeEventSubscription subscription =
                exchangeEventRegistry.subscribe(
                        MarketDataSubscription.create(tickerSpec, MarketDataType.UserTrade));

        Disposable disposable = subscription.getUserTrades().subscribe(handler);

        return new DisposableSubscription(subscription, disposable, description);
    }

    private void notifyAndLogError(String message) {
        notificationService.error(message);
    }

    private void notifyAndLogError(String message, Throwable t) {
        notificationService.error(message, t);
    }

    public static final class Module extends AbstractModule {
        @Override
        protected void configure() {
            install(
                    new FactoryModuleBuilder()
                            .implement(ScriptJob.Processor.class, ScriptJobProcessor.class)
                            .build(ScriptJob.Processor.ProcessorFactory.class));
        }
    }

    private static final class ExitException extends RuntimeException {
    }


    private static final class PermanentFailureException extends RuntimeException {

    }

    private static final class TransientFailureException extends RuntimeException {

    }

}
