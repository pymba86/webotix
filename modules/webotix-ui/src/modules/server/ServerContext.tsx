import React from "react";
import {Job} from "./types";

export interface ServerApi {
    jobs: Job[];
}

export const ServerContext = React.createContext<ServerApi | null>(null);