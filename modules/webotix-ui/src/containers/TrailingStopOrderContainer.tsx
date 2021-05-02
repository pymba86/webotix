import {Coin} from "../modules/market";
import React, {useContext, useMemo, useState} from "react";
import {Form} from "../elements/form";
import {Button} from "../elements/button";
import {Input} from "../elements/input";
import {FrameworkContext} from "../FrameworkContainer";
import {isValidNumber} from "../modules/common/number";
import {ServerContext} from "../modules/server/ServerContext";
import {JobType, SoftTrailingStopJob, TradeDirection} from "../modules/server";
import {v4 as uuidv4} from "uuid";
import {SocketContext} from "../modules/socket/SocketContext";
import {Ticker} from "../modules/socket";

export interface TrailingStopOrderContainerProps {
    coin: Coin;
}

export const TrailingStopOrderContainer: React.FC<TrailingStopOrderContainerProps> = (
    {
        coin
    }
) => {

    const frameworkApi = useContext(FrameworkContext);
    const serverApi = useContext(ServerContext);
    const socketApi = useContext(SocketContext);

    const [stopPrice, setStopPrice] = useState<string>("");
    const [limitPercentage, setLimitPercentage] = useState<string>("");
    const [stopPercentage, setStopPercentage] = useState<string>("");
    const [amount, setAmount] = useState<string>("");

    const currentPrice = (direction: string, ticker: Ticker): number =>
        direction === TradeDirection.BUY
            ? ticker.ask
            : ticker.bid;

    const createJob = (direction: TradeDirection, ticker: Ticker): SoftTrailingStopJob => {

        const lastSyncPrice = currentPrice(direction, ticker);

        return {
            jobType: JobType.SOFT_TRAILING_STOP,
            id: uuidv4(),
            tickTrigger: {
                exchange: coin.exchange,
                counter: coin.counter,
                base: coin.base
            },
            direction: direction,
            amount: Number(amount),
            stopPrice: Number(stopPrice),
            lastSyncPrice: Number(lastSyncPrice),
            limitPercentage: Number(limitPercentage),
            stopPercentage: Number(stopPercentage)
        }
    }

    const onSubmit = (direction: TradeDirection) => {
        if (socketApi.selectedCoinTicker) {
            serverApi.submitJob(createJob(direction, socketApi.selectedCoinTicker))
        }
    }

    const onFocusAmount = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setAmount(String(value)))
    }

    const onFocusStopPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setStopPrice(String(value)))
    }

    const stopPriceValid = useMemo(() => {
        return Boolean(stopPrice) && isValidNumber(stopPrice)
            && Number(stopPrice) > 0
    }, [stopPrice, setStopPrice]);

    const stopPercentageValid = useMemo(() => {
        return Boolean(stopPercentage) && isValidNumber(stopPercentage)
            && Number(stopPercentage) > 0
    }, [stopPercentage, setStopPercentage]);

    const limitPercentageValid = useMemo(() => {
        return Boolean(limitPercentage) && isValidNumber(limitPercentage)
            && Number(limitPercentage) > 0
    }, [limitPercentage, setLimitPercentage]);

    const amountValid = useMemo(() => {
        return Boolean(amount) && isValidNumber(amount)
            && Number(amount) > 0
    }, [amount, setAmount]);

    const valid = stopPriceValid && stopPercentageValid
        && limitPercentageValid && amountValid;

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
                label={`Stop price percentage (${coin.counter})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter percentage..."}
                    onChange={setStopPercentage}
                    value={stopPercentage}/>
            </Form.Item>
            <Form.Item
                required={true}
                label={`Limit price percentage (${coin.counter})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter percentage..."}
                    onChange={setLimitPercentage}
                    value={limitPercentage}/>
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