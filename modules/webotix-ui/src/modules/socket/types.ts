import {Coin, PartialServerCoin} from "../market";
import {augmentCoin} from "../market/utils";

export interface BaseOrder {
    type: OrderType;
    originalAmount: number;
    remainingAmount: number;
    stopPrice: number;
    limitPrice: number;
}

export interface Balance {
    total: number;
    available: number;
}

export enum OrderStatus {
    PENDING_NEW = "PENDING_NEW",
    NEW = "NEW",
    PARTIALLY_FILLED = "PARTIALLY_FILLED",
    FILLED = "FILLED",
    PENDING_CANCEL = "PENDING_CANCEL",
    PARTIALLY_CANCELED = "PARTIALLY_CANCELED",
    CANCELED = "CANCELED",
    PENDING_REPLACE = "PENDING_REPLACE",
    REPLACED = "REPLACED",
    STOPPED = "STOPPED",
    REJECTED = "REJECTED",
    EXPIRED = "EXPIRED",
    UNKNOWN = "UNKNOWN"
}

export enum RunningAtType {
    SERVER = "SERVER",
    EXCHANGE = "EXCHANGE"
}

export interface DisplayOrder extends Order {
    runningAt: RunningAtType;
    jobId: string;
}

export interface Order extends BaseOrder {
    id: string;
    timestamp: number;
    status: OrderStatus;
    currencyPair: PartialServerCoin;
    cumulativeAmount: number;
    averagePrice: number;
    fee: number;
    deleted: boolean;
    serverTimestamp: number;
}

export interface Ticker {
    bid: number;
    last: number;
    ask: number;
    open: number;
    low: number;
    high: number;
}

export enum OrderType {
    BID = "BID",
    ASK = "ASK",
    EXIT_ASK = "EXIT_ASK",
    EXIT_BID = "EXIT_BID"
}

export interface ServerTrade {
    t: OrderType;
    a: number;
    c: PartialServerCoin;
    p: number;
    d: Date;
    id: string;
    oid: string;
    fa: number;
    fc: string;
}

export class Trade {

    type: OrderType;
    originalAmount: number;
    coin: Coin;
    price: number;
    timestamp: Date;
    id: String;

    constructor(source: ServerTrade, exchange: string) {
        this.type = source.t;
        this.originalAmount = source.a;
        this.coin = augmentCoin(source.c, exchange);
        this.price = source.p;
        this.timestamp = new Date(source.d);
        this.id = source.id;
    }
}

export class UserTrade extends Trade {
    feeAmount: number;
    feeCurrency: string;

    constructor(source: ServerTrade, exchange: string) {
        super(source, exchange);
        this.feeAmount = source.fa;
        this.feeCurrency = source.fc
    }
}