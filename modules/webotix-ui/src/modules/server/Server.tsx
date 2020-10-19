import React, {useContext, useEffect, useMemo, useState} from "react";
import {ServerApi, ServerContext} from "./ServerContext"
import {CoinMetadata, Job, JobType, ScriptJob} from "./types";
import jobService from "./jobService";
import {useInterval} from "../common/hooks";
import {Coin, ServerCoin} from "../market";
import exchangeService from "../market/exchangeService";
import {coinFromTicker, tickerFromCoin} from "../market/utils";
import {LogContext} from "../log/LogContext";

function compareCoins(a: ServerCoin, b: ServerCoin) {
    if (a.exchange < b.exchange) return -1;
    if (a.exchange > b.exchange) return 1;
    if (a.base < b.base) return -1;
    if (a.base > b.base) return 1;
    if (a.counter < b.counter) return -1;
    if (a.counter > b.counter) return 1;
    return 0
}

function insertCoin(arr: Coin[], coin: Coin): Coin[] {
    for (let i = 0, len = arr.length; i < len; i++) {
        if (compareCoins(coin, arr[i]) < 0) {
            arr.splice(i, 0, coin);
            return arr
        }
    }
    return arr.concat([coin])
}

export const Server: React.FC = ({children}) => {

    const [jobs, setJobs] = useState<Job[]>([]);
    const [subscriptions, setSubscriptions] = useState<Coin[]>([]);
    const [coinMetadata, setCoinMetadata] = useState<Map<String, CoinMetadata>>(new Map());

    const logApi = useContext(LogContext);

    const errorPopup = logApi.errorPopup;
    const errorLog = logApi.localError;
    const trace = logApi.trace;

    const fetchMetadata = useMemo(
        () => (coin: Coin) => {
            exchangeService.fetchMetadata(coin)
                .then((meta: CoinMetadata) => setCoinMetadata(current => current.set(coin.key, meta)))
                .then(() => trace("Fetched metadata for " + coin.shortName))
                .catch((error: Error) => errorPopup("Could not fetch coin metadata: " + error.message));
        }, [setCoinMetadata, errorPopup, trace]
    );

    const fetchJobs = useMemo(
        () => () => {
            jobService.fetchJobs()
                .then((jobs: Job[]) => setJobs(jobs))
                .catch((error: Error) => errorLog("Could not fetch jobs: " + error.message));
        },
        [errorLog]
    );

    const submitScriptJob = useMemo(
        () => (job: ScriptJob) => {
            jobService.submitScriptJob(job)
                .catch((error: Error) => errorPopup("Could not submit job: " + error.message))
                .then(() => setJobs(
                    current => ([...current, {jobType: JobType.SCRIPT, ...job}])))
        },
        [errorPopup]
    );

    const addSubscription = useMemo(
        () => (coin: Coin) => {
            return exchangeService.addSubscription(tickerFromCoin(coin))
                .then(() => setSubscriptions(current => insertCoin(current, coin)))
                .then(() => fetchMetadata(coin))
                .catch((error: Error) => errorPopup("Could not add subscription: " + error.message));
        }, [setSubscriptions, errorPopup, fetchMetadata]
    );

    const removeSubscription = useMemo(
        () => (coin: Coin) => {
            return exchangeService.removeSubscription(tickerFromCoin(coin))
                .then(() => setSubscriptions(current => current.filter(c => c.key !== coin.key)))
                .catch((error: Error) => errorPopup("Could not remove subscription: " + error.message));
        }, [setSubscriptions, errorPopup]
    );

    useInterval(
        () => {
            fetchJobs();
        },
        5000,
        [fetchJobs]
    );

    useEffect(() => {
        exchangeService.fetchSubscriptions()
            .then((data: ServerCoin[]) => {
                const coins = data.map((t: ServerCoin) => coinFromTicker(t));
                setSubscriptions(coins.sort(compareCoins));
                trace("Fetched " + data.length + " subscriptions");
                coins.forEach(coin => fetchMetadata(coin));
            })
            .catch((error: Error) => errorPopup("Could not fetch coin list: " + error.message));
    }, [setSubscriptions, fetchMetadata, errorPopup, trace]);

    const api: ServerApi = useMemo(
        () => ({
            jobs: jobs ? jobs : [],
            jobsLoading: !jobs,
            coinMetadata,
            subscriptions,
            removeSubscription,
            addSubscription,
            submitScriptJob
        }),
        [jobs, subscriptions, submitScriptJob, addSubscription, coinMetadata, removeSubscription]
    );

    return (
        <ServerContext.Provider value={api}>
            {children}
        </ServerContext.Provider>
    )
};