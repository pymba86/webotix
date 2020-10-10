import React from "react";
import {Section} from "../elements/section";
import {BalanceInfo} from "../components/balance";

export const BalanceContainer: React.FC = () => {

    return (
        <Section id={"balance"}
                 heading={"Balances"}>
            <BalanceInfo/>
        </Section>

    )
}