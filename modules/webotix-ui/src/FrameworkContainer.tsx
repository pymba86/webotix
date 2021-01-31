import React, {useState, useEffect, useMemo, useContext} from "react"
import {Breakpoint, breakpoints, DragPanel, OfAllKeyPanel, Panel, useUiConfig} from "./config";
import {Framework} from "./Framework";
import {Layout, Layouts} from "react-grid-layout";
import {DraggableData} from "react-rnd";
import Immutable, {ImmutableObject} from "seamless-immutable"
import {CoinCallback, CoinNullableCallback} from "./components/coins";
import {Coin} from "./modules/market";
import {AuthApi, AuthContext} from "./modules/auth/AuthContext";

const windowToBreakpoint = (width: number): Breakpoint =>
    width < breakpoints.lg ? (width < breakpoints.md ? "sm" : "md") : "lg";

export type LastFocusedFieldPopulate = (value: number) => void;

export interface FrameworkApi {

    paperTrading: boolean;

    enablePaperTrading(): void;

    referencePriceCoin: Coin | undefined;

    setReferencePriceCoin: CoinNullableCallback;

    alertsCoin: Coin | null;

    populateLastFocusedField: LastFocusedFieldPopulate;

    setLastFocusedFieldPopulate(populate: LastFocusedFieldPopulate): void;

    setAlertsCoin: CoinCallback;
}

export const FrameworkContext =
    React.createContext<FrameworkApi>({
        alertsCoin: null,
        referencePriceCoin: undefined,
        enablePaperTrading() {
        },
        paperTrading: false,
        setAlertsCoin() {

        },
        populateLastFocusedField() {

        },
        setLastFocusedFieldPopulate(_: (value: number) => void): void {
        },
        setReferencePriceCoin() {

        }
    });

export interface FrameworkApiProps {
    frameworkApi: FrameworkApi;
}

export function withFramework<T extends FrameworkApiProps>(Component: React.ComponentType<T>) {
    return (props: T) => (
        <FrameworkContext.Consumer>
            {frameworkApi => <Component {...props} frameworkApi={frameworkApi}/>}
        </FrameworkContext.Consumer>);
}

export const FrameworkContainer: React.FC = () => {

    const [breakpoint, setBreakpoint] = useState(
        windowToBreakpoint(window.innerWidth));

    const [width, setWidth] = useState(window.innerWidth);
    const [paperTrading, setPaperTrading] = useState(false);
    const [showSettings, setShowSettings] = useState(false);
    const [alertsCoin, setAlertsCoin] = useState<Coin | null>(null);
    const [referencePriceCoin, setReferencePriceCoin] = useState<Coin | undefined>(undefined)
    const [lastFocusedFieldPopulate, setLastFocusedFieldPopulate] = useState<LastFocusedFieldPopulate[]>([]);


    const [uiConfig, uiConfigApi] = useUiConfig();

    const authApi: AuthApi = useContext(AuthContext);

    const api: FrameworkApi = useMemo(
        () => ({
            paperTrading,
            enablePaperTrading: () => setPaperTrading(true),
            setAlertsCoin,
            alertsCoin,
            setReferencePriceCoin,
            referencePriceCoin,
            populateLastFocusedField: value => {
                if (lastFocusedFieldPopulate.length === 1) {
                    const populate = lastFocusedFieldPopulate[0];
                    if (populate) {
                        populate(value);
                    }
                }
            },
            setLastFocusedFieldPopulate: (fn: LastFocusedFieldPopulate) => setLastFocusedFieldPopulate([fn])
        }),
        [alertsCoin, paperTrading, setReferencePriceCoin, referencePriceCoin, lastFocusedFieldPopulate]
    );

    const layoutsAsObject = uiConfig.layouts;

    const layouts = useMemo<ImmutableObject<Layouts>>(
        () => Immutable<Layouts>({
            lg: Object.values(layoutsAsObject.lg),
            md: Object.values(layoutsAsObject.md),
            sm: Object.values(layoutsAsObject.sm)
        }),
        [layoutsAsObject]
    );

    const panelsAsObject = uiConfig.panels;
    const panels = useMemo<Panel[]>(() => Object.values(panelsAsObject), [panelsAsObject]);

    const hiddenPanels = useMemo<Panel[]>(
        () => (panels ? panels.filter(panel => !panel.visible) : []),
        [panels]
    );

    useEffect(() => {
        window.addEventListener("resize",
            () => setWidth(window.innerWidth));
    });

    return (
        <FrameworkContext.Provider value={api}>
            <Framework
                isMobile={breakpoint === "sm"}
                width={width}
                showSettings={showSettings}
                onToggleViewSettings={() => setShowSettings(!showSettings)}
                onBreakpointChange={(breakpoint => setBreakpoint(breakpoint))}
                panels={panels}
                layoutsAsObj={layoutsAsObject[breakpoint]}
                layouts={layouts}
                hiddenPanels={hiddenPanels}
                onLayoutChange={(layout: Layout[], layouts: Layouts) => uiConfigApi.updateLayouts(layouts)}
                onMovePanel={(key: OfAllKeyPanel, d: DraggableData) => uiConfigApi.movePanel(key, d.x, d.y)}
                onResetLayout={uiConfigApi.resetPanelsAndLayouts}
                onResizePanel={(key: OfAllKeyPanel, d: DragPanel) => uiConfigApi.resizePanel(key, d.x, d.y, d.w, d.h)}
                onTogglePanelAttached={uiConfigApi.togglePanelAttached}
                onTogglePanelVisible={uiConfigApi.togglePanelVisible}
                onInteractPanel={(key: OfAllKeyPanel) => uiConfigApi.panelToFront(key)}
                onLogout={authApi.logout}
            />
        </FrameworkContext.Provider>
    )
};
