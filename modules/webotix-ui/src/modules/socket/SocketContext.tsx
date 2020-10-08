import React from "react";
import {Order, Ticker, UserTrade} from "./types";
import {Map} from "immutable"

export interface SocketApi {
    connected: boolean;
    tickers: Map<String, Ticker>;
    userTrades: Array<UserTrade>;
    openOrders: Array<Order>;
}

export const SocketContext = React.createContext<SocketApi>({
    connected: false,
    tickers: Map([]),
    userTrades: [],
    openOrders: []
});