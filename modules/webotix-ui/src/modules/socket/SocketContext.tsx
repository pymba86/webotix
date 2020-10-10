import React from "react";
import {Balance, Order, Ticker, UserTrade} from "./types";
import {Map} from "immutable"

export interface SocketApi {
    connected: boolean;
    tickers: Map<String, Ticker>;
    userTrades: Array<UserTrade>;
    balances: Map<String, Balance>;
    openOrders: Array<Order>;
    selectedCoinTicker?: Ticker;
}

export const SocketContext = React.createContext<SocketApi>({
    connected: false,
    tickers: Map([]),
    userTrades: [],
    openOrders: [],
    balances: Map([])
});