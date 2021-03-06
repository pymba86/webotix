package ru.webotix.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import ru.webotix.job.api.Job;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.webotix.job.JobRecord.TABLE_NAME;

@Singleton
public class RecordJobAccess implements JobAccess {

    private final ObjectMapper objectMapper;
    private final Provider<SessionFactory> sessionFactory;
    private final JobLocker jobLocker;

    @Inject
    RecordJobAccess(Provider<SessionFactory> sessionFactory,
                    ObjectMapper objectMapper,
                    JobLocker jobLocker) {

        this.sessionFactory = sessionFactory;
        this.objectMapper = objectMapper;
        this.jobLocker = jobLocker;
    }

    @Override
    public void insert(Job job) throws JobAlreadyExistsException {
        JobRecord record = new JobRecord(job.id(), encode(job), false);
        try {

            Session session = session();

            session.save(record);
            session.flush();

        } catch (NonUniqueObjectException e) {
            throw new JobAlreadyExistsException();
        } catch (PersistenceException e) {

            if (e.getCause() instanceof ConstraintViolationException) {
                throw new JobAlreadyExistsException();
            }

            throw e;

        }
    }

    private Session session() {
        return sessionFactory.get().getCurrentSession();
    }

    @Override
    public void update(Job job) {
        JobRecord record = fetchAndLockRecord(job.id());
        record.setContent(encode(job));
        session().update(record);
    }

    @Override
    public Job load(String id) {
        return decode(fetchRecord(id).getContent());
    }

    @Override
    public Iterable<Job> list() {
        List<JobRecord> results = session()
                .createQuery("from " + TABLE_NAME + " where processed = false", JobRecord.class)
                .list();

        return results.stream()
                .map(JobRecord::getContent)
                .map(this::decode)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String jobId) {
        int updated = session()
                .createQuery(
                        "update " + TABLE_NAME
                                + " set processed = true where id = :id and processed = false"
                )
                .setParameter("id", jobId)
                .executeUpdate();

        if (updated == 0) {
            throw new JobDoesNotExistException();
        }

        jobLocker.releaseAnyLock(jobId);
    }

    @Override
    public void deleteAll() {
        session()
                .createQuery("update " + TABLE_NAME + " set processed = true where processed = false")
                .executeUpdate();
    }

    private String encode(Job job) {
        try {
            return objectMapper.writeValueAsString(job);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Job decode(String content) {
        try {
            return objectMapper.readValue(content, Job.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JobRecord fetchAndLockRecord(String id) {
        JobRecord jobRecord = session().get(JobRecord.class, id, LockMode.PESSIMISTIC_WRITE);
        if (jobRecord == null || jobRecord.isProcessed()) {
            throw new JobDoesNotExistException();
        }

        return jobRecord;
    }

    private JobRecord fetchRecord(String id) {
        JobRecord jobRecord = session().get(JobRecord.class, id);
        if (jobRecord == null || jobRecord.isProcessed()) {
            throw new JobDoesNotExistException();
        }
        return jobRecord;
    }
}
