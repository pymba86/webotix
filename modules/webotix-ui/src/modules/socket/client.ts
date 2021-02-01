import ReconnectingWebSocket from "reconnecting-websocket";
import {LogRequest} from "../log/LogContext";
import {Coin, ServerCoin} from "../market";
import {Balance, Order, OrderBook, Ticker, Trade, UserTrade} from "./types";
import * as messages from "./messages"
import {augmentCoin, coin as createCoin} from "../market/utils";

type MessageHandler = (message: string) => void;
type NotificationHandler = (entry: LogRequest) => void;
type ConnectionStateHandler = (connected: boolean) => void;
type CoinAndPayloadHandler<T> = (coin: Coin, payload: T) => void;
type BalanceHandler = (exchange: string, currency: string, balance: Balance) => void;
type OrdersSnapshotHandler = (coin: Coin, orders: Order[], timestamp: number) => void;
type OrderUpdateHandler = (coin: Coin, order: Order, timestamp: number) => void;

let handleError: MessageHandler = message => {
};

let handleConnectionStateChange: ConnectionStateHandler = connected => {
};

let handleNotification: NotificationHandler = entry => {
};

let handleStatusUpdate: MessageHandler = message => {
};

let handleTicker: CoinAndPayloadHandler<Ticker> = (coin, ticker) => {
};

let handleOrderBook: CoinAndPayloadHandler<OrderBook> = (coin, orderBook) => {
};

let handleTrade: CoinAndPayloadHandler<Trade> = (coin, trade) => {
};

let handleOrdersSnapshot: OrdersSnapshotHandler = (coin, orders, timestamp) => {
};

let handleOrderUpdate: OrderUpdateHandler = (coin, order, timestamp) => {

};

let handleUserTrade: CoinAndPayloadHandler<UserTrade> = (coin, trade) => {
};

let handleBalance: BalanceHandler = (exchange, currency, balance) => {
};

let connected = false;
let selectedCoin: ServerCoin;
let subscribedCoins: ServerCoin[];

let socket: ReconnectingWebSocket;
let timer: NodeJS.Timeout;

export function onError(handler: MessageHandler) {
    handleError = handler;
}

export function onConnectionStateChange(handler: ConnectionStateHandler) {
    handleConnectionStateChange = handler;
}

export function onNotification(handler: NotificationHandler) {
    handleNotification = handler;
}

export function onStatusUpdate(handler: MessageHandler) {
    handleStatusUpdate = handler;
}

export function onTicker(handler: CoinAndPayloadHandler<Ticker>) {
    handleTicker = handler;
}

export function onOrderBook(handler: CoinAndPayloadHandler<OrderBook>) {
    handleOrderBook = handler;
}

export function onTrade(handler: CoinAndPayloadHandler<Trade>) {
    handleTrade = handler;
}

export function onOrdersSnapshot(handler: OrdersSnapshotHandler) {
    handleOrdersSnapshot = handler;
}

export function onOrderUpdate(handler: OrderUpdateHandler) {
    handleOrderUpdate = handler;
}

export function onUserTrade(handler: CoinAndPayloadHandler<UserTrade>) {
    handleUserTrade = handler;
}

export function onBalance(handler: BalanceHandler) {
    handleBalance = handler;
}

export function connect() {
    if (connected) {
        return;
    }

    socket = ws("ws");

    socket.addEventListener("open", () => {
        connected = true;
        console.log("Socket (re)connected");
        handleConnectionStateChange(true);
        resubscribe();
    });

    socket.addEventListener("close", () => {
        connected = false;
        console.log("Socket connection temporarily lost");
        handleConnectionStateChange(false);
    });

    socket.addEventListener("message", (evt: MessageEvent) => {
        let content;
        try {
            content = JSON.parse(evt.data);
        } catch (e) {
            console.log("Failed to parse message from server (" + e + ")", evt.data);
            return;
        }
        try {
            content = preProcess(content)
        } catch (e) {
            console.log("Failed to pre-process message from server (" + e + ")", evt.data)
        }
        try {
            receive(content);
        } catch (e) {
            console.log("Failed to handle message from server (" + e + ")", evt.data);
        }
    });

    timer = setInterval(() => send({command: messages.READY}), 3000);
}

