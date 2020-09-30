import React, {useState, useEffect, useMemo} from "react"
import {breakpoints, Panel, useUiConfig} from "./confg";
import {Framework} from "./Framework";
import {Layouts} from "react-grid-layout";
import Immutable from "seamless-immutable"

const windowToBreakpoint = (width: number) =>
    width < breakpoints.lg ? (width < breakpoints.md ? "sm" : "md") : "lg";

export interface FrameworkApi {

    paperTrading: boolean;

    enablePaperTrading(): void;
}

export const FrameworkContext =
    React.createContext<FrameworkApi | null>(null);

function withFramework<T>(Component: React.ComponentType<T>) {
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

    const [uiConfig] = useUiConfig();

    const api: FrameworkApi = useMemo(
        () => ({
            paperTrading,
            enablePaperTrading: () => setPaperTrading(true)
        }),
        [paperTrading]
    );

    const layoutsAsObject = uiConfig.layouts;
    const layouts = useMemo<Layouts>(
        () => {
            return {
                lg: Object.values(layoutsAsObject.lg),
                md: Object.values(layoutsAsObject.md),
                sm: Object.values(layoutsAsObject.sm)
            }
        },
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
                layouts={layouts}
                hiddenPanels={hiddenPanels}
            />
        </FrameworkContext.Provider>
    )
};