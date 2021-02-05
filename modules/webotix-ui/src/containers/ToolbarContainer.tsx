import React, {useContext, useMemo} from "react";
import {OfAllKeyPanel, Panel} from "../config";
import {Toolbar} from "../components/toolbar";
import {FrameworkContext} from "../FrameworkContainer";
import {SocketContext} from "../modules/socket/SocketContext";
import {MarketContext} from "../modules/market/MarketContext";
import {ServerContext} from "../modules/server/ServerContext";
import {formatNumber} from "../modules/common/number";
import {RootState} from "../store/reducers";
import {connect, ConnectedProps} from "react-redux";

const mapState = (state: RootState) => ({
    version: state.support.metadata.version
});

const connector = connect(mapState);

type StateProps = ConnectedProps<typeof connector>;

interface ToolbarContainerProps {
    onLogout(): void;

    onTogglePanelVisible(key: OfAllKeyPanel): void;

    onShowViewSettings(): void;

    width: number;
    mobile: boolean;
    hiddenPanels: Panel[];
}

type Props = StateProps & ToolbarContainerProps;

export const ToolbarWrapper: React.FC<Props> = (
    {
        mobile,
        hiddenPanels,
        width,
        onTogglePanelVisible,
        onLogout,
        onShowViewSettings,
        version
    }) => {

    const frameworkApi = useContext(FrameworkContext);
    const socketApi = useContext(SocketContext);
    const marketApi = useContext(MarketContext);
    const serverApi = useContext(ServerContext);

    const allMetadata = serverApi.coinMetadata;
    const ticker = socketApi.selectedCoinTicker;

    const coin = socketApi.selectedCoin;

    const coinMetadata = useMemo(() => {
        return coin ? allMetadata.get(coin.key) : null;
    }, [coin, allMetadata]);


    if (!socketApi.connected) {
        document.title = "Not connected";
    } else if (ticker && coin) {
        document.title =
            formatNumber(ticker.last, coinMetadata ? coinMetadata.priceScale : 8,
                "No price") + " " + coin.base + "/" + coin.counter;
    } else {
        document.title = "No coin";
    }

    return (
        <Toolbar updateFocusedField={frameworkApi.populateLastFocusedField}
                 ticker={socketApi.selectedCoinTicker}
                 connected={socketApi.connected}
                 mobile={mobile}
                 width={width}
                 version={version}
                 onShowPanel={key => onTogglePanelVisible(key)}
                 onShowViewSettings={onShowViewSettings}
                 hiddenPanels={hiddenPanels}
                 balances={socketApi.balances}
                 onLogout={onLogout}
                 exchange={marketApi.data.selectedExchange}
                 coin={coin}
        />
    )
};

export const ToolbarContainer = connector(ToolbarWrapper);
