import React, {useContext, useMemo, useState} from "react";
import {useVisibility} from "../components/visibility/Visibility";
import {RenderIf} from "../components/render/RenderIf";
import {Section} from "../elements/section";
import {useHistory} from "react-router-dom"
import {Icon} from "../elements/icon";
import {SectionLink} from "../elements/section/SectionLink";
import {ServerContext} from "../modules/server/ServerContext";
import {SocketContext} from "../modules/socket/SocketContext";
import {MarketContext} from "../modules/market/MarketContext";
import {Coins, FullCoinData} from "../components/coins";
import {RootState} from "../store/reducers";
import {connect, ConnectedProps} from "react-redux";
import {ReferencePriceContainer} from "./ReferencePriceContainer";
import {Coin} from "../modules/market";
import * as coinsActions from "../store/coins/actions";
import {AddCoinContainer} from "./AddCoinContainer";

const mapState = (state: RootState) => ({
    referencePrices: state.coins.referencePrices
});

const mapDispatch = {
    setReferencePrice: (coin: Coin, price?: number) =>
        coinsActions.setReferencePrice(coin, price)
};

const connector = connect(mapState, mapDispatch);

type StateProps = ConnectedProps<typeof connector>;

const CoinsWrapper: React.FC<StateProps> = ({referencePrices, setReferencePrice}) => {

    const serverApi = useContext(ServerContext);
    const socketApi = useContext(SocketContext);
    const marketApi = useContext(MarketContext);

    const [referencePriceCoin, setReferencePriceCoin] = useState<Coin | undefined>(undefined);
    const [visibleAddCoin, setVisibleAddCoin] = useState<boolean>(false);

    const visible = useVisibility();
    const history = useHistory();

    const coins = serverApi.subscriptions;
    const tickers = socketApi.tickers;
    const exchanges = marketApi.data.exchanges;

    const data: FullCoinData[] = useMemo(
        () => coins.map(coin => {
            const referencePrice = referencePrices[coin.key]
            const ticker = tickers.get(coin.key);
            return {
                ...coin,
                exchangeMeta: exchanges.find(e => e.code === coin.exchange),
                ticker,
                hasAlert: false,
                priceChange: referencePrice
                    ? Number(
                    (((ticker ? ticker.last : referencePrice) - referencePrice) * 100) / referencePrice
                ).toFixed(2) + "%"
                    : "--"
            }
        }),
        [coins, exchanges, referencePrices, tickers]
    );

    return (
        <RenderIf condition={visible}>
            <Section id={"coins"}
                     heading={"Coins"}
                     nopadding={true}
                     buttons={() => (
                         <SectionLink onClick={() => setVisibleAddCoin(true)}>
                             <Icon type="plus"/>
                         </SectionLink>
                     )}>

                <Coins data={data}
                       onRemove={coin => {
                           serverApi.removeSubscription(coin)
                               .then(() => history.push("/"));
                       }}
                       onClickReferencePrice={setReferencePriceCoin}/>

                {referencePriceCoin && (
                    <ReferencePriceContainer
                        coin={referencePriceCoin}
                        setReferencePriceCoin={setReferencePriceCoin}
                        referencePrices={referencePrices}
                        setReferencePrice={setReferencePrice}/>
                )}

                {visibleAddCoin && (
                    <AddCoinContainer visible={visibleAddCoin}
                                      onClose={() => setVisibleAddCoin(false)}/>
                )}

            </Section>
        </RenderIf>
    )
};

export const CoinsContainer = connector(CoinsWrapper);
