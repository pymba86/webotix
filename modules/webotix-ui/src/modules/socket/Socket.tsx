import React, {useContext, useMemo, useState} from "react";
import {SocketApi, SocketContext} from "./SocketContext";
import {Balance, OrderType, Ticker, UserTrade} from "./types";
import {Map} from "immutable"
import {useLocation} from "react-router-dom";
import {locationToCoin} from "../../selectors/coins";
import {ServerContext} from "../server/ServerContext";
import {useOrders} from "./useOrders";

export const Socket: React.FC = ({children}) => {

    const serverApi = useContext(ServerContext);

    const location = useLocation();

    const [tickers, setTickers] = useState(Map<String, Ticker>());
    const [balances, setBalances] = useState(Map<String, Balance>());
    const [openOrders, openOrdersUpdateApi] = useOrders();

    const [userTrades, userTradesUpdateApi] = useState<UserTrade[]>([
        {
            coin: {base: "123", counter: "123", exchange: "123", key: "123", name: "123", shortName: "213"},
            feeAmount: 8,
            feeCurrency: "BTC",
            id: "2",
            originalAmount: 10,
            price: 12,
            timestamp: new Date(),
            type: OrderType.ASK
        }, {
            coin: {base: "123", counter: "123", exchange: "123", key: "123", name: "123", shortName: "213"},
            feeAmount: 8,
            feeCurrency: "ETH",
            id: "3",
            originalAmount: 10,
            price: 12,
            timestamp: new Date(),
            type: OrderType.BID
        }, {
            coin: {base: "123", counter: "123", exchange: "123", key: "123", name: "123", shortName: "213"},
            feeAmount: 80,
            feeCurrency: "SOK",
            id: "1",
            originalAmount: 100,
            price: 224,
            timestamp: new Date(2132122312223),
            type: OrderType.ASK
        }]);

    const [connected, setConnected] = useState(false);

    const selectedCoin = useMemo(() => locationToCoin(location), [location]);

    const selectedCoinTicker = useMemo(
        () => (selectedCoin ? tickers.get(selectedCoin.key) : undefined),
        [tickers, selectedCoin]);

    const api: SocketApi = useMemo(
        () => ({
            connected,
            tickers,
            balances,
            userTrades,
            openOrders,
            selectedCoinTicker
        }),
        [connected, openOrders, tickers, userTrades, selectedCoinTicker, balances]
    );


    return <SocketContext.Provider value={api}>{children}</SocketContext.Provider>
};