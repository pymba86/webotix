import React from "react";
import {Panel} from "../config";
import {Toolbar} from "../components/toolbar";

interface ToolbarContainerProps {
    onLogout(): void;
    onTogglePanelVisible(key: string): void;
    onShowViewSettings(): void;
    width: number;
    mobile: boolean;
    hiddenPanels: Panel[];
}

export const ToolbarContainer: React.FC<ToolbarContainerProps> = props => {

    return (
        <Toolbar {...props}/>
    )
}
