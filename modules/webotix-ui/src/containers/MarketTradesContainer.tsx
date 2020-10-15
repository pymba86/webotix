import React, {useContext} from "react";
import {SocketContext} from "../modules/socket/SocketContext";
import {TradeHistory} from "../components/trade";

export const MarketTradesContainer: React.FC = () => {

    const trades = useContext(SocketContext).trades;

    return (
        <TradeHistory trades={trades} excludeFees={true}/>
    )
};