function preProcess(obj: any) {
    switch (obj.nature) {
        case messages.ORDERBOOK:
            const ORDERBOOK_SIZE = 20;
            const orderBook = obj.data.orderBook
            if (orderBook.asks.length > ORDERBOOK_SIZE) {
                orderBook.asks = orderBook.asks.slice(0, 20)
            }
            if (orderBook.bids.length > ORDERBOOK_SIZE) {
                orderBook.bids = orderBook.bids.slice(0, 20)
            }
            return obj
        default:
            return obj
    }
}

export function disconnect() {
    if (connected) {
        console.log("Disconnecting socket");
        socket.close(undefined, "Shutdown");
        connected = false;
        clearInterval(timer);
    }
}

export function changeSubscriptions(coins: ServerCoin[], selected?: ServerCoin) {
    if (selected) {
        subscribedCoins = coins.concat([selected]);

        selectedCoin = selected;

    } else {
        subscribedCoins = coins;
    }
}

export function resubscribe() {
    const serverSelectedCoinTickers = selectedCoin ? [webCoinToServerCoin(selectedCoin)] : [];

    send({
        command: messages.CHANGE_TICKERS,
        tickers: subscribedCoins.map(coin => webCoinToServerCoin(coin))
    });

    send({
        command: messages.CHANGE_OPEN_ORDERS,
        tickers: serverSelectedCoinTickers
    });

    send({
        command: messages.CHANGE_ORDER_BOOK,
        tickers: serverSelectedCoinTickers
    });

    send({
        command: messages.CHANGE_TRADES,
        tickers: serverSelectedCoinTickers
    });

    send({
        command: messages.CHANGE_ORDER_STATUS_CHANGE,
        tickers: serverSelectedCoinTickers
    });

    send({
        command: messages.CHANGE_USER_TRADES,
        tickers: serverSelectedCoinTickers
    });

    send({
        command: messages.CHANGE_BALANCE,
        tickers: serverSelectedCoinTickers
    });

    send({
        command: messages.UPDATE_SUBSCRIPTIONS
    });
}

function webCoinToServerCoin(coin: ServerCoin): ServerCoin {
    return {
        exchange: coin.exchange,
        counter: coin.counter,
        base: coin.base
    }
}

function receive(message: { nature: string, data: any }) {
    if (!message) {
        handleError("Empty event from server");
    } else {
        switch (message.nature) {
            case messages.ERROR:
                console.log("Error from socket");
                handleError(message.data);
                break;

            case messages.TICKER:
                handleTicker(
                    createCoin(
                        message.data.spec.exchange,
                        message.data.spec.counter,
                        message.data.spec.base
                    ),
                    message.data.ticker
                );
                break;

            case messages.OPEN_ORDERS:
                handleOrdersSnapshot(
                    augmentCoin(message.data.spec),
                    message.data.openOrders.allOpenOrders,
                    message.data.timestamp
                );
                break;

            case messages.ORDERBOOK:
                handleOrderBook(
                    augmentCoin(message.data.spec),
                    message.data.orderBook
                );
                break;

            case messages.TRADE:
                handleTrade(
                    augmentCoin(message.data.spec),
                    new Trade(message.data.trade, message.data.spec.exchange)
                );
                break;

            case messages.ORDER_STATUS_CHANGE:
                handleOrderUpdate(
                    augmentCoin(message.data.spec),
                    message.data.order,
                    message.data.timestamp
                );
                break;

            case messages.USER_TRADE:
                handleUserTrade(
                    augmentCoin(message.data.spec),
                    new UserTrade(message.data.trade, message.data.spec.exchange)
                );
                break;

            case messages.BALANCE:
                handleBalance(
                    message.data.exchange,
                    message.data.balance.currency,
                    message.data.balance
                );
                break;

            case messages.NOTIFICATION:
                handleNotification(message.data);
                break;

            case messages.STATUS_UPDATE:
                handleStatusUpdate(message.data);
                break;

            default:
                handleError("Unknown message type from server: " + message.nature);
        }
    }
}

function send<T>(message: T) {
    if (connected) {
        socket.send(JSON.stringify(message));
    }
}

function ws(url: string): ReconnectingWebSocket {

    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const fullUrl = protocol + "//" + window.location.host + "/" + url;

    return new ReconnectingWebSocket(fullUrl);
}
