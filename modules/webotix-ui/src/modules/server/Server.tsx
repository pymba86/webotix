import React, {useMemo, useState} from "react";
import {ServerApi, ServerContext} from "./ServerContext"
import {Job} from "./types";
import jobService from "./jobService";
import {useInterval} from "../common/hooks";
import {Coin} from "../market";

export const Server: React.FC = ({children}) => {

    const [jobs, setJobs] = useState<Job[]>([]);
    const [subscriptions, setSubscriptions] = useState<Coin[]>([]);

    const fetchJobs = useMemo(
        () => () => {
            jobService.fetchJobs()
                .then(response => response.json())
                .then((jobs: Job[]) => setJobs(jobs))
        },
        []
    );

    useInterval(
        () => {
          //  fetchJobs();
        },
        5000,
        [fetchJobs]
    );

    const api: ServerApi = useMemo(
        () => ({
            jobs: jobs ? jobs : [],
            jobsLoading: !jobs,
            subscriptions,
            removeSubscription(coin: Coin) {
            },
            addSubscription(coin: Coin) {
            }
        }),
        [jobs, subscriptions]
    );

    return (
        <ServerContext.Provider value={api}>
            {children}
        </ServerContext.Provider>
    )
};