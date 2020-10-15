import React from "react";
import {Coin} from "../../modules/market";
import classNames from 'classnames';
import {Amount} from "../../elements/amount";
import {Map} from "immutable";
import {Balance, Ticker} from "../../modules/socket";
import {CoinMetadata} from "../../modules/server";

export interface BalanceInfoProps {
    prefixCls?: string;
    className?: string;
    coin?: Coin;
    balances: Map<String, Balance>;
    ticker?: Ticker;
}

const LOG_10 = Math.log(10);
const log10 = (x: number) => Math.log(1 / x) / LOG_10;
const scaleOfValue = (x: number) => Math.floor(log10(x));

export const BalanceInfo: React.FC<BalanceInfoProps> = (
    {
        prefixCls = 'ui-balance-info',
        className,
        balances,
        ticker,
        coin
    }) => {

    if (coin) {

        const noBalances = !balances || !coin;

        const counterScale = (meta: CoinMetadata) =>
            (meta.minimumAmount ? scaleOfValue(meta.minimumAmount) : 8);

        const baseBalance = noBalances ? null : balances.get(coin.base);
        const baseCounter = noBalances ? null : balances.get(coin.counter);

        return (
            <div className={classNames(prefixCls, className)}>

                <Amount heading={true}
                        name={"Balance value"} coin={coin}
                        value={baseBalance ? baseBalance.total : undefined}/>

                <Amount heading={true}
                        name={"Can sell"} coin={coin}
                        value={baseBalance ? baseBalance.available : undefined}/>

                <Amount heading={true}
                        name={"Sale value"} coin={coin}
                        value={baseBalance && ticker ?
                            +Number(baseBalance.total * ticker.bid).toFixed(8)
                            : undefined}/>

                <Amount heading={true}
                        name={coin.counter + " balance"} coin={coin}
                        value={baseCounter ? baseCounter.total : undefined}/>

                <Amount heading={true}
                        name={coin.counter + " available"} coin={coin}
                        value={baseCounter ? baseCounter.available : undefined}/>

                <Amount heading={true}
                        deriveScale={counterScale}
                        name={"Can buy"} coin={coin}
                        value={baseCounter && ticker ? baseCounter.available / ticker.ask : undefined}/>
            </div>
        )
    } else {
        return <div>No coin selected</div>
    }
}