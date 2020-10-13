import React from "react";
import {Exchange} from "./types";

export interface MarketData {
    exchanges: Array<Exchange>;
    selectedExchange?: Exchange;
}

export interface MarketActions {
    refreshExchanges(): void;
}

export interface MarketApi {
    data: MarketData,
    actions: MarketActions;
}

export const MarketContext = React.createContext<MarketApi>({
    data: {
        exchanges: []
    },
    actions: {
        refreshExchanges(): void {
        }
    }
});