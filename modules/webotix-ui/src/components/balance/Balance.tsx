import React from "react";
import {Coin} from "../../modules/market";
import classNames from 'classnames';
import {Amount} from "../../elements/amount";

export interface BalanceProps {
    prefixCls?: string;
    className?: string;
    coin: Coin;
}

const LOG_10 = Math.log(10);
const log10 = (x: number) => Math.log(1 / x) / LOG_10;
const scaleOfValue = (x: number) => Math.floor(log10(x));

export const Balance: React.FC<BalanceProps> = ({
                                                    prefixCls = 'ui-balance',
                                                    className, coin
                                                }) => {

    const classes = classNames(prefixCls,
        `${prefixCls}-container`, className);

    if (coin) {

        return (
            <div className={classes}>
                <Amount heading={true} scale={0} name={"Balance value"} coin={coin} value={1000}/>
                <Amount heading={true} scale={0} name={"Can sell"} coin={coin} value={200}/>
                <Amount heading={true} scale={0} name={"Sale value"} coin={coin} value={55}/>
                <Amount heading={true} scale={0} name={"USD balance"} coin={coin} value={213}/>
                <Amount heading={true} scale={0} name={"USD available"} coin={coin} value={65}/>
                <Amount heading={true} scale={0} name={"Can buy"} coin={coin} value={656}/>
            </div>
        )
    } else {
        return <div>No coin selected</div>
    }
}