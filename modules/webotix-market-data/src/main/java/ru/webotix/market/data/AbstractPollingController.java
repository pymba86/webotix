package ru.webotix.market.data;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.wiring.BackgroundProcessingConfiguration;
import ru.webotix.market.data.api.SubscriptionController;

import java.util.Collections;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract class AbstractPollingController extends AbstractExecutionThreadService
        implements SubscriptionController {

    protected final Logger log = LoggerFactory.getLogger(AbstractPollingController.class);

    private final BackgroundProcessingConfiguration configuration;

    private final Phaser phaser = new Phaser(1);

    protected final SubscriptionPublisher publisher;

    private final LifecycleListener lifecycleListener = new LifecycleListener() {
    };

    protected AbstractPollingController(BackgroundProcessingConfiguration configuration,
                                        SubscriptionPublisher publisher) {
        this.configuration = configuration;
        this.publisher = publisher;
        this.publisher.setController(this);
    }

    @Override
    protected void run() {

        Thread.currentThread()
                .setName(AbstractPollingController.class.getSimpleName());

        log.info("{} started", this);

        try {
            doRun();
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        } catch (InterruptedException e) {
            log.info("{} stopping due to interrupt", this);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error(this + " stopping due to uncaught exception", e);
        } finally {
            updateSubscriptions(Collections.emptySet());
            log.info("{} stopped", this);
            lifecycleListener.onStopMain();
        }

    }

    protected abstract void doRun() throws InterruptedException;

    protected void wake() {
        int phase = phaser.arrive();
        log.debug("Progressing to phase {}", phase);
    }

    protected boolean isTerminated() {
        return phaser.isTerminated();
    }

    protected int getPhase() {
        return phaser.getPhase();
    }

    protected void subtaskStopped(String subTaskName) {
        lifecycleListener.onStop(subTaskName);
    }

    @Override
    protected void triggerShutdown() {
        super.triggerShutdown();
        phaser.arriveAndDeregister();
        phaser.forceTermination();
    }

    protected void suspend(String subTaskName, int phase, boolean failed)
            throws InterruptedException {
        log.debug("{} - poll going to sleep", subTaskName);
        try {
            if (failed) {
                long defaultSleep = (long) configuration.getLoopSeconds() * 1000;
                phaser.awaitAdvanceInterruptibly(phase, defaultSleep, TimeUnit.MILLISECONDS);
            } else {
                log.debug("{} - sleeping until phase {}", subTaskName, phase);
                lifecycleListener.onBlocked(subTaskName);
                phaser.awaitAdvanceInterruptibly(phase);
                log.debug("{} - poll woken up on request", subTaskName);
            }
        } catch (TimeoutException e) {
            // fine
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failure in phaser wait for " + subTaskName, e);
        }
    }
}
