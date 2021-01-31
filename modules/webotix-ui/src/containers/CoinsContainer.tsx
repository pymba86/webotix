import React, {useCallback, useContext, useMemo} from "react";
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
import {FrameworkContext} from "../FrameworkContainer";
import {RootState} from "../store/reducers";
import {connect, ConnectedProps} from "react-redux";

const mapState = (state: RootState) => ({
    referencePrices: state.coins.referencePrices
});

const connector = connect(mapState);

type StateProps = ConnectedProps<typeof connector>;

const CoinsWrapper: React.FC<StateProps> = ({referencePrices}) => {

    const serverApi = useContext(ServerContext);
    const socketApi = useContext(SocketContext);
    const marketApi = useContext(MarketContext);
    const frameworkApi = useContext(FrameworkContext);

    const visible = useVisibility();
    const history = useHistory();

    const coins = serverApi.subscriptions;
    const tickers = socketApi.tickers;
    const exchanges = marketApi.data.exchanges;

    const handleAddCoin = useCallback(
        () => history.push('/addCoin'), [history]);

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
                         <SectionLink onClick={handleAddCoin}>
                             <Icon type="plus"/>
                         </SectionLink>
                     )}>
                <Coins data={data}
                       onRemove={coin => {
                           serverApi.removeSubscription(coin)
                               .then(() => history.push("/"));
                       }}
                       onClickReferencePrice={frameworkApi.setReferencePriceCoin}/>
            </Section>
        </RenderIf>
    )
};

export const CoinsContainer = connector(CoinsWrapper);
