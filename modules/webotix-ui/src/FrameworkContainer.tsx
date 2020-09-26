import React, {useState, useEffect, useMemo} from "react"
import {breakpoints} from "./confg";
import {Framework} from "./Framework";

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

    const api: FrameworkApi = useMemo(
        () => ({
            paperTrading,
            enablePaperTrading: () => setPaperTrading(true)
        }),
        [paperTrading]
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
            />
        </FrameworkContext.Provider>
    )
};