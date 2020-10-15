import React, {useContext} from "react";
import {Section} from "../elements/section";
import {BalanceInfo} from "../components/balance";
import {SocketContext} from "../modules/socket/SocketContext";

export const BalanceContainer: React.FC = () => {

    const socketApi = useContext(SocketContext);

    const ticker = socketApi.selectedCoinTicker;
    const balances = socketApi.balances;
    const selectedCoin = socketApi.selectedCoin;

    return (
        <Section id={"balance"}
                 heading={"Balances"}>
            {selectedCoin ? <BalanceInfo coin={selectedCoin}
                                         balances={balances} ticker={ticker}/>
                : <div>No coin selected</div>}
        </Section>

    )
}