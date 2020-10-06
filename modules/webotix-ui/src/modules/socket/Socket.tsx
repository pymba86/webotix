import React, {useContext, useMemo, useState} from "react";
import {SocketApi} from "./SocketContext";
import {Ticker} from "./types";
import {Map} from "immutable"
import {useLocation} from "react-router-dom";
import {locationToCoin} from "../../selectors/coins";
import {SocketContext} from "./SocketContext"
import {ServerContext} from "../server/ServerContext";

export const Socket: React.FC = ({children}) => {

    const serverApi = useContext(ServerContext);

    const location = useLocation();

    const [tickers, setTickers] = useState(Map<String, Ticker>());

    const [connected, setConnected] = useState(false);

    const selectedCoin = useMemo(() => locationToCoin(location), [location]);

    const selectedCoinTicker = useMemo(() => (selectedCoin ? tickers.get(selectedCoin.key) : null),
        [tickers, selectedCoin])

    const api: SocketApi = useMemo(
        () => ({
            connected,
            tickers,

        }),
        [connected, tickers]
    )


    return <SocketContext.Provider value={api}>{children}</SocketContext.Provider>
}