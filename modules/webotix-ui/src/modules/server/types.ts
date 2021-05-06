import {ServerCoin} from "../market";

export enum JobType {
    LIMIT_ORDER = "LimitOrderJob",
    SOFT_TRAILING_STOP = "SoftTrailingStop",
    ALERT = "Alert",
    STATUS_UPDATE = "StatusUpdateJob",
    OCO = "OneCancelsOther",
    SCRIPT = "ScriptJob"
}

export interface Job {
    id: string;
    jobType: JobType;
}

export enum AlertLevel {
    INFO = "INFO",
    ERROR = "ERROR",
    ALERT = "ALERT"
}

export interface Notification {
    message: string;
    level: AlertLevel;
}

export interface AlertJob extends Job {
    notification: Notification;
}

export interface ScriptJob extends Job {
    id: string;
    name: string;
    script: string;
    ticker: ServerCoin;
}

export interface CoinMetadata {
    maximumAmount: number;
    minimumAmount: number;
    priceScale: number;
}

export enum TradeDirection {
    SELL = "SELL",
    BUY = "BUY"
}

export interface OcoThresholdTask {
    thresholdAsString: string;
    job: Job;
}

export interface OcoJob extends Job {
    tickTrigger: ServerCoin;
    high?: OcoThresholdTask;
    low?: OcoThresholdTask;
}

export interface LimitOrderJob extends Job {
    tickTrigger: ServerCoin;
    direction: TradeDirection;
    amount: number;
    limitPrice: number;
}

export interface SoftTrailingStopJob extends Job {
    tickTrigger: ServerCoin;
    direction: TradeDirection;
    amount: number;
    stopPrice: number;
    lastSyncPrice: number;
    stopPercentage: number;
    limitPercentage: number;
}

export enum Status {
    /**
     * The job is currently running.
     */
    RUNNING = "RUNNING",

    /**
     * The job ran successfully to completion.
     */
    SUCCESS = "SUCCESS",

    /**
     * The job failed permanently and should be removed.
     */
    FAILURE_PERMANENT = "FAILURE_PERMANENT",

    /**
     * The job failed temporarily and should be shut down
     * locally but picked up again in the future.
     */
    FAILURE_TRANSIENT = "FAILURE_TRANSIENT"
}

export interface StatusUpdateJob extends Job {
    requestId: string;
    status: Status;
    payload: object;
}
