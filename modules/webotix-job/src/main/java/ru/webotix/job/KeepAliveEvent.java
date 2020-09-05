package ru.webotix.job;

/**
 * Событие запускается при обычном тике.
 * Событие должно обновить блокировку базы данных
 */
final class KeepAliveEvent {

    public static final KeepAliveEvent INSTANCE = new KeepAliveEvent();

    private KeepAliveEvent() {

    }
}
