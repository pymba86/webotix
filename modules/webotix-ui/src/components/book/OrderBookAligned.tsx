import React from "react";
import classNames from 'classnames';
import {OrderBookDirection} from "./types";

export interface OrderBookAlignedProps {
    prefixCls?: string;
    className?: string;
    direction: OrderBookDirection;
}

export const OrderBookAligned: React.FC<OrderBookAlignedProps> = (
    {
        prefixCls = 'ui-order-book-aligned',
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
