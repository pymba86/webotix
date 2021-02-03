import React, {useContext, useMemo} from "react";
import {SocketContext} from "../modules/socket/SocketContext";
import {JobOrder, Order, OrdersType, OrderType, RunningAtType} from "../modules/socket";
import {OpenOrders} from "../components/orders";
import {AuthContext} from "../modules/auth/AuthContext";
import {LogContext} from "../modules/log/LogContext";
import {ServerContext} from "../modules/server/ServerContext";
import {Coin, ServerCoin} from "../modules/market";
import exchangeService from "../modules/market/exchangeService";
import {LimitOrderJob, OcoJob, TradeDirection} from "../modules/server";
import {isStop} from "../utils/jobUtils";

function jobTriggerMatchesCoin(job: OcoJob, coin: ServerCoin) {
    return (
        job.tickTrigger.exchange === coin.exchange &&
        job.tickTrigger.base === coin.base &&
        job.tickTrigger.counter === coin.counter
    )
}

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
                .map(o => ({...o, runningAt: RunningAtType.EXCHANGE, jobId: ''})),
        [openOrders]
    );

    const allJobs = serverApi.jobs;

    const jobsAsOrders = useMemo<JobOrder[]>(() => {
        if (!selectedCoin) return []
        return allJobs
            .filter(job => isStop(job))
            .map(job => job as OcoJob)
            .filter(job => jobTriggerMatchesCoin(job, selectedCoin))
            .map(job => ({
                kind: 'job',
                runningAt: RunningAtType.SERVER,
                jobId: job.id,
                type: job.high
                    ? (job.high.job as LimitOrderJob).direction === TradeDirection.BUY
                        ? OrderType.BID
                        : OrderType.ASK
                    : (job.low.job as LimitOrderJob).direction === TradeDirection.BUY
                        ? OrderType.BID
                        : OrderType.ASK,
                stopPrice: job.high ? Number(job.high.thresholdAsString) : Number(job.low.thresholdAsString),
                limitPrice: job.high
                    ? Number((job.high.job as LimitOrderJob).limitPrice)
                    : Number((job.low.job as LimitOrderJob).limitPrice),
                originalAmount: job.high
                    ? Number((job.high.job as LimitOrderJob).amount)
                    : Number((job.low.job as LimitOrderJob).amount),
                remainingAmount: job.high
                    ? Number((job.high.job as LimitOrderJob).amount)
                    : Number((job.low.job as LimitOrderJob).amount)
            }))
    }, [allJobs, selectedCoin])

    const orders: OrdersType[] = selectedCoin && allOrders ? [...allOrders, ...jobsAsOrders] : [];

    const authenticatedRequest = authApi.authenticatedRequest;
    const logPopup = logApi.errorPopup;
    const pendingCancelOrder = socketApi.pendingCancelOrder;

    const onCancelExchange = useMemo(
        () => (id: string, coin: Coin) => {

            const order = openOrders.find(o => o.id === id);
            if (order) {
                pendingCancelOrder(
                    id,
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
