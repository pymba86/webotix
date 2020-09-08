package ru.webotix.market.data;

import com.google.inject.AbstractModule;

public class MarketDataModule extends AbstractModule {



    public enum MarketDataSource {
        MANAGE_LOCALLY,
        MANAGE_REMOTELY
    }
}
