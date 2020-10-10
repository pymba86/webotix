import React from "react";
import classNames from 'classnames';
import {Coin, Exchange} from "../../modules/market";

export interface ToolbarCoinProps {
    prefixCls?: string;
    className?: string;
    coin?: Coin;
    exchange?: Exchange;
}

export const ToolbarCoin: React.FC<ToolbarCoinProps> = (
    {
        prefixCls = 'ui-toolbar-coin',
        className,
        coin,
        exchange
    }) => {

    return (
        <div className={classNames(`${prefixCls}-container`, className)}>
            <h1 className={classNames(`${prefixCls}-ticker`)}>
                {coin ? coin.base + "/" + coin.counter : ""}
            </h1>
            <h2 className={classNames(`${prefixCls}-exchange`)}>
                {coin && exchange ? exchange.name : ""}
            </h2>
        </div>
    )
};