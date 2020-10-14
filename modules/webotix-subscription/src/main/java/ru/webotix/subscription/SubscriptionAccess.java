package ru.webotix.subscription;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.market.data.api.TickerSpec;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.webotix.subscription.Subscription.TABLE_NAME;

@Singleton
public class SubscriptionAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionAccess.class);

    private final Provider<SessionFactory> sessionFactory;

    @Inject
    SubscriptionAccess(Provider<SessionFactory> sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    void add(TickerSpec spec) {
        LOGGER.info("Adding permanent subscription to {}", spec);
        Subscription sub = session().get(Subscription.class, spec.key());
        if (sub == null) {
            session().merge(new Subscription(spec, null));
        }
    }

    void remove(TickerSpec spec) {
        LOGGER.info("Removing permanent subscription to {}", spec);
        Subscription subscription = session().get(Subscription.class, spec.key());
        if (subscription != null) session().delete(subscription);
    }

    Set<TickerSpec> all() {
        List<Subscription> results =
                session().createQuery("from " + TABLE_NAME, Subscription.class).list();
        return results.stream().map(Subscription::getTicker).collect(Collectors.toSet());
    }

    void setReferencePrice(TickerSpec tickerSpec, BigDecimal price) {
        session().merge(new Subscription(tickerSpec, price));
    }

    Map<TickerSpec, BigDecimal> getReferencePrices() {
        List<Subscription> results =
                session().createQuery("from " + TABLE_NAME, Subscription.class).list();
        Builder<TickerSpec, BigDecimal> builder = ImmutableMap.builder();
        results.stream()
                .filter(r -> r.getReferencePrice() != null)
                .forEach(r -> builder.put(r.getTicker(), r.getReferencePrice()));
        return builder.build();
    }

    private Session session() {
        return sessionFactory.get().getCurrentSession();
    }
}
