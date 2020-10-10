import React, {useMemo, useState} from "react";
import {MarketApi, MarketContext} from "./MarketContext";
import {Coin, Exchange} from "./types";

export interface MarketProps {
    coin: Coin | null;
}

export const Market: React.FC<MarketProps> = ({coin, children}) => {

    const [exchanges, setExchanges] = useState<Array<Exchange>>([]);

    const selectedExchange = useMemo(() => {
        return !coin ? undefined : exchanges.find(e => e.code === coin.exchange)
    }, [coin, exchanges]);


    const api: MarketApi = useMemo(
        () => ({
            data: {
                exchanges,
                selectedExchange
            }
        }),
        [exchanges, selectedExchange]
    );

    return <MarketContext.Provider value={api}>{children}</MarketContext.Provider>
};