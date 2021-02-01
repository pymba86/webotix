import React from "react";
import classNames from 'classnames';
import {OrderBookDirection} from "./types";

export interface OrderBookBarSizeProps {
    prefixCls?: string;
    className?: string;
    direction: OrderBookDirection;
    size: number;
}

export const OrderBookBarSize: React.FC<OrderBookBarSizeProps> = (
    {
        prefixCls = 'ui-order-book-bar-size',
        className,
        direction,
        size,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-direction-${direction}`]: direction,
            [`${prefixCls}-size-auto`]: size > 50,
        },
        className
    );

    return (
        <div className={classes}>
            {children}
        </div>
    )
};
