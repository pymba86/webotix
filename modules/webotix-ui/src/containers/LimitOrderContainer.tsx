import React, {useContext, useMemo, useState} from "react"
import {Form} from "../elements/form";
import {Input} from "../elements/input";
import {Coin} from "../modules/market";
import {Button} from "../elements/button";
import {FrameworkContext} from "../FrameworkContainer";
import exchangeService from "../modules/market/exchangeService";
import {LogContext} from "../modules/log/LogContext";
import {isValidNumber} from "../modules/common/number";
import {SocketContext} from "../modules/socket/SocketContext";
import {AuthContext} from "../modules/auth/AuthContext";
import {Order, OrderType} from "../modules/socket";

export interface LimitOrderContainerProps {
    coin: Coin;
}

export const LimitOrderContainer: React.FC<LimitOrderContainerProps> = (
    {
        coin
    }
) => {

    const frameworkApi = useContext(FrameworkContext);
    const socketApi = useContext(SocketContext);
    const authApi = useContext(AuthContext);
    const logApi = useContext(LogContext);

    const [limitPrice, setLimitPrice] = useState<string>("");
    const [amount, setAmount] = useState<string>("");

    const onFocusLimitPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setLimitPrice(String(value)))
    }

    const onFocusAmount = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setAmount(String(value)))
    }

    const onSubmit = (direction: string) => {
        const order = createOrder(direction)
        socketApi.createPlaceholder(order)
        authApi
            .authenticatedRequest(
                () => exchangeService.submitOrder(coin.exchange, order))
            .catch(error => {
                socketApi.removePlaceholder()
                logApi.errorPopup("Could not submit order: " + error.message)
            })
    }

    const createOrder = (direction: string): any => ({
        type: direction === "BUY" ? OrderType.BID : OrderType.ASK,
        counter: coin.counter,
        base: coin.base,
        amount: amount,
        limitPrice: Number(limitPrice)
    })

    const calculateOrder = async (direction: string) => {
        try {
            const response = await exchangeService.calculateOrder(
                coin.exchange,
                createOrder(direction)
            )
            setAmount(response.amount);
        } catch (error) {
            console.log(error);
            logApi.errorPopup(error.message)
        }
    }

    const limitPriceValid = useMemo(() => {
        return Boolean(limitPrice) && isValidNumber(limitPrice) && Number(limitPrice) > 0
    }, [limitPrice, setLimitPrice]);

    const amountValid = useMemo(() => {
        return Boolean(amount) && isValidNumber(amount) && Number(amount) > 0
    }, [amount, setAmount]);

    const valid = limitPriceValid && amountValid;

    return (
        <Form>
            <Form.Item
                labelAlign={'left'}
                label={`Calculate amount (${coin.base})`}>
                <Form.Group>
                    <Button
                        outline={true}
                        type={'button'}
                        disabled={!limitPriceValid}
                        onClick={() => calculateOrder('SELL')}
                        variant={"sell"}>
                        Max Sell
                    </Button>
                    <Button
                        outline={true}
                        type={'button'}
                        disabled={!limitPriceValid}
                        onClick={() => calculateOrder('BUY')}
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
                    onFocus={onFocusLimitPrice}
                    value={limitPrice}/>
            </Form.Item>
            <Form.Item
                required={true}
                label={`Amount (${coin.base})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter amount..."}
                    onChange={setAmount}
                    onFocus={onFocusAmount}
                    value={amount}/>
            </Form.Item>
            <Form.Group>
                <Button
                    disabled={!valid}
                    type={'button'}
                    onClick={() => onSubmit('SELL')}
                    variant={"sell"}>
                    Sell
                </Button>
                <Button
                    disabled={!valid}
                    onClick={() => onSubmit('BUY')}
                    type={'button'}
                    variant={"buy"}>
                    Buy
                </Button>
            </Form.Group>
        </Form>
    )
}
