import React from "react";
import classNames from 'classnames';

export interface OrderBookSplitProps {
    prefixCls?: string;
    className?: string;
}

export const OrderBookSplit: React.FC<OrderBookSplitProps> = (
    {
        prefixCls = 'ui-order-book-split',
        className,
        children
    }) => {

    return (
        <div className={classNames(prefixCls, className)}>
            {children}
        </div>
    )
};
