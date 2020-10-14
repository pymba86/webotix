import React from "react";
import {CoinMetadata, Job} from "./types";
import {Coin} from "../market";

export interface ServerApi {
    subscriptions: Coin[];

    coinMetadata: Map<String, CoinMetadata>;

    addSubscription(coin: Coin): void;

    removeSubscription(coin: Coin): void;

    jobs: Job[];
    jobsLoading: boolean;
}

export const ServerContext = React.createContext<ServerApi>({
    jobs: [],
    jobsLoading: true,
    subscriptions: [],
    coinMetadata: new Map(),
    addSubscription(coin: Coin) {
        console.warn("addSubscription not implemented");
    },
    removeSubscription(coin: Coin) {
        console.warn("removeSubscription not implemented")
    }
});