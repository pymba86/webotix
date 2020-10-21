import React, {useContext, useEffect, useMemo, useState} from "react";
import {MarketApi, MarketContext} from "./MarketContext";
import {Exchange} from "./types";
import {LogContext} from "../log/LogContext";
import exchangeService from "./exchangeService";
import {SocketContext} from "../socket/SocketContext";


export const Market: React.FC = ({ children}) => {

    const logApi = useContext(LogContext);
    const socketApi = useContext(SocketContext);

    const [exchanges, setExchanges] = useState<Array<Exchange>>([]);

    const coin = socketApi.selectedCoin;

    const selectedExchange = useMemo(() => {
        return !coin ? undefined : exchanges.find(e => e.code === coin.exchange)
    }, [coin, exchanges]);

    const trace = logApi.trace;
    const errorPopup = logApi.errorPopup;

    const refreshExchanges = useMemo(
        () => () => {
            trace("Fetching exchanges");
            exchangeService.fetchExchanges()
                .then((exchanges: Array<Exchange>) => {
                    setExchanges(exchanges);
                    trace("Fetched " + exchanges.length + " exchanges");
                })
                .catch((error: Error) => {
                    errorPopup(error.message);
                })
        }, [errorPopup, trace, setExchanges]
    )

    useEffect(() => {
        refreshExchanges()
    }, [refreshExchanges])


    const api: MarketApi = useMemo(
        () => ({
            data: {
                exchanges,
                selectedExchange
            },
            actions: {
                refreshExchanges
            }
        }),
        [exchanges, refreshExchanges, selectedExchange]
    );


    return <MarketContext.Provider value={api}>{children}</MarketContext.Provider>
};