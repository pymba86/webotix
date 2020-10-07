import React, {useContext} from "react";
import {SocketContext} from "../modules/socket/SocketContext";
import {TradeHistory} from "../components/trade";

export const TradeHistoryContainer: React.FC = () => {

    const userTrades = useContext(SocketContext).userTrades;
    return (
        <TradeHistory trades={userTrades} excludeFees={false}/>
    )
};