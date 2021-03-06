package ru.webotix.exchange;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/exchanges")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class ExchangeResource implements WebResource {

    private static final Logger log = LoggerFactory.getLogger(ExchangeResource.class);

    private final ExchangeService exchanges;
    private final Map<String, ExchangeConfiguration> configuration;

    @Inject
    ExchangeResource(ExchangeService exchanges,
                     Map<String, ExchangeConfiguration> configuration) {
        this.exchanges = exchanges;
        this.configuration = configuration;
    }

    /**
     * Получить список доступных бирж
     *
     * @return Список бирж
     */
    @GET
    @Timed
    public Collection<ExchangeMeta> list() {
        return this.exchanges.getExchanges()
                .stream()
                .map(code -> {
                    ExchangeConfiguration exchangeConfig = configuration.get(code);
                    return new ExchangeMeta(
                            code,
                            Exchanges.name(code),
                            exchangeConfig != null
                                    && StringUtils.isNotBlank(exchangeConfig.getApiKey())
                    );
                })
                .collect(Collectors.toList());
    }
}
