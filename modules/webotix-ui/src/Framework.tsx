import React, {ReactElement} from "react"
import {DragPanel, OfAllPanels, Panel} from "./confg";
import {WidthProvider, Responsive, Layouts, Layout} from "react-grid-layout";
import {Rnd, DraggableData} from "react-rnd";

interface FrameworkProps {

    isMobile: boolean;
    width: number;
    panels: Panel[];
    hiddenPanels: Panel[];
    layouts: Layouts;
    showSettings: boolean;

    onToggleViewSettings(): void;

    onTogglePanelAttached(key: string): void;

    onTogglePanelVisible(key: string): void;

    onResetLayout(): void;

    onLayoutChange(layout: Layout[], layouts: Layouts): void;

    onMovePanel(key: string, d: DraggableData): void;

    onResizePanel(Key: string, d: DragPanel): void;

    onBreakpointChange(breakpoint: string): void;
}

interface Renders extends OfAllPanels<() => ReactElement> {
}

export class Framework extends React.Component<FrameworkProps> {

    panelsRenders: Renders;

    constructor(props: FrameworkProps) {
        super(props);

        this.panelsRenders = {
            jobs: () => (<div>123</div>)
        }
    }

    public render() {

        const {
            isMobile,
            width,
            panels,
            hiddenPanels,
            layouts,
            showSettings,
            onLayoutChange,
            onMovePanel,
            onResetLayout,
            onResizePanel,
            onTogglePanelAttached,
            onTogglePanelVisible,
            onToggleViewSettings
        } = this.props;

        return (<div>13413</div>)
    }

}

