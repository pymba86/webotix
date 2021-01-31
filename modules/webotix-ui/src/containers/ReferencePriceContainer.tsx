import React, {useState, useContext, useMemo} from "react"

import {connect, ConnectedProps} from "react-redux"
import {RootState} from "../store/reducers";
import * as coinsActions from "../store/coins/actions";
import {Coin} from "../modules/market";
import {FrameworkContext} from "../FrameworkContainer";
import {ServerContext} from "../modules/server/ServerContext";
import {formatNumber, isValidNumber} from "../modules/common/number";
import {Modal} from "../elements/modal";
import {Form} from "../elements/form";
import {Input} from "../elements/input";
import {Button} from "../elements/button";
import {AuthContext} from "../modules/auth/AuthContext";
import exchangeService from "../modules/market/exchangeService";
import {LogContext} from "../modules/log/LogContext";

const mapState = (state: RootState) => ({
    referencePrices: state.coins.referencePrices
});

const mapDispatch = {
    setReferencePrice: (coin: Coin, price?: number) =>
        coinsActions.setReferencePrice(coin, price)
};

const connector = connect(mapState, mapDispatch);

type StateProps = ConnectedProps<typeof connector>;

const ReferencePrice: React.FC<StateProps> = (
    {
        referencePrices,
        setReferencePrice
    }
) => {
    const [price, setPrice] = useState<string | undefined>("");
    const frameworkApi = useContext(FrameworkContext);
    const authApi = useContext(AuthContext);
    const logApi = useContext(LogContext);
    const serverApi = useContext(ServerContext);

    const coin = frameworkApi.referencePriceCoin;

    const errorPopup = logApi.errorPopup;

    const coinMetadata = coin ? serverApi.coinMetadata.get(coin.key) : null;

    const referencePriceUnformatted = coin ? referencePrices[coin.key] : null;

    const referencePrice = useMemo(() => {
        const priceScale = coinMetadata ? coinMetadata.priceScale : 8
        return formatNumber(referencePriceUnformatted, priceScale, "")
    }, [coinMetadata, referencePriceUnformatted]);

    if (coin) {

        const onSubmit = (price?: string) => {
            authApi.authenticatedRequest(
                () => exchangeService.setReferencePrice(coin, price))
                .then(() => {
                    setReferencePrice(coin, price ? Number(price) : undefined)
                    frameworkApi.setReferencePriceCoin(undefined)
                    setPrice("")
                })
                .catch((error: Error) => errorPopup("Could not fetch coin metadata: " + error.message));
        };

        const onFocus = () => {
            frameworkApi.setLastFocusedFieldPopulate((value) => setPrice(String(value)))
        }

        const footerMarkup = (
            <div style={{display: "flex", justifyContent: "space-between"}}>
                <Button
                    variant={"danger"}
                    onClick={() => onSubmit(undefined)}>
                    Clear
                </Button>
                <Button
                    variant={"primary"}
                    onClick={() => onSubmit(price)}>
                    Set
                </Button>
            </div>
        );

        const ready = Boolean(price) && isValidNumber(price) && Number(price) > 0;

        return (
            <Modal visible={true} closable={true}
                   footer={footerMarkup}
                   header={"Set reference price for " + coin.name}
                   onClose={() => frameworkApi.setReferencePriceCoin(undefined)}>
                <Form>
                    <Form.Item
                        invalid={ready}
                        label={"Reference price"}>
                        <Input
                            type={"number"}
                            placeholder={"Enter price..."}
                            value={price ? price : referencePrice}
                            onFocus={onFocus}
                            onChange={setPrice}/>
                    </Form.Item>
                </Form>
            </Modal>
        )
    }
    return null;
}

export const ReferencePriceContainer = connector(ReferencePrice);
