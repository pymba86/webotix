import {Coin, PartialServerCoin} from "../market";
import {augmentCoin} from "../market/utils";

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