package ru.webotix.datasource.database;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.hibernate.UnitOfWorkAspect;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.context.internal.ManagedSessionContext;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;

@Singleton
public class Transactionally {

    public static final UnitOfWork DEFAULT_UNIT =
            new UnitOfWork() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return UnitOfWork.class;
                }

                @Override
                public boolean readOnly() {
                    return false;
                }

                @Override
                public boolean transactional() {
                    return true;
                }

                @Override
                public CacheMode cacheMode() {
                    return CacheMode.NORMAL;
                }

                @Override
                public FlushMode flushMode() {
                    return FlushMode.AUTO;
                }

                @Override
                public String value() {
                    return HibernateBundle.DEFAULT_NAME;
                }
            };

    public static final UnitOfWork READ_ONLY_UNIT =
            new UnitOfWork() {

                @Override
                public Class<? extends Annotation> annotationType() {
                    return UnitOfWork.class;
                }

                @Override
                public boolean readOnly() {
                    return false;
                }

                @Override
                public boolean transactional() {
                    return true;
                }

                @Override
                public CacheMode cacheMode() {
                    return CacheMode.NORMAL;
                }

                @Override
                public FlushMode flushMode() {
                    return FlushMode.AUTO;
                }

                @Override
                public String value() {
                    return HibernateBundle.DEFAULT_NAME;
                }
            };

    private final Provider<SessionFactory> sessionFactory;
    private final boolean allowNested;

    @Inject
    Transactionally(Provider<SessionFactory> sessionFactory) {
        this(sessionFactory, false);
    }

    private Transactionally(Provider<SessionFactory> sessionFactory, boolean allowNested) {
        this.sessionFactory = sessionFactory;
        this.allowNested = allowNested;
    }

    public void run(Runnable runnable) {
        call(
                () -> {
                    runnable.run();
                    return null;
                }
        );
    }

    public void run(UnitOfWork unitOfWork, Runnable runnable) {
        call(
                unitOfWork,
                () -> {
                    runnable.run();
                    return null;
                }
        );
    }

    public <T> T call(Callable<T> callable) {
        return call(DEFAULT_UNIT, callable);
    }

    public <T> T callChecked(Callable<T> callable) throws Exception {
        return callChecked(DEFAULT_UNIT, callable);
    }

    public <T> T callChecked(UnitOfWork unitOfWork, Callable<T> callable) throws Exception {

        boolean nested = ManagedSessionContext.hasBind(sessionFactory.get());

        if (nested) {
            if (allowNested) {
                return callable.call();
            } else {
                throw new IllegalStateException("Nested units of work not permitted");
            }
        }

        UnitOfWorkAspect unitOfWorkAspect =
                new UnitOfWorkAspect(ImmutableMap.of(HibernateBundle.DEFAULT_NAME,
                        sessionFactory.get()));

        try {
            unitOfWorkAspect.beforeStart(unitOfWork);
            T result = callable.call();
            unitOfWorkAspect.afterEnd();
            return result;
        } catch (Exception e) {
            unitOfWorkAspect.onError();
            throw e;
        } finally {
            unitOfWorkAspect.onFinish();
        }
    }

    public <T> T call(UnitOfWork unitOfWork, Callable<T> callable) {
        try {
            return callChecked(unitOfWork, callable);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public Transactionally allowingNested() {
        return new Transactionally(sessionFactory, true);
    }

}
