export enum JobType {
    ALERT = "Alert"
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

export interface CoinMetadata {
    maximumAmount: number;
    minimumAmount: number;
    priceScale: number;
}