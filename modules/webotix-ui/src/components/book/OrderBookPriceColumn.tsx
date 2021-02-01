import React from "react";
import classNames from 'classnames';
import {OrderBookDirection} from "./types";

export interface OrderBookPriceColumnProps {
    prefixCls?: string;
    className?: string;
    direction: OrderBookDirection;
}

export const OrderBookPriceColumn: React.FC<OrderBookPriceColumnProps> = (
    {
        prefixCls = 'ui-order-book-price-column',
        className,
        direction,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-direction-${direction}`]: direction,
        },
        className
    );

    return (
        <div className={classes}>
            {children}
        </div>
    )
};
