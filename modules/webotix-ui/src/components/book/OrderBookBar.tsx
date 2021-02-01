import React from "react";
import classNames from 'classnames';
import {OrderBookDirection} from "./types";

export interface OrderBookBarProps {
    prefixCls?: string;
    className?: string;
    direction: OrderBookDirection;
    size: number;
}

export const OrderBookBar: React.FC<OrderBookBarProps> = (
    {
        prefixCls = 'ui-order-book-bar',
        className,
        direction,
        size,
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
        <div style={{width: `${size}%`}} className={classes}>
            {children}
        </div>
    )
};
