import React from "react";
import {Exchange} from "./types";

export interface MarketData {
    exchanges: Array<Exchange>;
    selectedExchange: Exchange | null | undefined;
}


export interface MarketApi {
    data: MarketData
}

export const MarketContext = React.createContext<MarketApi>({
    data: {
        exchanges: [],
        selectedExchange: null
    }
});