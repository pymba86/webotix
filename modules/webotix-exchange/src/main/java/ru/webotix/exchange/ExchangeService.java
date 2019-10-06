package ru.webotix.exchange;

import java.util.Collection;

public interface ExchangeService {

    /**
     * Получить список названий используемых криптобирж
     * @return Список используемых криптобирж
     */
    Collection<String> getExchanges();
}
