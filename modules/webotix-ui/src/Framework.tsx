import React, {ReactElement} from "react"
import {Breakpoint, breakpoints, cols, DragPanel, KeyedLayouts, OfAllKeyPanel, OfAllPanels, Panel} from "./config";
import {WidthProvider, Responsive, Layouts, Layout} from "react-grid-layout";
import {Rnd, DraggableData} from "react-rnd"
import {LayoutBox} from "./elements/layout";
import {Section, SectionProvider} from "./elements/section";
import {ErrorBoundary} from "./components/error";
import {ImmutableObject} from "seamless-immutable";
import {JobsContainer} from "./containers/JobsContainer";
import {Logs} from "./modules/log/Logs";
import {Route} from "react-router-dom"
import {AddCoinContainer} from "./containers/AddCoinContainer";
import {CoinsContainer} from "./containers/CoinsContainer";
import {OrdersContainer} from "./containers/OrdersContainer";
import {MarketContainer} from "./containers/MarketContainer";
import {ChartContainer} from "./containers/ChartContainer";
import {BalanceContainer} from "./containers/BalanceContainer";
import {TradeContainer} from "./containers/TradeContainer";
import {ToolbarContainer} from "./containers/ToolbarContainer";

interface FrameworkProps {

    isMobile: boolean;
    width: number;
    panels: Panel[];
    hiddenPanels: Panel[];
    layouts: ImmutableObject<Layouts>;
    layoutsAsObj: KeyedLayouts;
    showSettings: boolean;

    onToggleViewSettings(): void;

    onTogglePanelAttached(key: OfAllKeyPanel): void;

    onTogglePanelVisible(key: OfAllKeyPanel): void;

    onResetLayout(): void;

    onLayoutChange(layout: Layout[], layouts: Layouts): void;

    onMovePanel(key: OfAllKeyPanel, d: DraggableData): void;

    onResizePanel(key: OfAllKeyPanel, d: DragPanel): void;

    onInteractPanel(key: OfAllKeyPanel): void;

    onBreakpointChange(breakpoint: Breakpoint): void;

    onToggleViewSettings(): void;

    onTogglePanelVisible(key: string): void;

    onLogout(): void;
}

interface Renders extends OfAllPanels<() => ReactElement> {
}

const ResponsiveReactGridLayout = WidthProvider(Responsive);

export class Framework extends React.Component<FrameworkProps> {

    panelsRenders: Renders;

    constructor(props: FrameworkProps) {
        super(props);

        const icons = new Map(props.panels.map(panel => [panel.key, panel.icon]));

        const Panel: React.FC<{ id: OfAllKeyPanel }> = ({id, children}) => (
            <SectionProvider value={{
                draggable: true,
                icon: icons.get(id),
                compactDragHandle: this.props.isMobile,
                onHide: () => this.props.onTogglePanelVisible(id),
                onToggleAttached: this.props.isMobile ? undefined : () => this.props.onTogglePanelAttached(id)
            }}>
                <ErrorBoundary
                    wrapper={({message, children}) =>
                        <Section heading={message}>{children}</Section>}>
                    {children}
                </ErrorBoundary>
            </SectionProvider>
        );

        this.panelsRenders = {
            jobs: () => (
                <LayoutBox key={"jobs"} data-grid={props.layoutsAsObj.jobs}>
                    <Panel id={"jobs"}>
                        <JobsContainer/>
                    </Panel>
                </LayoutBox>
            ),
            notifications: () => (
                <LayoutBox key={"notifications"} data-grid={props.layoutsAsObj.notifications}>
                    <Panel id={"notifications"}>
                        <Logs/>
                    </Panel>
                </LayoutBox>
            ),
            coins: () => (
                <LayoutBox key={"coins"} data-grid={this.props.layoutsAsObj.coins}>
                    <Panel id={"coins"}>
                        <CoinsContainer/>
                    </Panel>
                </LayoutBox>
            ),
            openOrders: () => (
                <LayoutBox key="openOrders" data-grid={this.props.layoutsAsObj.openOrders}>
                    <Panel id={"openOrders"}>
                        <OrdersContainer/>
                    </Panel>
                </LayoutBox>
            ),
            marketData: () => (
                <LayoutBox key="marketData" data-grid={this.props.layoutsAsObj.marketData}>
                    <Panel id="marketData">
                        <MarketContainer/>
                    </Panel>
                </LayoutBox>
            ),
            chart: () => (
                <LayoutBox key="chart" data-grid={this.props.layoutsAsObj.chart}>
                    <Panel id="chart">
                        <ChartContainer/>
                    </Panel>
                </LayoutBox>
            ),
            balance: () => (
                <LayoutBox key="balance" data-grid={this.props.layoutsAsObj.balance}>
                    <Panel id="balance">
                        <BalanceContainer/>
                    </Panel>
                </LayoutBox>
            ),
            tradeSelector: () => (
                <LayoutBox key="tradeSelector" data-grid={this.props.layoutsAsObj.tradeSelector}>
                    <Panel id="tradeSelector">
                        <TradeContainer/>
                    </Panel>
                </LayoutBox>
            ),
        }
    }

