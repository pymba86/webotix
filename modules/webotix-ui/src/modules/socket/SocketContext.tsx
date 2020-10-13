import React from "react";
import {Balance, Order, OrderBook, Ticker, Trade, UserTrade} from "./types";
import {Map} from "immutable"

export interface SocketApi {

    connected: boolean;
    tickers: Map<String, Ticker>;
    userTrades: Array<UserTrade>;
    balances: Map<String, Balance>;
    openOrders: Array<Order>;
    selectedCoinTicker?: Ticker;
    trades: Array<Trade>;
    orderBook?: OrderBook;

    createOrder(Order: Order, timestamp: number): void;

    pendingCancelOrder(id: string, timestamp: number): void;

    createPlaceholder(order: Order): void;

    removePlaceholder(): void;
}

export const SocketContext = React.createContext<SocketApi>({
    connected: false,
    tickers: Map([]),
    userTrades: [],
    openOrders: [],
    balances: Map([]),
    trades: [],
    createOrder(Order: Order, timestamp: number): void {
    },
    createPlaceholder(order: Order): void {
    },
    pendingCancelOrder(id: string, timestamp: number): void {
    },
    removePlaceholder(): void {
    }
});