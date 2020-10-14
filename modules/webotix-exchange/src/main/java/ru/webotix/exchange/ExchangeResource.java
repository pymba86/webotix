package ru.webotix.exchange;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.gruelbox.tools.dropwizard.guice.resources.WebResource;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.webotix.exchange.api.ExchangeService;
import ru.webotix.exchange.api.PairMetaData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
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

    /**
     * Получить текущий тике для указанной биржи и пары
     *
     * @param exchange Биржда
     * @param counter  Контр валюта
     * @param base     Базовая валюта
     * @return Тикер
     * @throws IOException Если выбрасывается при обмене
     */
    @GET
    @Path("{exchange}/markets/{base}-{counter}/ticker")
    @Timed
    public Ticker ticker(@PathParam("exchange") String exchange,
                         @PathParam("counter") String counter,
                         @PathParam("base") String base) throws IOException {

        return exchanges.get(exchange)
                .getMarketDataService()
                .getTicker(new CurrencyPair(base, counter));
    }

    /**
     * Список валютных пар на указанной бирже
     *
     * @param exchange Биржа
     * @return Поддерживаемые валютные пары
     */
    @GET
    @Timed
    @Path("{exchange}/pairs")
    public Collection<ExchangePair> pairs(@PathParam("exchange") String exchange) {

        return exchanges.get(exchange)
                .getExchangeMetaData()
                .getCurrencyPairs()
                .keySet()
                .stream()
                .map(ExchangePair::new)
                .collect(Collectors.toSet());
    }

    @GET
    @Timed
    @Path("{exchange}/pairs/{base}-{counter}")
    public PairMetaData metadata(
            @PathParam("exchange") String exchangeName,
            @PathParam("counter") String counter,
            @PathParam("base") String base) {
        Exchange exchange = exchanges.get(exchangeName);
        CurrencyPair currencyPair = new CurrencyPair(base, counter);
        return new PairMetaData(exchange.getExchangeMetaData().getCurrencyPairs().get(currencyPair));
    }
}
