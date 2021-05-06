import React, {useContext, useMemo} from "react";
import {SocketContext} from "../modules/socket/SocketContext";
import { Order, OrdersType, RunningAtType} from "../modules/socket";
import {OpenOrders} from "../components/orders";
import {AuthContext} from "../modules/auth/AuthContext";
import {LogContext} from "../modules/log/LogContext";
import {ServerContext} from "../modules/server/ServerContext";
import {Coin} from "../modules/market";
import exchangeService from "../modules/market/exchangeService";

export const OpenOrdersContainer: React.FC = () => {

    const socketApi = useContext(SocketContext);
    const authApi = useContext(AuthContext);
    const logApi = useContext(LogContext);
    const serverApi = useContext(ServerContext);

    const openOrders = socketApi.openOrders;
    const selectedCoin = socketApi.selectedCoin;

    const allOrders = useMemo<Order[]>(
        () =>
            openOrders.filter(o => !o.deleted)
                .map(o => ({...o, kind: 'order', runningAt: RunningAtType.EXCHANGE, jobId: ''})),
        [openOrders]
    );

    const orders: OrdersType[] = selectedCoin && allOrders ? allOrders : [];

    const authenticatedRequest = authApi.authenticatedRequest;
    const logPopup = logApi.errorPopup;
    const pendingCancelOrder = socketApi.pendingCancelOrder;

    const onCancelExchange = useMemo(
        () => (id: string, coin: Coin) => {

            const order = openOrders.find(o => o.id === id);

            if (order) {
                pendingCancelOrder(
                    order.id,
                    // Deliberately new enough to be relevant now but get immediately overwritten
                    order.serverTimestamp + 1
                )
                authenticatedRequest(() => exchangeService
                    .cancelOrder(coin, id))
                    .catch(error =>
                        logPopup("Could not cancel order: " + error.message)
                    )
            } else {
                logPopup("Could not cancel order by order not found");
            }
        },
        [authenticatedRequest, logPopup, pendingCancelOrder, openOrders]
    )

    if (selectedCoin) {
        return (
            <OpenOrders
                orders={orders}
                onCancelExchange={(id: string) => onCancelExchange(id, selectedCoin)}
                onCancelServer={(id: string) => serverApi.deleteJob(id)}
                coin={selectedCoin}
            />
        )
    }

    return (
        <div>No coin selected</div>
    )
};
