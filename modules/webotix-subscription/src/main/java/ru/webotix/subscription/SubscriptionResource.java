package ru.webotix.subscription;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import io.dropwizard.hibernate.UnitOfWork;
import ru.webotix.market.data.api.TickerSpec;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Path("/subscriptions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class SubscriptionResource implements WebResource {

    private final SubscriptionManager subscriptionManager;
    private final SubscriptionAccess subscriptionAccess;

    @Inject
    SubscriptionResource(
            SubscriptionManager permanentSubscriptionManager,
            SubscriptionAccess permanentSubscriptionAccess) {
        this.subscriptionManager = permanentSubscriptionManager;
        this.subscriptionAccess = permanentSubscriptionAccess;
    }

    @GET
    @Timed
    @UnitOfWork(readOnly = true)
    public Collection<TickerSpec> list() {
        return subscriptionAccess.all();
    }

    @PUT
    @Timed
    @UnitOfWork
    public void put(TickerSpec spec) {
        subscriptionManager.add(spec);
    }

    @GET
    @Timed
    @UnitOfWork(readOnly = true)
    @Path("referencePrices")
    public Map<String, BigDecimal> listReferencePrices() {

        Set<Map.Entry<TickerSpec, BigDecimal>> prices =
                subscriptionAccess.getReferencePrices().entrySet();

        Map<String, Map.Entry<TickerSpec, BigDecimal>> keys =
                Maps.uniqueIndex(prices, e -> Objects.requireNonNull(e).getKey().key());

        return Maps.transformValues(keys, tickerSpecBigDecimalEntry
                -> tickerSpecBigDecimalEntry != null ? tickerSpecBigDecimalEntry.getValue() : null);
    }

    @PUT
    @Timed
    @UnitOfWork
    @Path("referencePrices/{exchange}/{base}-{counter}")
    public void setReferencePrice(
            @PathParam("exchange") String exchange,
            @PathParam("counter") String counter,
            @PathParam("base") String base,
            BigDecimal price) {
        subscriptionAccess.setReferencePrice(
                TickerSpec.builder().exchange(exchange).base(base).counter(counter).build(), price);
    }

    @DELETE
    @Timed
    @UnitOfWork
    public void delete(TickerSpec spec) {
        subscriptionManager.remove(spec);
    }
}
