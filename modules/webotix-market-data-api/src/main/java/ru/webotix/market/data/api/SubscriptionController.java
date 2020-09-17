package ru.webotix.market.data.api;

import java.util.Set;

public interface SubscriptionController {

    /**
     * Обновить подписчиков на тикер
     *
     * @param subscriptions Подписки на тип данных - определенный тикер
     */
    void updateSubscriptions(Set<MarketDataSubscription> subscriptions);
}
