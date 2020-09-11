package ru.webotix.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.base.exception.WebotixAbortException;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.job.spi.Job;
import ru.webotix.job.spi.JobRunConfiguration;

import java.util.concurrent.CountDownLatch;

/**
 * Фоновый сервис, который следит за заданиями.
 * Запускает задания из списка в базе, в случае если еще не запушен
 * Останавливает все запушенные задания, при остановке приложения
 */
@Singleton
public class GuardianLoop extends AbstractExecutionThreadService {

    private static final Logger log = LoggerFactory.getLogger(GuardianLoop.class);

    private final JobAccess jobAccess;
    private final JobRunner jobRunner;
    private final EventBus eventBus;
    private final Transactionally transactionally;
    private final Provider<SessionFactory> sessionFactory;
    private final RateLimiter rateLimiter;

    private volatile boolean kill;
    private final CountDownLatch killed = new CountDownLatch(1);

    @Inject
    GuardianLoop(
            JobAccess jobAccess,
            JobRunner jobRunner,
            EventBus eventBus,
            JobRunConfiguration configuration,
            Transactionally transactionally,
            Provider<SessionFactory> sessionFactory
    ) {
        this.jobAccess = jobAccess;
        this.jobRunner = jobRunner;
        this.eventBus = eventBus;
        this.transactionally = transactionally;
        this.sessionFactory = sessionFactory;

        this.rateLimiter = RateLimiter.create(
                2.0D / configuration.getGuardianLoopSeconds());
    }

    @Override
    protected void run() {
        Thread.currentThread().setName("Guardian loop");
        log.info("{} started", this);
        while (isRunning() && !kill) {
            try {

                if (Thread.currentThread().isInterrupted()) {
                    throw new WebotixAbortException("thread interrupted");
                }

                rateLimiter.acquire();

                log.debug("{} checking and restarting jobs", this);

                checkSessionFactoryState();

                lockAndStartInactiveJobs();

                rateLimiter.acquire();

                log.debug("{} refreshing locks", this);

                checkSessionFactoryState();

                eventBus.post(KeepAliveEvent.INSTANCE);


            } catch (WebotixAbortException e) {
                log.info("{} shutting down: {}", this, e.getMessage());
                break;
            } catch (Exception e) {
                log.error("Error in keep-alive loop");
            }
        }

        if (kill) {
            killed.countDown();
            log.warn("{} killed (should only ever happen in test code", this);
        } else {
            eventBus.post(StopEvent.INSTANCE);
            log.info("{} stopped", this);
        }
    }

    private void lockAndStartInactiveJobs() {
        boolean foundJobs = false;
        for (Job job : transactionally.call(jobAccess::list)) {
            foundJobs = true;
            try {
                transactionally.callChecked(
                        () -> {
                            jobRunner.submitExisting(job);
                            return null;
                        }
                );
            } catch (Exception e) {
                log.error("Failed to start job [" + job + "]", e);
            }
        }

        if (!foundJobs) {
            log.debug("Nothing running");
        }
    }

    private void checkSessionFactoryState() throws WebotixAbortException {
        if (sessionFactory.get().isClosed()) {
            throw new WebotixAbortException("session factory closed");
        }
    }

    @Override
    protected String serviceName() {
        return getClass().getSimpleName() + "[" + System.identityHashCode(this) + "]";
    }

    @VisibleForTesting
    void kill() throws InterruptedException {
        kill = true;
        killed.await();
    }
}
