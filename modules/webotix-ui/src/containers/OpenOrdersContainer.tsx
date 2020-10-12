import React, {useContext, useMemo} from "react";
import {Coin} from "../modules/market";
import {SocketContext} from "../modules/socket/SocketContext";
import {DisplayOrder, OrderStatus, OrderType, RunningAtType} from "../modules/socket";
import {OpenOrders} from "../components/orders";

export interface OpenOrdersContainerProps {
    coin: Coin;
}

export const OpenOrdersContainer: React.FC<OpenOrdersContainerProps> = ({coin}) => {

    const socketApi = useContext(SocketContext);

    const openOrders = socketApi.openOrders;

    const allOrders = useMemo<DisplayOrder[]>(
        () =>
            openOrders.filter(o => !o.deleted).map(o => ({...o, runningAt: RunningAtType.EXCHANGE, jobId: ''})),
        [openOrders]
    );

    const jobsAsOrders: DisplayOrder[] = [

        {
            status: OrderStatus.PENDING_NEW, timestamp: 1232132132131, serverTimestamp: 213213213213213,
            deleted: false, averagePrice: 213, cumulativeAmount: 213, currencyPair: {
                counter: "BCD", base: "SAD"
            }, fee: 21, id: "213", jobId: "13", limitPrice: 213, originalAmount: 213, remainingAmount: 213,
            runningAt: RunningAtType.SERVER, stopPrice: 0, type: OrderType.ASK
        },
    ];

    const orders: DisplayOrder[] = coin && allOrders ? allOrders.concat(jobsAsOrders) : []

    return (
        <OpenOrders
            orders={orders}
            onCancelExchange={(id: string) => console.log(id)}
            onCancelServer={(id: string) => console.log(id)}
            coin={coin}
        />
    )
};