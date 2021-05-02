package ru.webotix.exchange.api;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import ru.webotix.market.data.api.TickerSpec;

import java.util.Collection;

public interface ExchangeService {

    /**
     * Получить список названий используемых криптобирж
     *
     * @return Список используемых криптобирж
     */
    Collection<String> getExchanges();

    /**
     * Получить биржу по названию
     *
     * @param name название биржы
     * @return биржа
     */
    Exchange get(String name);

    /**
     * Проверка на аутенфикацию на бирже
     *
     * @param name название биржы
     * @return проверка на аутенфикацию на бирже
     */
    boolean isAuthenticated(String name);


    CurrencyPairMetaData fetchCurrencyPairMetaData(TickerSpec ex);

    /**
     * Получить ограничитель скорости операций по бирже
     *
     * @param name название биржы
     * @return ограничитель скорости
     */
    RateController rateController(String name);
}
