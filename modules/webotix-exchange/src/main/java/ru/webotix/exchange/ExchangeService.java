package ru.webotix.exchange;

import com.google.inject.ImplementedBy;
import org.knowm.xchange.Exchange;

import java.util.Collection;

@ImplementedBy(CacheExchangeService.class)
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

    /**
     * Получить ограничитель скорости операций по бирже
     *
     * @param name название биржы
     * @return ограничитель скорости
     */
    RateController rateController(String name);
}