    public render() {

        const {
            isMobile,
            panels,
            layouts,
            onLayoutChange,
            onBreakpointChange,
            onInteractPanel,
            onMovePanel,
            onResizePanel,
            onToggleViewSettings,
            hiddenPanels,
            width,
            onLogout,
            onTogglePanelVisible
        } = this.props;

        return (
            <React.Fragment>
                <ErrorBoundary>
                    <ToolbarContainer
                        mobile={isMobile}
                        onShowViewSettings={onToggleViewSettings}
                    onTogglePanelVisible={onTogglePanelVisible}
                    hiddenPanels={hiddenPanels}
                    onLogout={onLogout}
                    width={width}/>
                </ErrorBoundary>
                <ErrorBoundary>
                    <Route exact={true} path={"/addCoin"} component={AddCoinContainer}/>
                </ErrorBoundary>
                <ResponsiveReactGridLayout
                    breakpoints={breakpoints}
                    cols={cols}
                    rowHeight={24}
                    layouts={layouts.asMutable({deep: true})}
                    onLayoutChange={onLayoutChange}
                    onBreakpointChange={onBreakpointChange}
                    margin={[4, 4]}
                    containerPadding={[4, 4]}
                    draggableHandle={".dragMe"}>
                    {panels
                        .filter(p => !p.detached || isMobile)
                        .filter(p => p.visible)
                        .map(p => this.panelsRenders[p.key]())
                    }
                </ResponsiveReactGridLayout>
                {!isMobile && panels
                    .filter(p => p.detached)
                    .filter(p => p.visible)
                    .map(p => (
                        <Rnd
                            key={p.key}
                            bounds="parent"
                            style={{
                                border: "1px solid #131722",
                                boxShadow: "0 0 16px rgba(0, 0, 0, 0.4)",
                                zIndex: p.stackPosition
                            }}
                            dragHandleClassName="dragMe"
                            position={{x: p.x ? p.x : 100, y: p.y ? p.y : 100}}
                            size={{width: p.w ? p.w : 400, height: p.h ? p.h : 400}}
                            onDragStart={() => onInteractPanel(p.key)}
                            onResizeStart={() => onInteractPanel(p.key)}
                            onDragStop={(e, d) => onMovePanel(p.key, d)}
                            onResizeStop={(e, direction, ref, delta, position) => {
                                onResizePanel(p.key, {
                                    w: ref.offsetWidth,
                                    h: ref.offsetHeight,
                                    x: position.x,
                                    y: position.y
                                })
                            }}
                        >
                            {this.panelsRenders[p.key]()}
                        </Rnd>
                    ))}
            </React.Fragment>
        )
    }

}

