package ru.webotix.market.data;

/**
 * Для тестирования.
 * Запускает сигналы в ключевых событиях, позволяя организовать тесты.
 */
interface LifecycleListener {
    default void onBlocked(String exchange) {
    }

    default void onStop(String exchange) {
    }

    default void onStopMain() {
    }
}