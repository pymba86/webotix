import React, {useContext, useEffect, useRef} from "react";
import {SocketContext} from "../modules/socket/SocketContext";

export interface OrderBookContainerProps {
    animate: boolean;
}

export const OrderBookContainer: React.FC<OrderBookContainerProps> = (
    {animate}) => {

    const socketApi = useContext(SocketContext);

    const orderBook = socketApi.orderBook;
    const coin = socketApi.selectedCoin;

    const largestOrder = useRef(0);

    useEffect(() => {
        largestOrder.current = 0
    }, [coin]);

    if (orderBook) {
        largestOrder.current = Math.max(
            ...orderBook.bids.map(o => o.remainingAmount),
            ...orderBook.asks.map(o => o.remainingAmount),
            largestOrder.current
        )
    }

    return (
        <div>order book</div>
    )
};