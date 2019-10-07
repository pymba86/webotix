package ru.webotix.exchange;

import org.knowm.xchange.Exchange;

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
}
