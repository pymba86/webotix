import React from "react";
import {Ticker} from "./types";
import {Map} from "immutable"

export interface SocketApi {
    connected: boolean;
    tickers: Map<String, Ticker>;
}

export const SocketContext = React.createContext<SocketApi>({
    connected: false,
    tickers: Map([])
});