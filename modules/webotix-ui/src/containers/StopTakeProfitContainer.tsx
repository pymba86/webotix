import {Coin} from "../modules/market";
import React, {useContext, useMemo, useState} from "react";
import {Form} from "../elements/form";
import {Button} from "../elements/button";
import {Input} from "../elements/input";
import {FrameworkContext} from "../FrameworkContainer";
import {isValidNumber} from "../modules/common/number";
import {ServerContext} from "../modules/server/ServerContext";
import {JobType, LimitOrderJob, OcoJob, TradeDirection} from "../modules/server";
import {v4 as uuidv4} from "uuid";
import {Checkbox} from "../elements/checkbox";
import {Ticker} from "../modules/socket";
import {SocketContext} from "../modules/socket/SocketContext";

export interface StopTakeProfitContainerProps {
    coin: Coin;
}

export const StopTakeProfitContainer: React.FC<StopTakeProfitContainerProps> = (
    {
        coin
    }
) => {

    const frameworkApi = useContext(FrameworkContext);
    const serverApi = useContext(ServerContext);
    const socketApi = useContext(SocketContext);

    const [highPrice, setHighPrice] = useState<string>("");
    const [highLimitPrice, setHighLimitPrice] = useState<string>("");
    const [highTrailingStop, setHighTrailingStop] = useState<boolean>(false);
    const [highTrailingStopPercentage, setHighTrailingStopPercentage] = useState<string>("");
    const [highTrailingLimitPercentage, setHighTrailingLimitPercentage] = useState<string>("");

    const [lowPrice, setLowPrice] = useState<string>("");
    const [lowTrailingStop, setLowTrailingStop] = useState<boolean>(false);
    const [lowLimitPrice, setLowLimitPrice] = useState<string>("");
    const [lowTrailingStopPercentage, setLowTrailingStopPercentage] = useState<string>("");
    const [lowTrailingLimitPercentage, setLowTrailingLimitPercentage] = useState<string>("");

    const [amount, setAmount] = useState<string>("");

    const currentPrice = (direction: string, ticker: Ticker): number =>
        direction === TradeDirection.BUY
            ? ticker.ask
            : ticker.bid;

    const createJob = (direction: TradeDirection, ticker: Ticker): OcoJob => {

        const tickTrigger = {
            exchange: coin.exchange,
            counter: coin.counter,
            base: coin.base
        }

        const limitOrder = (limitPrice: string): LimitOrderJob => ({
            jobType: JobType.LIMIT_ORDER,
            id: uuidv4(),
            direction: direction,
            tickTrigger,
            amount: Number(amount),
            limitPrice: Number(limitPrice)
        })

        const lastSyncPrice = currentPrice(direction, ticker);

        const trailingOrder = (stopPrice: string,
                               stopPercentage: string, limitPercentage: string) => ({
            jobType: JobType.SOFT_TRAILING_STOP,
            id: uuidv4(),
            tickTrigger,
            direction: direction,
            amount: Number(amount),
            stopPrice: Number(stopPrice),
            lastSyncPrice: Number(lastSyncPrice),
            limitPercentage: Number(limitPercentage),
            stopPercentage: Number(stopPercentage)
        })

        return {
            jobType: JobType.OCO,
            id: uuidv4(),
            tickTrigger: tickTrigger,
            low: {
                thresholdAsString: lowPrice,
                job: lowTrailingStop
                    ? trailingOrder(
                        lowPrice,
                        lowTrailingStopPercentage,
                        lowTrailingLimitPercentage
                    )
                    : limitOrder(lowLimitPrice)
            },
            high: {
                thresholdAsString: highPrice,
                job: highTrailingStop
                    ? trailingOrder(
                        highPrice,
                        highTrailingStopPercentage,
                        highTrailingLimitPercentage
                    )
                    : limitOrder(highLimitPrice)
            }
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

    const onFocusHighPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setHighPrice(String(value)))
    }

    const onFocusLowPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setHighPrice(String(value)))
    }

    const onFocusHighLimitPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setHighLimitPrice(String(value)))
    }

    const onFocusLowLimitPrice = () => {
        frameworkApi.setLastFocusedFieldPopulate(
            (value) => setLowLimitPrice(String(value)))
    }

    const highTrailingStopPercentageValid = useMemo(() => {
        return Boolean(highTrailingStopPercentage) && isValidNumber(highTrailingStopPercentage)
            && Number(highTrailingStopPercentage) > 0
    }, [highTrailingStopPercentage, setHighTrailingStopPercentage]);

    const highTrailingLimitPercentageValid = useMemo(() => {
        return Boolean(highTrailingLimitPercentage) && isValidNumber(highTrailingLimitPercentage)
            && Number(highTrailingLimitPercentage) > 0
    }, [highTrailingLimitPercentage, setHighTrailingStopPercentage]);

    const lowTrailingStopPercentageValid = useMemo(() => {
        return Boolean(lowTrailingStopPercentage) && isValidNumber(lowTrailingStopPercentage)
            && Number(lowTrailingStopPercentage) > 0
    }, [lowTrailingStopPercentage, setLowTrailingStopPercentage]);

    const lowTrailingLimitPercentageValid = useMemo(() => {
        return Boolean(lowTrailingLimitPercentage) && isValidNumber(lowTrailingLimitPercentage)
            && Number(lowTrailingLimitPercentage) > 0
    }, [lowTrailingLimitPercentage, setLowTrailingLimitPercentage]);

    const highLimitPriceValid = useMemo(() => {
        return Boolean(highLimitPrice) && isValidNumber(highLimitPrice)
            && Number(highLimitPrice) > 0 && !highTrailingStop
    }, [highLimitPrice, setHighLimitPrice, highTrailingStop, setHighTrailingStop]);

    const lowLimitPriceValid = useMemo(() => {
        return Boolean(lowLimitPrice) && isValidNumber(lowLimitPrice)
            && Number(lowLimitPrice) > 0 && !lowTrailingStop
    }, [lowLimitPrice, setLowLimitPrice, lowTrailingStop, setLowTrailingStop]);


    const highPriceValid = useMemo(() => {
        return Boolean(highPrice) && isValidNumber(highPrice)
            && Number(highPrice) > 0
    }, [highPrice, setHighPrice]);

    const lowPriceValid = useMemo(() => {
        return Boolean(highPrice) && isValidNumber(highPrice)
            && Number(highPrice) > 0
    }, [highPrice, setHighPrice]);


    const amountValid = useMemo(() => {
        return Boolean(amount) && isValidNumber(amount)
            && Number(amount) > 0
    }, [amount, setAmount]);

    const valid = amountValid
        && (highPriceValid &&
            (highLimitPriceValid || (highTrailingStop && !lowTrailingStop
                && highTrailingStopPercentageValid && highTrailingLimitPercentageValid))
            && (lowPriceValid &&
                (lowLimitPriceValid || (lowTrailingStop && !highTrailingStop
                    && lowTrailingStopPercentageValid && lowTrailingLimitPercentageValid))));

    return (
        <Form>
            <Form.Item validate={false}>
                <Checkbox type={'checkbox'}
                          checked={highTrailingStop}
                          onChange={setHighTrailingStop}>
                    Enable high trailing stop
                </Checkbox>
            </Form.Item>
            <Form.Item
                required={true}
                label={`High price (${coin.counter})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter price..."}
                    onChange={setHighPrice}
                    value={highPrice}
                    onFocus={onFocusHighPrice}
                />
            </Form.Item>
            {highTrailingStop ? (
                <>
                    <Form.Item
                        required={true}
                        label={`Stop price percentage (${coin.counter})`}>
                        <Input
                            type={"number"}
                            placeholder={"Enter percentage..."}
                            onChange={(setHighTrailingStopPercentage)}
                            value={highTrailingStopPercentage}/>
                    </Form.Item>
                    <Form.Item
                        required={true}
                        label={`Limit price percentage (${coin.counter})`}>
                        <Input
                            type={"number"}
                            placeholder={"Enter percentage..."}
                            onChange={setHighTrailingLimitPercentage}
                            value={highTrailingLimitPercentage}/>
                    </Form.Item>
                </>
            ) : (
                <Form.Item
                    required={true}
                    label={`Limit price (${coin.counter})`}>
                    <Input
                        type={"number"}
                        placeholder={"Enter percentage..."}
                        onChange={setHighLimitPrice}
                        value={highLimitPrice}
                        onFocus={onFocusHighLimitPrice}/>
                </Form.Item>
            )}

            <Form.Item validate={false}>
                <Checkbox type={'checkbox'}
                          checked={lowTrailingStop}
                          onChange={setLowTrailingStop}>
                    Enable low trailing stop
                </Checkbox>
            </Form.Item>
            <Form.Item
                required={true}
                label={`Low price (${coin.counter})`}>
                <Input
                    type={"number"}
                    placeholder={"Enter price..."}
                    onChange={setLowPrice}
                    value={lowPrice}
                    onFocus={onFocusLowPrice}
                />
            </Form.Item>
            {lowTrailingStop ? (
                <>
                    <Form.Item
                        required={true}
                        label={`Stop price percentage (${coin.counter})`}>
                        <Input
                            type={"number"}
                            placeholder={"Enter percentage..."}
                            onChange={setLowTrailingStopPercentage}
                            value={lowTrailingStopPercentage}/>
                    </Form.Item>
                    <Form.Item
                        required={true}
                        label={`Limit price percentage (${coin.counter})`}>
                        <Input
                            type={"number"}
                            placeholder={"Enter percentage..."}
                            onChange={setLowTrailingLimitPercentage}
                            value={lowTrailingLimitPercentage}/>
                    </Form.Item>
                </>
            ) : (
                <Form.Item
                    required={true}
                    label={`Limit price (${coin.counter})`}>
                    <Input
                        type={"number"}
                        placeholder={"Enter percentage..."}
                        onChange={setLowLimitPrice}
                        value={lowLimitPrice}
                        onFocus={onFocusLowLimitPrice}/>
                </Form.Item>
            )}
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