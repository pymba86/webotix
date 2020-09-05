package ru.webotix.job;

/**
 * Событие запускается, чтобы сообщить всем запушенным заданиям,
 * чтобы они остановили свою работу и подготовились к отключению
 */
public class StopEvent {

    public static final StopEvent INSTANCE = new StopEvent();

    private StopEvent() {

    }
}
