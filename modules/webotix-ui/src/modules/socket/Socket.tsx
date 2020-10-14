import React, {useCallback, useContext, useEffect, useMemo, useRef, useState} from "react";
import {SocketApi, SocketContext} from "./SocketContext";
import {Balance, OrderBook, Ticker, Trade, UserTrade} from "./types";
import {Map} from "immutable"
import {useLocation} from "react-router-dom";
import {locationToCoin} from "../../selectors/coins";
import {ServerContext} from "../server/ServerContext";
import {useOrders} from "./useOrders";
import {LogContext} from "../log/LogContext";
import * as socketClient from "./client"
import {useInterval} from "../common/hooks";
import ReactDOM from "react-dom";
import {Coin} from "../market";

const MAX_PUBLIC_TRADES = 48;
const UPDATE_FREQUENCY = 1000;

enum BatchScope {
    GLBOAL,
    COIN
}

class BatchItem {

    scope: BatchScope;
    process: () => void;

    constructor(scope: BatchScope, process: () => void) {
        this.scope = scope;
        this.process = process;
    }
}

const initialOrderBookState: OrderBook = {
    asks: [],
    bids: [],
    timestamp: new Date()
};

export const Socket: React.FC = ({children}) => {

    const batch = useRef<Array<BatchItem>>([]);

    useInterval(() => {
        ReactDOM.unstable_batchedUpdates(() => {
            if (batch.current.length > 1) {
                batch.current.forEach(it => it.process());
                batch.current = new Array<BatchItem>();
            }
        });
    }, UPDATE_FREQUENCY, [batch]);

    const clearBatchItemsForCoin = () => {
        batch.current = batch.current.filter(it => it.scope !== BatchScope.COIN);
    };

    const addToBatch = (scope: BatchScope, process: () => void) => {
        batch.current.push(new BatchItem(scope, process));
    };

    const serverApi = useContext(ServerContext);
    const logApi = useContext(LogContext);

    const location = useLocation();

    const [tickers, setTickers] = useState(Map<String, Ticker>());
    const [balances, setBalances] = useState(Map<String, Balance>());

    const [orderBook, setOrderBook] = useState<OrderBook>(initialOrderBookState);

    const [trades, tradesUpdateApi] = useState<Trade[]>([]);

    const [openOrders, openOrdersUpdateApi] = useOrders();

    const [userTrades, userTradesUpdateApi] = useState<UserTrade[]>([]);

    const [connected, setConnected] = useState(false);

    const selectedCoin = useMemo(() => locationToCoin(location), [location]);

    const selectedCoinTicker = useMemo(
        () => (selectedCoin ? tickers.get(selectedCoin.key) : undefined),
        [tickers, selectedCoin]);

    const logError = logApi.localError;
    const logMessage = logApi.localMessage;
    const logNotification = logApi.add;
    const getSelectedCoin = useCallback(() => locationToCoin(location), [location]);

    useEffect(() => {

            socketClient.onError(
                message => addToBatch(BatchScope.GLBOAL, () => logError(message)));

            socketClient.onNotification(
                (entry => addToBatch(BatchScope.GLBOAL, () => logNotification(entry))));

            const sameCoin = (left?: Coin, right?: Coin) => left && right && left.key === right.key;

            socketClient.onTicker(((coin, ticker) => {
                addToBatch(BatchScope.GLBOAL,
                    () => setTickers(tickers => tickers.set(coin.key, ticker)))
            }));

            socketClient.onBalance(((exchange, currency, balance) => {

                const coin = getSelectedCoin();

                if (coin && coin.exchange === exchange) {

                    if (coin.base === currency) {
                        addToBatch(BatchScope.COIN, () => setBalances(
                            balances => Map.of(
                                currency, balance, coin.counter, balances.get(coin.counter))))
                    }

                    if (coin.counter === currency) {
                        addToBatch(BatchScope.COIN, () => setBalances(
                            balances => Map.of(
                                currency, balance, coin.base, balances.get(coin.base))))
                    }
                }
            }));

            socketClient.onOrderBook((coin, orderBook) => {

                if (sameCoin(coin, getSelectedCoin())) {
                    addToBatch(BatchScope.COIN,
                        () => setOrderBook(orderBook));
                }
            });

            socketClient.onTrade((coin, trade) => {

                if (sameCoin(coin, getSelectedCoin())) {
                    addToBatch(BatchScope.COIN,
                        () => tradesUpdateApi(state => {

                            const trades = [trade, ...state];

                            if (trades.length > MAX_PUBLIC_TRADES) {
                                return trades.slice(0, MAX_PUBLIC_TRADES);
                            } else {
                                return trades;
                            }
                        }));
                }
            });

            socketClient.onUserTrade((coin, trade) => {

                if (sameCoin(coin, getSelectedCoin())) {
                    addToBatch(BatchScope.COIN,
                        () => userTradesUpdateApi(state => {

                            const status = state.find(
                                existing => !!trade.id && existing.id === trade.id);

                            if (status) {
                                return state;
                            } else {
                                return [trade, ...state];
                            }
                        }))
                }
            });

            socketClient.onOrderUpdate(((coin, order, timestamp) => {

                if (sameCoin(coin, getSelectedCoin())) {
                    addToBatch(BatchScope.COIN,
                        () => openOrdersUpdateApi.orderUpdated(order, timestamp));
                }
            }));

            socketClient.onOrdersSnapshot((coin, orders, timestamp) => {

                if (sameCoin(coin, getSelectedCoin())) {
                    addToBatch(BatchScope.COIN,
                        () => openOrdersUpdateApi.updateSnapshot(orders, timestamp));
                }
            });

        },
        [
            getSelectedCoin, tradesUpdateApi, userTradesUpdateApi,
            openOrdersUpdateApi, logError, logNotification
        ]);

    useEffect(() => {
        socketClient.connect();

        return () => {
            console.log("Authorization lost");
            socketClient.disconnect();
        }
    }, []);

    useEffect(() => {
        socketClient.onConnectionStateChange(state => {
            console.log("Detected socket connection state", state);
            setConnected(state);
        });
    }, [setConnected]);

    useEffect(() => {
        if (connected) {
            console.log("Reconnected");
            logMessage("Socket connected");
        }

        return () => {
            console.log("Disconnected");
            logMessage("Socket disconnected");
        }
    }, [connected, logMessage]);

    const subscribedCoins = serverApi.subscriptions;

    useEffect(() => {
        if (connected) {
            console.log("Resubscribing");
            socketClient.changeSubscriptions(subscribedCoins, selectedCoin);
            socketClient.resubscribe();
        }
    }, [connected, subscribedCoins, selectedCoin]);

    useEffect(() => {
        console.log("Clearing current coin state");
        setOrderBook(initialOrderBookState);
        userTradesUpdateApi([]);
        openOrdersUpdateApi.clear();
        tradesUpdateApi([]);
        setBalances(Map<String, Balance>());
        clearBatchItemsForCoin();
    }, [selectedCoin, userTradesUpdateApi, tradesUpdateApi, openOrdersUpdateApi, setBalances, setOrderBook]);

    const api: SocketApi = useMemo(
        () => ({
            connected,
            tickers,
            balances,
            userTrades,
            openOrders,
            selectedCoinTicker,
            orderBook,
            trades,
            removePlaceholder: openOrdersUpdateApi.removePlaceholder,
            pendingCancelOrder: openOrdersUpdateApi.pendingCancelOrder,
            createPlaceholder: openOrdersUpdateApi.createPlaceholder,
            createOrder: openOrdersUpdateApi.orderUpdated
        }),
        [connected, openOrders, tickers, userTrades, trades,
            selectedCoinTicker, balances, openOrdersUpdateApi, orderBook]
    );


    return <SocketContext.Provider value={api}>{children}</SocketContext.Provider>
};