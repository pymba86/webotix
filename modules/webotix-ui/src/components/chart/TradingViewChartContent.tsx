import {Coin} from "../../modules/market";
import React from "react";
import classNames from 'classnames';

const CONTAINER_ID = "tradingview-widget-container";

export interface TradingViewChartContentProps {
    coin: Coin;
    interval: string;
    prefixCls?: string;
}

export class TradingViewChartContent extends React.Component<TradingViewChartContentProps, {}> {

    shouldComponentUpdate(nextProps: Readonly<TradingViewChartContentProps>,
                          nextState: Readonly<{}>, nextContext: any): boolean {

        return (
            this.props.coin.key !== nextProps.coin.key ||
            this.props.interval !== nextProps.interval
        )
    }

    componentDidMount = () => {
        this.reLoad()
    }

    componentDidUpdate = () => {
        this.reLoad()
    }

    reLoad = () => {
        const element = document.getElementById(CONTAINER_ID);
        if (element) {
            element.innerHTML = ""
            this.initWidget()
        }
    }

    initWidget = () => {
        try {
            new (window as any).TradingView.widget({
                autosize: true,
                symbol: this.symbol(),
                interval: this.props.interval,
                timezone: "Asia/Ashkhabad",
                theme: "Dark",
                style: "1",
                locale: "en",
                toolbar_bg: "#131722",
                enable_publishing: false,
                save_image: true,
                show_popup_button: true,
                popup_width: "1000",
                popup_height: "650",
                container_id: CONTAINER_ID,
                hide_side_toolbar: false,
                studies: [],
                details: true
            })
        } catch (error) {
            console.error("Failed to initialise TradingView widget", error)
        }
    }

    symbol = () => {
        return this.props.coin.exchange.toUpperCase()
            + ":" + this.props.coin.base + this.props.coin.counter
    }

    render() {

        const {
            prefixCls = 'ui-chart'
        } = this.props;

        return (
            <div className={classNames(prefixCls, `${prefixCls}-outer`)}>
                <div className={classNames(prefixCls, `${prefixCls}-inner`)}
                     id={CONTAINER_ID}/>
            </div>
        )
    }
}
