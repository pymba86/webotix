import React from "react";
import classNames from 'classnames';
import {ToolbarSpace} from "./ToolbarSpace";
import {ToolbarTicker} from "./ToolbarTicker";
import {Coin, Exchange} from "../../modules/market";
import {Balance, Ticker} from "../../modules/socket";
import {Panel} from "../../config";
import {BalanceInfo} from "../balance";
import {ToolbarPanel} from "./ToolbarPanel";
import {Map} from "immutable";
import {ToolbarSocket} from "./ToolbarSocket";
import {ToolbarLink} from "./ToolbarLink";
import {ToolbarCoin} from "./ToolbarCoin";
import {ToolbarLogo} from "./ToolbarLogo";

export interface ToolbarProps {
    prefixCls?: string;
    className?: string;
    mobile: boolean;
    updateFocusedField?: (value: number) => void;
    coin?: Coin;
    ticker?: Ticker;
    onShowPanel: (key: string) => void;
    width: number;

    onLogout(): void;

    balances: Map<String, Balance>;
    hiddenPanels: Panel[];
    exchange?: Exchange;
}

export const Toolbar: React.FC<ToolbarProps> = (
    {
        prefixCls = 'ui-toolbar',
        mobile,
        coin,
        ticker,
        updateFocusedField,
        hiddenPanels,
        onShowPanel,
        onLogout,
        width,
        balances,
        exchange,
        className
    }) => {

    const boxClasses = classNames(
        prefixCls,
        {
            [`${prefixCls}-box`]: true,
        },
        className
    );

    return (
        <div className={boxClasses}>
            <ToolbarLogo icon={"layers"}/>
            <ToolbarCoin coin={coin} exchange={exchange}/>
            {!mobile && coin && (
                <ToolbarSpace box={true} border={true}>
                    <ToolbarTicker mobile={mobile}
                                   coin={coin}
                                   ticker={ticker}
                                   onClickNumber={number => {
                                       if (updateFocusedField) {
                                           updateFocusedField(number);
                                       }
                                   }}/>
                </ToolbarSpace>
            )}
            {!mobile && coin && hiddenPanels
                .filter(p => p.key === "balance")
                .map(panel => (
                    <React.Fragment key={panel.key}>
                        {width >= 1440 && (
                            <ToolbarSpace border={true} leftAlign={true}>
                                <ToolbarPanel
                                    margin={false}
                                    key={panel.key}
                                    panel={panel}
                                    onClick={() => onShowPanel(panel.key)}
                                />
                                <BalanceInfo coin={coin} balances={balances} ticker={ticker}/>
                            </ToolbarSpace>
                        )}
                        {width < 1440 && (
                            <ToolbarPanel
                                margin={false}
                                key={panel.key}
                                panel={panel}
                                onClick={() => onShowPanel(panel.key)}
                            />
                        )}
                    </React.Fragment>
                ))}
            {!mobile &&
            hiddenPanels
                .filter(p => p.key !== "balance")
                .map(panel => (
                    <ToolbarPanel
                        margin={true}
                        key={panel.key}
                        panel={panel}
                        onClick={() => onShowPanel(panel.key)}
                    />
                ))}
            <ToolbarSocket connected={true}/>
            <ToolbarLink icon={"code"} onClick={onLogout}/>
            <ToolbarLink icon={"layout"} onClick={onLogout}/>
            <ToolbarLink icon={"lock"} onClick={onLogout}/>
        </div>
    )
};