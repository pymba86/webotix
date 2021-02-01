import React, {useContext, useEffect, useRef} from "react";
import {SocketContext} from "../modules/socket/SocketContext";
import {OrderBookFlexSide, OrderBookSide, OrderBookSplit} from "../components/book";

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

    if (coin) {

        if (orderBook) {
            largestOrder.current = Math.max(
                ...orderBook.bids.map(o => o.remainingAmount),
                ...orderBook.asks.map(o => o.remainingAmount),
                largestOrder.current
            )

            return (
                <OrderBookSplit>
                    <OrderBookFlexSide>
                        <OrderBookSide
                            key={'asks'}
                            coin={coin}
                            orders={orderBook.bids}
                            direction={'bid'}
                            largestOrder={largestOrder.current}
                            animate={animate}>

                        </OrderBookSide>
                    </OrderBookFlexSide>
                    <OrderBookFlexSide border={true}>
                        <OrderBookSide
                            key={'buys'}
                            coin={coin}
                            orders={orderBook.asks}
                            direction={'ask'}
                            largestOrder={largestOrder.current}
                            animate={animate}>
                        </OrderBookSide>
                    </OrderBookFlexSide>
                </OrderBookSplit>
            )
        }

        return (
            <div>Empty order book</div>
        )
    }
    return (
        <div>No coin selected</div>
    )
};
