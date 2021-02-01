import React from "react";
import classNames from 'classnames';

export interface OrderBookFlexSideProps {
    prefixCls?: string;
    className?: string;
    border?: boolean;
}

export const OrderBookFlexSide: React.FC<OrderBookFlexSideProps> = (
    {
        prefixCls = 'ui-order-book-flex-side',
        className,
        border,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-border`]: border,
        },
        className
    );

    return (
        <div className={classes}>
            {children}
        </div>
    )
};
