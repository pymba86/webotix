package ru.webotix.datasource.wiring;

public interface BackgroundProcessingConfiguration {

    /**
     * Некоторые операции требуют опроса
     * (обмены без поддержки веб-сокетов, таймауты кеширования и т. Д.).
     *
     * Этот параметр отвечает за время цикла.
     */
    public int getLoopSeconds();
}
