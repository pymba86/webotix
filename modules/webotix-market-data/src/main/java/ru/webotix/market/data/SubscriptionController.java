package ru.webotix.market.data;

import java.util.Set;

interface SubscriptionController {

    /**
     * Изменяет публикуемые рыночные данные
     * @param subscriptions
     */
    void updateSubscriptions(Set<MarketDataSubscription> subscriptions);
}
