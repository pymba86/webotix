import React from "react";
import {Section} from "../elements/section";
import {Balance} from "../components/balance";

export const BalanceContainer: React.FC = () => {

    return (
        <Section id={"balance"}
                 heading={"Balances"}>
            <Balance coin={
                {
                    base: "123",
                    counter: "213",
                    shortName: "213",
                    name: "213",
                    key: "213",
                    exchange: "213"
                }}/>
        </Section>

    )
}