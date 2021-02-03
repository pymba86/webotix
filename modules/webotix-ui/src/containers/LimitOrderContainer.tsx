import React, {useState, useContext, useMemo} from "react"
import {Form} from "../elements/form";
import {Input} from "../elements/input";
import {Coin} from "../modules/market";
import {Checkbox} from "../elements/checkbox";
import {Button} from "../elements/button";

export interface LimitOrderContainerProps {
    coin: Coin;
}

export const LimitOrderContainer: React.FC<LimitOrderContainerProps> = (
    {
        coin
    }
) => {

    const [limitPrice, setLimitPrice] = useState<string>("");
    const [amount, setAmount] = useState<string>("");

    return (
        <Form>
            <Form.Item
                labelAlign={'left'}
                label={`Calculate amount (${coin.base})`}>
                <Form.Group>
                    <Button
                        variant={"sell"}>
                        Max Sell
                    </Button>
                    <Button
                        variant={"buy"}>
                        Max Buy
                    </Button>
                </Form.Group>
            </Form.Item>
            <Form.Item
                required={true}
                label={`Limit price (${coin.counter})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter price..."}
                    onChange={setLimitPrice}
                    value={limitPrice}/>
            </Form.Item>


            <Form.Item
                required={true}
                label={`Amount (${coin.base})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter amount..."}
                    onChange={setAmount}
                    value={amount}/>
            </Form.Item>
            <Form.Group>
                <Button
                    variant={"sell"}>
                    Sell
                </Button>
                <Button
                    variant={"buy"}>
                    Buy
                </Button>
            </Form.Group>
        </Form>
    )
}
