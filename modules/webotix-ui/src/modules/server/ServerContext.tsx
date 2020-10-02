import React from "react";
import {Job} from "./types";

export interface ServerApi {
    jobs: Job[];
    jobsLoading: boolean;
}

export const ServerContext = React.createContext<ServerApi>({
    jobs: [],
    jobsLoading: true
});