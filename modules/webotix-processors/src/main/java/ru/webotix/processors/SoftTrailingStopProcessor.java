package ru.webotix.processors;

import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
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
import ru.webotix.notification.api.NotificationService;
import ru.webotix.processors.LimitOrderJob.Direction;
import ru.webotix.utils.SafelyClose;
import ru.webotix.utils.SafelyDispose;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static ru.webotix.job.status.api.Status.SUCCESS;

public class SoftTrailingStopProcessor implements SoftTrailingStop.Processor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftTrailingStopProcessor.class);

    private final JobStatusService jobStatusService;
    private final NotificationService notificationService;
    private final CurrencyPairMetaData currencyPairMetaData;
    private final JobSubmitter jobSubmitter;
    private final JobControl jobControl;
    private final ExchangeEventRegistry exchangeEventRegistry;
    private final Transactionally transactionally;

    private final RateLimiter validationTick = RateLimiter.create(0.05);

    private volatile boolean done;
    private volatile SoftTrailingStop job;

    private ExchangeEventRegistry.ExchangeEventSubscription subscription;
    private Disposable disposable;

    @Inject
    public SoftTrailingStopProcessor(
            @Assisted SoftTrailingStop job,
            @Assisted JobControl jobControl,
            final JobStatusService jobStatusService,
            final NotificationService notificationService,
            final ExchangeService exchangeService,
            final JobSubmitter jobSubmitter,
            final ExchangeEventRegistry exchangeEventRegistry,
            final Transactionally transactionally) {
        this.job = job;
        this.jobControl = jobControl;
        this.jobStatusService = jobStatusService;
        this.notificationService = notificationService;
        this.jobSubmitter = jobSubmitter;
        this.exchangeEventRegistry = exchangeEventRegistry;
        this.transactionally = transactionally;
        this.currencyPairMetaData = exchangeService.fetchCurrencyPairMetaData(job.tickTrigger());
    }

    @Override
    public Status start() {
        subscription =
                exchangeEventRegistry.subscribe(MarketDataSubscription.create(job.tickTrigger(),
                        MarketDataType.Ticker));

        disposable = subscription.getTickers().subscribe(this::tick);
        return Status.RUNNING;
    }

    @Override
    public void stop() {
        SafelyDispose.of(disposable);
        SafelyClose.the(subscription);
    }

    @Override
    public void setReplacedJob(SoftTrailingStop job) {
        this.job = job;
    }

    private synchronized void tick(TickerEvent tickerEvent) {
        try {
            if (!done) transactionally.run(() -> tickTransaction(tickerEvent));
        } catch (Exception t) {
            String message =
                    String.format(
                            "Trailing stop on %s %s/%s market temporarily failed with error: %s",
                            job.tickTrigger().exchange(),
                            job.tickTrigger().base(),
                            job.tickTrigger().counter(),
                            t.getMessage());
            LOGGER.error(message, t);
            jobStatusService.status(job.id(), Status.FAILURE_TRANSIENT, message);
        }
    }

    private void tickTransaction(TickerEvent tickerEvent) {

        final Ticker ticker = tickerEvent.ticker();
        final TickerSpec ex = job.tickTrigger();

        if (ticker.getAsk() == null) {
            jobStatusService.status(job.id(), Status.FAILURE_PERMANENT);
            notificationService.error(
                    String.format("Market %s/%s/%s has no sellers!", ex.exchange(), ex.base(), ex.counter()));
            return;
        }
        if (ticker.getBid() == null) {
            jobStatusService.status(job.id(), Status.FAILURE_PERMANENT);
            notificationService.error(
                    String.format("Market %s/%s/%s has no buyers!", ex.exchange(), ex.base(), ex.counter()));
            return;
        }

        BigDecimal triggerPrice = currencyScalePrice(
                job.stopPrice(), currencyPairMetaData);

        if (job.direction().equals(Direction.SELL)) {

            BigDecimal tickerPrice = currencyScalePrice(
                    ticker.getBid(), currencyPairMetaData);

            sellTick(tickerPrice, triggerPrice);

        } else if (job.direction().equals(Direction.BUY)) {

            BigDecimal tickerPrice = currencyScalePrice(
                    ticker.getAsk(), currencyPairMetaData);

            buyTick(tickerPrice, triggerPrice);
        }
    }

    private LimitOrderJob buildOrderJob() {

        BigDecimal limitPercentagePrice = job.lastSyncPrice()
                .multiply(job.limitPercentage());

        BigDecimal limitPrice = currencyScalePrice(
                limitPercentagePrice, currencyPairMetaData);

        LimitOrderJob limitOrderJob = LimitOrderJob.builder()
                .tickTrigger(job.tickTrigger())
                .direction(job.direction())
                .amount(job.amount())
                .balanceState(job.balanceState())
                .limitPrice(limitPrice)
                .build();

        if (validationTick.tryAcquire()) {
            validate(limitOrderJob);
        }

        return limitOrderJob;
    }


    private void sellTick(BigDecimal tickerPrice, BigDecimal triggerPrice) {

        if (tickerPrice.compareTo(triggerPrice) >= 0
                || job.lastSyncPrice().compareTo(triggerPrice) >= 0) {

            BigDecimal stopPercentagePrice = job.lastSyncPrice()
                    .multiply(job.stopPercentage());

            BigDecimal stopPrice = currencyScalePrice(
                    stopPercentagePrice, currencyPairMetaData);

            LimitOrderJob limitOrderJob = buildOrderJob();

            if (tickerPrice.compareTo(stopPrice) <= 0) {

                notificationService.alert(
                        String.format(
                                "Trailing stop on %s %s/%s market hit exit price (%s < %s)",
                                job.tickTrigger().exchange(),
                                job.tickTrigger().base(),
                                job.tickTrigger().counter(),
                                tickerPrice,
                                stopPrice)
                );

                jobSubmitter.submitNewUnchecked(limitOrderJob);

                jobControl.finish(SUCCESS);
                done = true;
                return;
            }

            if (tickerPrice.compareTo(job.lastSyncPrice()) > 0) {

                jobControl.replace(
                        job.toBuilder()
                                .lastSyncPrice(tickerPrice)
                                .build()
                );
            }
        }
    }

    private void buyTick(BigDecimal tickerPrice, BigDecimal triggerPrice) {

        if (tickerPrice.compareTo(triggerPrice) <= 0
                || job.lastSyncPrice().compareTo(triggerPrice) <= 0) {

            BigDecimal stopPercentagePrice = job.lastSyncPrice()
                    .multiply(job.stopPercentage());

            BigDecimal stopPrice = currencyScalePrice(
                    stopPercentagePrice, currencyPairMetaData);

            LimitOrderJob limitOrderJob = buildOrderJob();

            if (tickerPrice.compareTo(stopPrice) >= 0) {

                notificationService.alert(
                        String.format(
                                "Trailing stop on %s %s/%s market hit entry price (%s < %s)",
                                job.tickTrigger().exchange(),
                                job.tickTrigger().base(),
                                job.tickTrigger().counter(),
                                tickerPrice,
                                stopPrice)
                );

                jobSubmitter.submitNewUnchecked(limitOrderJob);

                jobControl.finish(SUCCESS);
                done = true;
                return;
            }

            if (tickerPrice.compareTo(job.lastSyncPrice()) < 0) {

                jobControl.replace(
                        job.toBuilder()
                                .lastSyncPrice(tickerPrice)
                                .build()
                );
            }
        }
    }

    private BigDecimal currencyScalePrice(
            BigDecimal price, CurrencyPairMetaData currencyPairMetaData) {
        return price.setScale(
                currencyPairMetaData.getPriceScale(), RoundingMode.HALF_UP);
    }

    private void validate(LimitOrderJob limitOrderJob) {
        jobSubmitter.validate(
                limitOrderJob,
                new JobControl() {

                    @Override
                    public void replace(Job replacement) {
                        jobControl.replace(
                                SoftTrailingStopProcessor.this.job.toBuilder()
                                        .balanceState(((LimitOrderJob) replacement).balanceState())
                                        .build());
                    }

                    @Override
                    public void finish(Status status) {
                        throw new UnsupportedOperationException();
                    }
                });
    }

    public static final class Module extends AbstractModule {
        @Override
        protected void configure() {
            install(
                    new FactoryModuleBuilder()
                            .implement(SoftTrailingStop.Processor.class, SoftTrailingStopProcessor.class)
                            .build(SoftTrailingStop.Processor.ProcessorFactory.class));
        }
    }
}
