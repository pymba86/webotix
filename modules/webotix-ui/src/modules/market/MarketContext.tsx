import React from "react";
import {Exchange} from "./types";

export interface MarketData {
    exchanges: Array<Exchange>;
    selectedExchange?: Exchange;
}


export interface MarketApi {
    data: MarketData
}

export const MarketContext = React.createContext<MarketApi>({
    data: {
        exchanges: []
    }
});