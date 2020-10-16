package ru.webotix.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.datasource.database.ConnectionSource;
import ru.webotix.datasource.database.Transactionally;
import ru.webotix.utils.SafelyDispose;

import static java.time.LocalDateTime.now;
import static java.time.ZoneOffset.UTC;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class RecordJobLocker implements JobLocker, Managed {

    private static final Logger log = LoggerFactory.getLogger(RecordJobLocker.class);

    private static final org.jooq.Table<Record> TABLE = DSL.table(JobLockContribution.TABLE_NAME);
    private static final Field<Object> OWNER_ID_FIELD = DSL.field(JobLockContribution.OWNER_ID);
    private static final Field<Object> JOB_ID_FIELD = DSL.field(JobLockContribution.JOB_ID);
    private static final Field<Long> EXPIRES_FIELD = DSL.field(JobLockContribution.EXPIRES, Long.class);

    private final JobRunConfiguration configuration;

    private Disposable interval;

    private final Transactionally transactionally;
    private final ConnectionSource connectionSource;

    @Inject
    RecordJobLocker(
            JobRunConfiguration configuration,
            ConnectionSource connectionSource,
            Transactionally transactionally
    ) {
        this.configuration = configuration;
        this.connectionSource = connectionSource;
        this.transactionally = transactionally;
    }


    @Override
    public void start()  {
        interval = Observable
                .interval(configuration.getGuardianLoopSeconds(), TimeUnit.SECONDS)
                .doOnError(throwable -> log.error("Interval error: " + throwable.getMessage()))
                .subscribe(x -> transactionally.run(() -> cleanup(now())));
    }

    @VisibleForTesting
    void cleanup(LocalDateTime localDateTime) {

        long expiry = localDateTime.toEpochSecond(UTC);

        int deleted = connectionSource.getWithCurrentConnection(
                dsl -> dsl
                        .deleteFrom(TABLE)
                        .where(EXPIRES_FIELD.lessOrEqual(expiry))
                        .execute()
        );

        if (deleted != 0) {
            log.info("Expired {} locks on active jobs", deleted);
        }
    }

    @Override
    public void stop() {
        SafelyDispose.of(interval);
    }

    @Override
    public boolean attemptLock(String jobId, UUID uuid) {
        return attemptLock(jobId, uuid, LocalDateTime.now());
    }

    @Override
    public boolean updateLock(String jobId, UUID uuid) {
        return updateLock(jobId, uuid, LocalDateTime.now());
    }

    @VisibleForTesting
    boolean updateLock(String jobId, UUID uuid, LocalDateTime dateTime) {

        log.debug("Updating lock on {} for {}", jobId, uuid);

        return connectionSource.getWithCurrentConnection(
                dsl -> dsl.update(TABLE)
                        .set(EXPIRES_FIELD, newExpiryDate(dateTime))
                        .where(fullKeyMatch(jobId, uuid))
                        .execute()
        ) != 0;
    }

    @VisibleForTesting
    boolean attemptLock(String jobId, UUID uuid, LocalDateTime dateTime) {
        try {

            log.debug("Attempting to lock {} for {}", jobId, uuid);

            boolean result =
                    connectionSource.getWithCurrentConnection(
                            dsl -> dsl.insertInto(TABLE)
                                    .values(jobId, uuid, newExpiryDate(dateTime))
                                    .execute()) == 1;

            if (result) {
                log.debug("Locked {} for {}", jobId, uuid);
            } else {
                log.debug("Failed to lock {} for {}", jobId, uuid);
            }

            return result;

        } catch (DataAccessException e) {

            log.debug("Failed to lock {} for {}", jobId, uuid);

            return false;
        }
    }

    private long newExpiryDate(LocalDateTime dateTime) {
        return dateTime.plusSeconds(configuration.getDatabaseLockSeconds())
                .toEpochSecond(UTC);
    }

    private Condition fullKeyMatch(String jobId, UUID uuid) {
        return JOB_ID_FIELD.eq(jobId).and(OWNER_ID_FIELD.eq(uuid.toString()));
    }

    @Override
    public void releaseLock(String jobId, UUID uuid) {

        log.debug("Releasing lock on {} by {}", jobId, uuid);

        connectionSource.withCurrentConnection(
                dsl -> dsl.delete(TABLE)
                        .where(fullKeyMatch(jobId, uuid)).execute()
        );
    }

    @Override
    public void releaseAnyLock(String jobId) {
        connectionSource.withCurrentConnection(
                dsl -> dsl.delete(TABLE)
                        .where(JOB_ID_FIELD.eq(jobId))
                        .execute()
        );
    }

    @Override
    public void releaseAllLocks() {
        connectionSource.withCurrentConnection(dsl -> dsl.delete(TABLE).execute());
    }
}
