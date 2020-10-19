package ru.webotix.job;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.hibernate.BaseSessionEventListener;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.job.api.*;
import ru.webotix.job.status.api.JobStatusService;
import ru.webotix.job.status.api.Status;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Singleton
class JobRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);

    private final JobLocker jobLocker;
    private final JobAccess jobAccess;
    private final Injector injector;
    private final JobStatusService jobStatusService;
    private final Transactionally transactionally;
    private final EventBus eventBus;
    private final UUID uuid;
    private final Provider<SessionFactory> sessionFactory;
    private final ExecutorService executorService;

    @Inject
    JobRunner(
            JobLocker jobLocker,
            JobAccess jobAccess,
            Injector injector,
            EventBus eventBus,
            JobStatusService jobStatusService,
            Transactionally transactionally,
            ExecutorService executorService,
            Provider<SessionFactory> sessionFactory
    ) {
        this.jobLocker = jobLocker;
        this.jobAccess = jobAccess;
        this.injector = injector;
        this.jobStatusService = jobStatusService;
        this.transactionally = transactionally;
        this.sessionFactory = sessionFactory;
        this.executorService = executorService;
        this.eventBus = eventBus;

        this.uuid = UUID.randomUUID();
    }

    /**
     * Отправить сущетсвующие задание в пулл
     *
     * @param job Задание
     * @return Задание можно заблокировать и выполнить
     */
    public boolean submitExisting(Job job) {
        if (jobLocker.attemptLock(job.id(), uuid)) {
            Job loadJob = jobAccess.load(job.id());
            startAfterCommit(loadJob);
            return true;
        }
        return false;
    }

    private void startAfterCommit(Job job) {
        sessionFactory.get()
                .getCurrentSession()
                .addEventListeners(
                        new BaseSessionEventListener() {
                            @Override
                            public void transactionCompletion(boolean successful) {
                                if (successful) {
                                    executorService.execute(
                                            () -> new JobLifeTimeManager(job).start()
                                    );
                                }
                            }
                        }
                );
    }

    /**
     * Попытка вставить и запустить новое задание
     * <p>
     * Учитывая, что вставка в базу данных гарантирует, что она будет
     * запущена в какой-то момент, обеспечивает возможность подтвердить это с
     * помощью обратного вызова перед фактическим запуском. Это можно использовать для
     * подтвердить восходящий запрос.
     * <p>
     * Запрос игнорируется (и вызывается обратный вызов), если задание уже создано, чтобы
     * избегать двойных звонков.
     *
     * @param job    Задание
     * @param ack    Обратный вызов, в случае успеха
     * @param reject Обратный вызов, если вставка не удалась
     * @throws Exception Если возникли какие либо ошибки
     */
    public void submitNew(Job job, ExceptionThrowingRunnable ack,
                          ExceptionThrowingRunnable reject) throws Exception {

        createJob(job, ack, reject);

        if (!attemptLock(job, reject)) {
            throw new RuntimeException("Created but could not immediately lock new job");
        }

        startAfterCommit(job);
    }

    private boolean attemptLock(Job job, ExceptionThrowingRunnable reject)
            throws Exception {
        boolean locked;
        try {
            locked = jobLocker.attemptLock(job.id(), uuid);
        } catch (Exception t) {
            reject.run();
            log.warn("Job {} could not be locked. Request reject.", job.id());
            throw t;
        }
        return locked;
    }

    private void createJob(Job job, ExceptionThrowingRunnable ack,
                           ExceptionThrowingRunnable reject) throws Exception {

        try {
            jobAccess.insert(job);
        } catch (JobAccess.JobAlreadyExistsException e) {
            log.info("Job {} already exists. Request ignored.", job.id());
            ack.run();
            throw e;
        } catch (Exception t) {
            reject.run();
            throw t;
        }

        ack.run();

    }

    public interface ExceptionThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Состояние задания
     */
    private enum JobStatus {
        CREATED,
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }

    private final class JobLifeTimeManager implements JobControl {

        private final JobProcessor<Job> processor;

        private volatile Job job;
        private volatile JobStatus status = JobStatus.CREATED;

        JobLifeTimeManager(Job job) {

            this.job = job;

            processor = JobProcessor
                    .createProcessor(job, this, injector);
        }

        private synchronized void start() {

            // Убеждаемся, что это этот менеджер можно использовать только один раз
            if (!status.equals(JobStatus.CREATED)) {
                throw new IllegalStateException(
                        "Job lifecycle status indicates re-use of lifetime manager: " + job);
            }

            status = JobStatus.STARTING;

            log.info("{} starting...", job);

            // Попытка запустить задачу
            Status result = safeStart();
            // Отправить ссообшение об изменении статуса задачи
            jobStatusService.status(job.id(), result);

            switch (result) {
                case FAILURE_PERMANENT:
                case SUCCESS:
                    // Если задание завершено или окончательно не выполнено, удалите его. Работа
                    // сам по себе может быть транзакционным, если он не работает с внешними
                    // ресурсы, чтобы разрешить выполнение вложенной транзакции.
                    log.debug("{} finished immediately ({}), cleaning up", job, result);

                    transactionally.allowingNested()
                            .run(() -> jobAccess.delete(job.id()));

                    safeStop();
                    status = JobStatus.STOPPED;
                    log.debug("{} cleaned up", job);
                    break;
                case FAILURE_TRANSIENT:
                    log.warn("{}: temporary failure. Sending back to queue for retry", job);
                    safeStop();
                    log.debug("{} cleaned up", job);
                    break;
                case RUNNING:
                    register();
                    break;
                default:
                    throw new IllegalStateException("Unknown job status " + result);
            }
        }

        @Override
        public void replace(Job newVersion) {
            Preconditions.checkNotNull(newVersion, "Job replaced with null");

            log.debug("{} replacing...", newVersion);

            if (!JobStatus.RUNNING.equals(status)
                    && !JobStatus.STARTING.equals(status)) {
                log.warn("Illegal state",
                        new IllegalStateException(
                                "Replacement of job which is already shutting down. Status="
                                        + status
                                        + ", job="
                                        + newVersion));
                return;
            }

            transactionally.allowingNested().run(
                    () -> jobAccess.update(newVersion)
            );

            job = newVersion;
            processor.setReplacedJob(newVersion);

            log.debug("{} replaced", newVersion);
        }

        @Override
        public synchronized void finish(Status status) {
            Preconditions.checkArgument(
                    status == Status.FAILURE_PERMANENT
                            || status == Status.SUCCESS,
                    "Finish condition must be success or permanent failure"
            );

            log.info("{} finishing ({})...", job, status);
            jobStatusService.status(job.id(), status);
            if (!stopAndUnregister()) {
                log.warn("Finish of job which is already shutting down. Status={}, job={}",
                        this.status, job);
                return;
            }

            transactionally.allowingNested().run(() -> jobAccess.delete(job.id()));
            log.info("{} finished", job);
        }

        private boolean stopAndUnregister() {
            if (status.equals(JobStatus.RUNNING)) {
                status = JobStatus.STOPPING;
                safeStop();
                eventBus.unregister(this);
                status = JobStatus.STOPPED;
                return true;
            } else if (status.equals(JobStatus.STARTING)) {
                status = JobStatus.STOPPED;
                return true;
            } else {
                return false;
            }
        }

        private void register() {
            if (status.equals(JobStatus.STARTING)) {
                status = JobStatus.RUNNING;
                eventBus.register(this);
                log.info("{} started", job);
            }
        }

        private void safeStop() {
            try {
                processor.stop();
            } catch (Exception e) {
                log.error("Error in stop for job [{}]. " +
                        "Cleanup may not be complete", e, e);
            }
        }

        private Status safeStart() {
            Status result;
            try {
                result = processor.start();
            } catch (Exception e) {
                log.error("Error in start for job [{}].", e, e);
                result = Status.FAILURE_TRANSIENT;
            }
            return result;
        }

        @Subscribe
        public synchronized void onKeepAlive(KeepAliveEvent keepAlive) {
            log.debug("{} checking lock...", job);
            if (!status.equals(JobStatus.RUNNING)) return;
            log.debug("{} updating lock...", job);
            if (!transactionally.call(() -> jobLocker.updateLock(job.id(), uuid))) {
                log.debug("{} stopping due to loss of lock...", job);
                if (stopAndUnregister()) log.debug("{} stopped due to loss of lock", job);
            }
        }

        @Subscribe
        public synchronized void stop(StopEvent stop) {
            log.debug("{} stopping due to shutdown", job);
            if (!stopAndUnregister()) {
                log.warn("Stop of job which is already shutting down. Status={}, job={}", status, job);
                return;
            }
            transactionally.allowingNested().run(() -> jobLocker.releaseLock(job.id(), uuid));
            log.debug("{} stopped due to shutdown", job);
        }
    }
}
