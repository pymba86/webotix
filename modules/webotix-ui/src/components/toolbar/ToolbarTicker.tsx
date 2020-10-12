import React from "react";
import classNames from 'classnames';
import {Coin} from "../../modules/market";
import {Ticker} from "../../modules/socket";
import {Amount} from "../../elements/amount";

export interface ToolbarTickerProps {
    prefixCls?: string;
    className?: string;
    mobile: boolean;
    coin?: Coin;
    ticker?: Ticker;
    onClickNumber: (value: number) => void;
}

export const ToolbarTicker: React.FC<ToolbarTickerProps> = (
    {
        prefixCls = 'ui-toolbar-ticker',
        className,
        mobile, coin, ticker, onClickNumber
    }) => {

    const boxClasses = classNames(
        prefixCls,
        {
            [`${prefixCls}-container`]: true,
        },
        className
    );

    if (coin) {
        return (
            <div className={boxClasses}>

                <Amount scale={1}
                        coin={coin}
                        heading={true}
                        name={"Bid"}
                        icon={"chevron-up"}
                        onClick={onClickNumber}
                        value={ticker ? ticker.bid : undefined}
                />

                <Amount scale={1}
                        coin={coin}
                        heading={true}
                        name={"Last"}
                        icon={"activity"}
                        onClick={onClickNumber}
                        value={ticker ? ticker.last : undefined}
                />

                <Amount scale={1}
                        coin={coin}
                        heading={true}
                        name={"Ask"}
                        icon={"chevron-down"}
                        onClick={onClickNumber}
                        value={ticker ? ticker.ask : undefined}
                />
                <Amount scale={1}
                        coin={coin}
                        heading={true}
                        name={"Open"}
                        onClick={onClickNumber}
                        value={ticker ? ticker.open : undefined}
                />
                <Amount scale={1}
                        coin={coin}
                        heading={true}
                        name={"24h Low"}
                        onClick={onClickNumber}
                        value={ticker ? ticker.low : undefined}
                />

                <Amount scale={1}
                        coin={coin}
                        heading={true}
                        name={"24h High"}
                        onClick={onClickNumber}
                        value={ticker ? ticker.high : undefined}
                />

            </div>
        )
    } else {
        return <div>No coin selected</div>
    }
};