import React, {useContext, useEffect} from "react";
import {RouteComponentProps} from "react-router";
import {Button} from "../elements/button";
import {Modal} from "../elements/modal";
import {Form} from "../elements/form";
import {Select} from "../elements/select";
import {MarketContext} from "../modules/market/MarketContext";
import {Coin, Exchange, PartialServerCoin} from "../modules/market";
import {LogContext} from "../modules/log/LogContext";
import exchangeService from "../modules/market/exchangeService";
import {augmentCoin} from "../modules/market/utils";
import {ServerContext} from "../modules/server/ServerContext";

export const AddCoinContainer: React.FC<RouteComponentProps> = ({history}) => {

    const marketApi = useContext(MarketContext);
    const logApi = useContext(LogContext);
    const serverApi = useContext(ServerContext);

    const [exchange, setExchange] = React.useState<Exchange | undefined>(undefined);

    const [pair, setPair] = React.useState<Coin | undefined>(undefined);

    const [pairs, setPairs] = React.useState<Array<Coin>>([]);

    const refreshExchanges = marketApi.actions.refreshExchanges;

    useEffect(() => {
        refreshExchanges();
    }, [refreshExchanges]);

    const onChangeExchange = (exchange: Exchange) => {

        setExchange(exchange);

        setPair(undefined);
        setPairs([]);

        logApi.trace("Fetching pairs for exchange: " + exchange.name);

        exchangeService.fetchPairs(exchange.code)
            .then((pairs: Array<PartialServerCoin>) => {
                setPairs(pairs.map(p => augmentCoin(p, exchange.code)));
                logApi.trace(pairs.length + " pairs fetched");
            })
            .catch(error => logApi.errorPopup(error.message));
    };

    const onChangePair = (pair: Coin) => {
        setPair(pair);
    };

    const onSubmit = () => {
        if (pair) {
            serverApi.addSubscription(pair);
            history.push("/coin/" + pair.key);
        }
    };

    const footerMarkup = (
        <Button disabled={!pair}
                variant={"primary"}
                onClick={onSubmit}>
            Add
        </Button>
    );

    return (
        <Modal visible={true} closable={true}
               footer={footerMarkup}
               header={"Add coin"}
               onClose={() => history.push("/")}>

            <Form>
                <Form.Item label={"Exchange"} required={true}>
                    <Select placeholder={"Select exchange"}
                            value={exchange}
                            loading={marketApi.data.exchanges.length === 0}
                            options={marketApi.data.exchanges}
                            onChange={onChangeExchange}
                            getOptionKey={exchange => exchange.code}
                            getOptionLabel={exchange => exchange.name}
                    />
                </Form.Item>
                <Form.Item label={"Pair"} required={true}>
                    <Select placeholder={"Select pair"}
                            value={pair}
                            loading={exchange && pairs.length === 0}
                            options={pairs}
                            onChange={onChangePair}
                            getOptionKey={pair => pair.key}
                            getOptionLabel={pair => pair.shortName}
                    />
                </Form.Item>
            </Form>
        </Modal>
    )
};