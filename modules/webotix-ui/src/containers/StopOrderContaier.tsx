import {Coin} from "../modules/market";
import React, {useContext, useMemo, useState} from "react";
import {Form} from "../elements/form";
import {Button} from "../elements/button";
import {Input} from "../elements/input";
import {FrameworkContext} from "../FrameworkContainer";
import {isValidNumber} from "../modules/common/number";
import {ServerContext} from "../modules/server/ServerContext";
import {JobType, OcoJob, TradeDirection} from "../modules/server";
import {v4 as uuidv4} from "uuid";

export interface StopOrderContainerProps {
    coin: Coin;
}

export const StopOrderContainer: React.FC<StopOrderContainerProps> = (
    {
        coin
    }
) => {

    const frameworkApi = useContext(FrameworkContext);
    const serverApi = useContext(ServerContext);

    const [stopPrice, setStopPrice] = useState<string>("");
    const [limitPrice, setLimitPrice] = useState<string>("");
    const [amount, setAmount] = useState<string>("");

    const createJob = (direction: TradeDirection): OcoJob => ({
        jobType: JobType.SOFT_TRAILING_STOP,
        id: uuidv4(),
        tickTrigger: {
            exchange: coin.exchange,
            counter: coin.counter,
            base: coin.base
        },
        [direction === "BUY" ? "high" : "low"]: {
            thresholdAsString: Number(stopPrice),
            job: {
                jobType: JobType.LIMIT_ORDER,
                id: uuidv4(),
                direction: direction,
                tickTrigger: {
                    exchange: coin.exchange,
                    counter: coin.counter,
                    base: coin.base
                },
                amount: Number(amount),
                limitPrice: Number(limitPrice)
            }
        }
    });

    const onSubmit = (direction: TradeDirection) => {
        serverApi.submitJob(createJob(direction))
    }

    const onFocusAmount = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setAmount(String(value)))
    }

    const onFocusStopPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setStopPrice(String(value)))
    }

    const onFocusLimitPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setLimitPrice(String(value)))
    }

    const limitPriceValid = useMemo(() => {
        return Boolean(limitPrice) && isValidNumber(limitPrice)
            && Number(limitPrice) > 0
    }, [limitPrice, setLimitPrice]);

    const amountValid = useMemo(() => {
        return Boolean(amount) && isValidNumber(amount)
            && Number(amount) > 0
    }, [amount, setAmount]);

    const stopPriceValid = useMemo(() => {
        return Boolean(stopPrice) && isValidNumber(stopPrice)
            && Number(stopPrice) > 0
    }, [stopPrice, setStopPrice]);

    const valid = stopPriceValid && limitPriceValid && amountValid;

    return (
        <Form>
            <Form.Item
                required={true}
                label={`Stop price (${coin.counter})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter price..."}
                    onChange={setStopPrice}
                    value={stopPrice}
                    onFocus={onFocusStopPrice}
                />
            </Form.Item>
            <Form.Item
                required={true}
                label={`Limit price (${coin.counter})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter percentage..."}
                    onChange={setLimitPrice}
                    value={limitPrice}
                    onFocus={onFocusLimitPrice}/>
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
                    onClick={() => onSubmit(TradeDirection.SELL)}
                    variant={"sell"}>
                    Sell
                </Button>
                <Button
                    disabled={!valid}
                    type={'button'}
                    onClick={() => onSubmit(TradeDirection.BUY)}
                    variant={"buy"}>
                    Buy
                </Button>
            </Form.Group>
        </Form>
    )
}