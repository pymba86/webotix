import React, {useContext, useState} from "react";
import {Section, SectionTab} from "../elements/section";
import {MarketContext} from "../modules/market/MarketContext";
import {getFromLS, saveToLS} from "../modules/common/localStorage";
import {SocketContext} from "../modules/socket/SocketContext";
import {Coin} from "../modules/market";
import {LimitOrderContainer} from "./LimitOrderContainer";
import {TrailingStopOrderContainer} from "./TrailingStopOrderContainer";

const TRADE_SELECTED_KEY = "Trade.selected";

export type TradeSelector = 'limit' | 'stop' | 'trailing' | 'oco';

export const TradeContainer: React.FC = () => {

    const marketApi = useContext(MarketContext);
    const socketApi = useContext(SocketContext);

    const exchange = marketApi.data.selectedExchange;
    const selectedCoin = socketApi.selectedCoin;

    const [selected, setSelected] = useState<TradeSelector>(
        getFromLS(TRADE_SELECTED_KEY) || "limit"
    );

    const tradeMarkup = (coin: Coin, selected: TradeSelector) => {
        if (selected === "limit") {
            return <LimitOrderContainer key={coin.key} coin={coin}/>
        } else if (selected === "stop") {
            return <div>limit</div>
        } else if (selected === "trailing") {
            return <TrailingStopOrderContainer key={coin.key} coin={coin}/>
        } else if (selected === "oco") {
            return <div>limit</div>
        }
        return null
    };

    return (
        <Section id={"trading"}
                 heading={exchange && exchange.authenticated ? "Trading" : "Paper Trading"}
                 buttons={() => (
                     <React.Fragment>
                         <SectionTab
                             selected={selected === 'limit'}
                             onClick={() => setSelected(saveToLS(TRADE_SELECTED_KEY, 'limit'))}>
                             Limit
                         </SectionTab>
                         <SectionTab
                             selected={selected === 'stop'}
                             onClick={() => setSelected(saveToLS(TRADE_SELECTED_KEY, 'stop'))}>
                             Stop
                         </SectionTab>
                         <SectionTab
                             selected={selected === 'trailing'}
                             onClick={() => setSelected(saveToLS(TRADE_SELECTED_KEY, 'trailing'))}>
                             Trailing stop
                         </SectionTab>
                         <SectionTab
                             selected={selected === 'oco'}
                             onClick={() => setSelected(saveToLS(TRADE_SELECTED_KEY, 'oco'))}>
                             OCO
                         </SectionTab>
                     </React.Fragment>
                 )}>
            {selectedCoin ? tradeMarkup(selectedCoin, selected) : (
                <div>No coin selected</div>
            )}
        </Section>

    )
};
