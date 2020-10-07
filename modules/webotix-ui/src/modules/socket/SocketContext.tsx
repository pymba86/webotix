import React from "react";
import {Ticker, UserTrade} from "./types";
import {Map} from "immutable"

export interface SocketApi {
    connected: boolean;
    tickers: Map<String, Ticker>;
    userTrades: Array<UserTrade>;
}

export const SocketContext = React.createContext<SocketApi>({
    connected: false,
    tickers: Map([]),
    userTrades: []
});