import React, {useContext} from "react";
import {OfAllKeyPanel, Panel} from "../config";
import {Toolbar} from "../components/toolbar";
import {FrameworkContext} from "../FrameworkContainer";
import {SocketContext} from "../modules/socket/SocketContext";
import {Coin} from "../modules/market";
import {MarketContext} from "../modules/market/MarketContext";

interface ToolbarContainerProps {
    onLogout(): void;

    onTogglePanelVisible(key: OfAllKeyPanel): void;

    onShowViewSettings(): void;

    width: number;
    mobile: boolean;
    hiddenPanels: Panel[];
    coin?: Coin;
}

export const ToolbarContainer: React.FC<ToolbarContainerProps> = (
    {
        mobile,
        coin,
        hiddenPanels,
        width,
        onTogglePanelVisible,
        onLogout,
        onShowViewSettings
    }) => {

    const frameworkApi = useContext(FrameworkContext);
    const socketApi = useContext(SocketContext);
    const marketApi = useContext(MarketContext);

    return (
        <Toolbar updateFocusedField={frameworkApi.populateLastFocusedField}
                 ticker={socketApi.selectedCoinTicker}
                 mobile={mobile}
                 width={width}
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
