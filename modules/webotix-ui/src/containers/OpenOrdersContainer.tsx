import React, {useContext, useMemo} from "react";
import {Coin} from "../modules/market";
import {SocketContext} from "../modules/socket/SocketContext";
import {DisplayOrder, RunningAtType} from "../modules/socket";
import {OpenOrders} from "../components/orders";

export interface OpenOrdersContainerProps {
    coin: Coin;
}

export const OpenOrdersContainer: React.FC<OpenOrdersContainerProps> = ({coin}) => {

    const socketApi = useContext(SocketContext);

    const openOrders = socketApi.openOrders;

    const allOrders = useMemo<DisplayOrder[]>(
        () =>
            openOrders.filter(o => !o.deleted)
                .map(o => ({...o, runningAt: RunningAtType.EXCHANGE, jobId: ''})),
        [openOrders]
    );

    const jobsAsOrders: DisplayOrder[] = [];

    const orders: DisplayOrder[] = coin && allOrders ? allOrders.concat(jobsAsOrders) : [];

    return (
        <OpenOrders
            orders={allOrders}
            onCancelExchange={(id: string) => console.log(id)}
            onCancelServer={(id: string) => console.log(id)}
            coin={coin}
        />
    )
};