package ru.webotix.processors;

import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.exchange.api.ExchangeEventRegistry;
import ru.webotix.exchange.api.ExchangeService;
import ru.webotix.job.api.Job;
import ru.webotix.job.api.JobControl;
import ru.webotix.job.api.JobSubmitter;
import ru.webotix.job.status.api.JobStatusService;
import ru.webotix.job.status.api.Status;
import ru.webotix.market.data.api.MarketDataSubscription;
import ru.webotix.market.data.api.MarketDataType;
import ru.webotix.market.data.api.TickerEvent;
import ru.webotix.market.data.api.TickerSpec;
import ru.webotix.notification.api.Notification;
import ru.webotix.notification.api.NotificationLevel;
import ru.webotix.notification.api.NotificationService;
import ru.webotix.utils.SafelyClose;
import ru.webotix.utils.SafelyDispose;

import java.util.concurrent.atomic.AtomicBoolean;

import static ru.webotix.job.status.api.Status.SUCCESS;
import static ru.webotix.utils.MoreBigDecimals.stripZeros;

public class OneCancelsOtherProcessor implements OneCancelsOther.Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftTrailingStopProcessor.class);

    private final JobSubmitter jobSubmitter;
    private final JobStatusService statusUpdateService;
    private final NotificationService notificationService;
    private final ExchangeEventRegistry exchangeEventRegistry;
    private final JobControl jobControl;
    private final ExchangeService exchangeService;
    private final Transactionally transactionally;

    private final RateLimiter validationTick = RateLimiter.create(0.05);

    private volatile OneCancelsOther job;
    private volatile boolean done;

    private ExchangeEventRegistry.ExchangeEventSubscription subscription;
    private Disposable disposable;

    @AssistedInject
    OneCancelsOtherProcessor(
            @Assisted OneCancelsOther job,
            @Assisted JobControl jobControl,
            JobSubmitter jobSubmitter,
            JobStatusService statusUpdateService,
            NotificationService notificationService,
            ExchangeEventRegistry exchangeEventRegistry,
            ExchangeService exchangeService,
            Transactionally transactionally) {
        this.job = job;
        this.jobControl = jobControl;
        this.jobSubmitter = jobSubmitter;
        this.statusUpdateService = statusUpdateService;
        this.notificationService = notificationService;
        this.exchangeEventRegistry = exchangeEventRegistry;
        this.exchangeService = exchangeService;
        this.transactionally = transactionally;
    }

    @Override
    public Status start() {
        if (!exchangeService.exchangeSupportsPair(
                job.tickTrigger().exchange(), job.tickTrigger().currencyPair())) {
            notificationService.error("Cancelling job as currency no longer supported: " + job);
            return Status.FAILURE_PERMANENT;
        }
        subscription =
                exchangeEventRegistry.subscribe(
                        MarketDataSubscription.create(
                                job.tickTrigger(), MarketDataType.Ticker));

        disposable = subscription.getTickers().subscribe(this::tick);
        return Status.RUNNING;
    }

    @Override
    public void setReplacedJob(OneCancelsOther job) {
        this.job = job;
    }

    @Override
    public void stop() {
        SafelyDispose.of(disposable);
        SafelyClose.the(subscription);
    }

    private synchronized void tick(TickerEvent tickerEvent) {
        try {
            if (!done) tickInner(tickerEvent);
        } catch (Exception t) {
            String message =
                    String.format(
                            "One-cancels-other on %s %s/%s market temporarily failed with error: %s",
                            job.tickTrigger().exchange(),
                            job.tickTrigger().base(),
                            job.tickTrigger().counter(),
                            t.getMessage());
            LOGGER.error(message, t);
            statusUpdateService.status(job.id(), Status.FAILURE_TRANSIENT);
            notificationService.error(message, t);
        }
    }

    private void tickInner(TickerEvent tickerEvent) {

        final Ticker ticker = tickerEvent.ticker();

        if (validationTick.tryAcquire() && !validateJobs()) return;

        OneCancelsOther.ThresholdAndJob low = job.low();

        OneCancelsOther.ThresholdAndJob high = job.high();

        if (low != null && ticker.getBid().compareTo(low.threshold()) <= 0) {

            transactionally.run(
                    () -> {
                        notificationService.send(Notification.create(
                                String.format(
                                        "One-cancels-other on %s %s/%s market hit low threshold (%s < %s)",
                                        job.tickTrigger().exchange(),
                                        job.tickTrigger().base(),
                                        job.tickTrigger().counter(),
                                        stripZeros(ticker.getBid()).toPlainString(),
                                        stripZeros(low.threshold()).toPlainString()),
                                NotificationLevel.Alert));

                        jobSubmitter.submitNewUnchecked(low.job());
                        done = true;
                        jobControl.finish(SUCCESS);
                    });

        } else if (high != null && ticker.getBid().compareTo(high.threshold()) >= 0) {

            transactionally.run(
                    () -> {
                        notificationService.send(Notification.create(
                                String.format(
                                        "One-cancels-other on %s %s/%s market hit high threshold (%s > %s)",
                                        job.tickTrigger().exchange(),
                                        job.tickTrigger().base(),
                                        job.tickTrigger().counter(),
                                        stripZeros(ticker.getBid()).toPlainString(),
                                        stripZeros(high.threshold()).toPlainString()),
                                NotificationLevel.Alert));

                        jobSubmitter.submitNewUnchecked(high.job());
                        done = true;
                        jobControl.finish(SUCCESS);
                    });
        }
    }

    private boolean validateJobs() {
        LOGGER.debug("Validating {}", job);
        AtomicBoolean success = new AtomicBoolean(true);

        OneCancelsOther.ThresholdAndJob low = job.low();

        if (low != null) {
            jobSubmitter.validate(
                    low.job(),
                    new JobControl() {

                        @Override
                        public void replace(Job job) {
                            jobControl.replace(
                                    OneCancelsOtherProcessor.this.job.toBuilder()
                                            .low(OneCancelsOther.ThresholdAndJob.create(
                                                    low.threshold(), job))
                                            .build());
                        }

                        @Override
                        public void finish(Status status) {
                            notificationService.error(
                                    "Cancelling one-cancels-other due to validation failure on low job");
                            jobControl.finish(status);
                            success.set(false);
                        }
                    });
        }


        OneCancelsOther.ThresholdAndJob high = job.high();

        if (high != null) {
            jobSubmitter.validate(
                    high.job(),
                    new JobControl() {

                        @Override
                        public void replace(Job job) {
                            jobControl.replace(
                                    OneCancelsOtherProcessor.this.job.toBuilder()
                                            .high(OneCancelsOther.ThresholdAndJob.create(
                                                    high.threshold(), job))
                                            .build());
                        }

                        @Override
                        public void finish(Status status) {
                            notificationService.error(
                                    "Cancelling one-cancels-other due to validation failure on high job");
                            jobControl.finish(status);
                            success.set(false);
                        }
                    });
        }
        return success.get();
    }

    public static final class Module extends AbstractModule {
        @Override
        protected void configure() {
            install(
                    new FactoryModuleBuilder()
                            .implement(OneCancelsOther.Processor.class, OneCancelsOtherProcessor.class)
                            .build(OneCancelsOther.Processor.ProcessorFactory.class));
        }
    }
}
