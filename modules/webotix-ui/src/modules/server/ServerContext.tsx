import React from "react";
import {CoinMetadata, Job, ScriptJob} from "./types";
import {Coin} from "../market";

export interface ServerApi {
    subscriptions: Coin[];
    coinMetadata: Map<String, CoinMetadata>;
    jobs: Job[];
    jobsLoading: boolean;

    addSubscription(coin: Coin): Promise<void>;

    removeSubscription(coin: Coin): Promise<void>;

    submitScriptJob(job: ScriptJob): void;

    deleteJob(id: string): void;

    submitJob(job: Job): void
}

export const ServerContext = React.createContext<ServerApi>({
    jobs: [],
    jobsLoading: true,
    subscriptions: [],
    coinMetadata: new Map(),
    addSubscription(coin: Coin) {
        return Promise.reject("addSubscription not implemented");
    },
    removeSubscription(coin: Coin) {
        return Promise.reject("removeSubscription not implemented")
    },
    submitScriptJob(job: ScriptJob): void {
    },
    deleteJob(id: string) {
    },
    submitJob(job: Job) {
    }
});