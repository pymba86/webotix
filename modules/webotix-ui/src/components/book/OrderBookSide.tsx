import React from "react";
import {Coin} from "../../modules/market";
import {LimitOrder} from "../../modules/socket";
import {OrderBookDirection} from "./types";
import {OrderBookAligned} from "./OrderBookAligned";
import {OrderBookBarColumn} from "./OrderBookBarColumn";
import {OrderBookBar} from "./OrderBookBar";
import {OrderBookBarSize} from "./OrderBookBarSize";
import classNames from "classnames";
import {OrderBookPriceColumn} from "./OrderBookPriceColumn";
import {Amount} from "../../elements/amount";


export interface OrderBookSideProps {
    prefixCls?: string;
    className?: string;
    coin?: Coin;
    orders: Array<LimitOrder>;
    direction: OrderBookDirection;
    largestOrder: number;
    animate: boolean;
}

export const OrderBookSide: React.FC<OrderBookSideProps> = (
    {
        prefixCls = 'ui-order-book-side',
        className,
        animate,
        orders,
        direction,
        largestOrder,
        coin
    }) => {

    return (
        <div className={classNames(prefixCls, className)}>
            {orders.map(order => {

                    const magnitude = (order.remainingAmount * 100.0) / largestOrder;

                    return (
                        <OrderBookAligned key={direction + '-' + order.limitPrice}
                                          direction={direction}>
                            <OrderBookBarColumn direction={direction}>
                                <OrderBookBar direction={direction}
                                              className={'orderbook-bar'}
                                              size={magnitude}>
                                    <OrderBookBarSize direction={direction}
                                                      size={magnitude}
                                                      className="orderbook-value">
                                        {order.remainingAmount}
                                    </OrderBookBarSize>
                                </OrderBookBar>
                            </OrderBookBarColumn>
                            <OrderBookPriceColumn direction={direction}>
                                <Amount noflash={true}
                                        className="orderbook-value"
                                        color={direction === "bid" ? "buy" : "sell"}
                                        coin={coin}
                                        value={order.limitPrice}/>
                            </OrderBookPriceColumn>
                        </OrderBookAligned>
                    )
                }
            )}
        </div>
    )
};
