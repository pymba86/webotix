import React from "react";
import classNames from 'classnames';
import {OrderBookDirection} from "./types";

export interface OrderBookBarColumnProps {
    prefixCls?: string;
    className?: string;
    direction: OrderBookDirection;
}

export const OrderBookBarColumn: React.FC<OrderBookBarColumnProps> = (
    {
        prefixCls = 'ui-order-book-bar-column',
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
