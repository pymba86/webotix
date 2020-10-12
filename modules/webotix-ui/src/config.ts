import {Layout, Layouts} from "react-grid-layout"
import Immutable from "seamless-immutable"
import {getFromLS, saveToLS} from "./modules/common/localStorage";
import {useMemo, useReducer} from "react";

const VERSION = 1;

export type OfAllKeyPanel = 'jobs' | 'notifications' | 'balance'
    | 'coins' | 'openOrders' | 'marketData' | 'chart' | 'tradeSelector';

export type OfAllPanels<T> = Record<OfAllKeyPanel, T>;

interface BasePanel {
    title: string;
    icon: string;
    key: OfAllKeyPanel;
    visible: boolean;
    detached: boolean;
    stackPosition: number;
}

export interface DragPanel {
    x: number;
    y: number;
    w: number;
    h: number;
}

export type AllKeyedPanels = OfAllPanels<Panel>;

export type KeyedLayouts = OfAllPanels<Layout>;

export type AllKeyedLayouts = Breakpoints<KeyedLayouts>;

export type Breakpoint = 'lg' | 'md' | 'sm';

export type Breakpoints<T> = Record<Breakpoint, T>;

export type Panel = BasePanel & DragPanel;

interface Meta {
    version: number;
}

export interface UiConfig {
    layouts: AllKeyedLayouts;
    panels: AllKeyedPanels;
}

export interface UiConfigApi {
    panelToFront(key: OfAllKeyPanel): void

    togglePanelAttached(key: OfAllKeyPanel): void

    togglePanelVisible(key: OfAllKeyPanel): void

    movePanel(key: OfAllKeyPanel, x: number, y: number): void

    resizePanel(key: OfAllKeyPanel, x: number, y: number, w: number, h: number): void

    resetPanels(): void

    resetLayouts(): void

    resetPanelsAndLayouts(): void

    updateLayouts(payload: Layouts): void
}

const basePanels: AllKeyedPanels = Immutable({
    jobs: {
        key: "jobs",
        title: "Jobs",
        icon: "check-square",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
    notifications: {
        key: "notifications",
        title: "Notifications",
        icon: "mail",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
    coins: {
        key: "coins",
        title: "Coins",
        icon: "folder-plus",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
    openOrders: {
        title: "Orders",
        icon: "target",
        key: "openOrders",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
    marketData: {
        title: "Market",
        icon: "book",
        key: "marketData",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
    chart: {
        title: "Chart",
        icon: "activity",
        key: "chart",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
    balance: {
        title: "Balance",
        key: "balance",
        icon: "dollar-sign",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
    tradeSelector: {
        title: "Trading",
        icon: "paperclip",
        key: "tradeSelector",
        visible: true,
        detached: false,
        stackPosition: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    },
});

const baseLayouts: AllKeyedLayouts = Immutable({
    lg: {
        coins: {i: "coins", x: 0, y: 100, w: 8, h: 22},
        jobs: {i: "jobs", x: 26, y: 300, w: 14, h: 9},
        notifications: {i: "notifications", x: 0, y: 200, w: 8, h: 9},
        openOrders: {i: "openOrders", x: 26, y: 200, w: 14, h: 11},
        chart: { i: "chart", x: 8, y: 100, w: 18, h: 18 },
        marketData: { i: "marketData", x: 26, y: 100, w: 14, h: 11 },
        tradeSelector: { i: "tradeSelector", x: 8, y: 300, w: 18, h: 9 },
        balance: { i: "balance", x: 8, y: 200, w: 18, h: 4 },
    },
    md: {
        coins: {i: "jobs", x: 20, y: 300, w: 12, h: 5},
        chart: { i: "chart", x: 0, y: 100, w: 20, h: 13 },
        jobs: {i: "coins", x: 20, y: 100, w: 12, h: 11},
        notifications: {i: "notifications", x: 20, y: 400, w: 12, h: 7},
        tradeSelector: { i: "tradeSelector", x: 0, y: 400, w: 20, h: 9 },
        marketData: { i: "marketData", x: 20, y: 200, w: 12, h: 8 },
        balance: { i: "balance", x: 0, y: 300, w: 20, h: 4 },
        openOrders: {i: "openOrders", x: 0, y: 200, w: 20, h: 5}
    },
    sm: {
        coins: {i: "coins", x: 0, y: 100, w: 4, h: 12},
        chart: { i: "chart", x: 0, y: 200, w: 4, h: 12 },
        jobs: {i: "jobs", x: 0, y: 700, w: 4, h: 6},
        notifications: {i: "notifications", x: 0, y: 800, w: 4, h: 6},
        tradeSelector: { i: "tradeSelector", x: 0, y: 500, w: 4, h: 9 },
        balance: { i: "balance", x: 0, y: 400, w: 4, h: 4 },
        marketData: { i: "marketData", x: 0, y: 600, w: 4, h: 6 },
        openOrders: {i: "openOrders", x: 0, y: 300, w: 4, h: 6}
    }
});

export const breakpoints: Breakpoints<number> = {
    lg: 1630,
    md: 992,
    sm: 0
};

export const cols: Breakpoints<number> = {
    lg: 40,
    md: 32,
    sm: 4
};

const loadedPanels: AllKeyedPanels = getFromLS("panels");

const loadedLayouts: AllKeyedLayouts = getFromLS("layouts");

const meta: Meta = getFromLS("layoutMeta");

const version = meta ? (meta.version ? meta.version : 0) : 0;

if (version < VERSION) {
    const m: Meta = {version: VERSION};
    saveToLS("layoutMeta", m);
}

const initPanels: AllKeyedPanels
    = loadedPanels === null || version < VERSION ? basePanels : loadedPanels;

const initLayouts: AllKeyedLayouts
    = loadedLayouts === null || version < VERSION ? baseLayouts : loadedLayouts;

interface BaseAction {
    reduce(state: UiConfig): UiConfig;
}

type PanelTransform = (panel: Panel) => Panel;

class ResetPanelsAction implements BaseAction {

    reduce(state: UiConfig): UiConfig {
        return {
            ...state,
            panels: saveToLS("panels", basePanels)
        };
    }
}

class ResetLayoutsAction implements BaseAction {
    reduce(state: UiConfig): UiConfig {
        return {
            ...state,
            layouts: saveToLS("layouts", baseLayouts)
        }
    }
}

class ResetPanelsAndLayoutsAction implements BaseAction {
    reduce(state: UiConfig): UiConfig {
        return new ResetLayoutsAction()
            .reduce(new ResetPanelsAction()
                .reduce(state))
    }
}

class PartialPanelUpdate implements BaseAction {

    private readonly key: OfAllKeyPanel;
    private readonly panelTransform: PanelTransform;
    private readonly resetLayouts: boolean;

    constructor(key: OfAllKeyPanel, panelTransform: PanelTransform, resetLayouts: boolean = false) {
        this.key = key;
        this.panelTransform = panelTransform;
        this.resetLayouts = resetLayouts;
    }

    reduce(state: UiConfig): UiConfig {

        return {
            ...state,
            panels: saveToLS(
                "panels",
                {
                    ...state.panels,
                    [this.key]: this.panelTransform(state.panels[this.key])

                }
            ),
            layouts: this.resetLayouts
                ? saveToLS(
                    "layouts",
                    {
                        ...state.layouts,
                        lg: {
                            ...state.layouts.lg,
                            [this.key]: baseLayouts.lg[this.key]
                        },
                        md: {
                            ...state.layouts.lg,
                            [this.key]: baseLayouts.md[this.key]
                        },
                        sm: {
                            ...state.layouts.lg,
                            [this.key]: baseLayouts.sm[this.key]
                        }
                    }
                )
                : state.layouts
        }
    }
}

class UpdateLayoutsAction implements BaseAction {
    private payload: Layouts;

    constructor(payload: Layouts) {
        this.payload = payload
    }

    reduce(state: UiConfig): UiConfig {

        return {
            ...state,
            layouts: saveToLS(
                "layouts",
                {
                    ...baseLayouts,
                    lg: this.payload.lg
                        .filter((l: Layout) => l.w !== 1)
                        .reduce((acc: Record<string, Layout>, val) => {
                            acc[val.i] = val;
                            return acc;
                        }, {}),
                    md: this.payload.md
                        .filter((l: Layout) => l.w !== 1)
                        .reduce((acc: Record<string, Layout>, val) => {
                            acc[val.i] = val;
                            return acc;
                        }, {}),
                    sm: this.payload.sm
                        .filter((l: Layout) => l.w !== 1)
                        .reduce((acc: Record<string, Layout>, val) => {
                            acc[val.i] = val;
                            return acc;
                        }, {}),
                }
            )
        }

    }
}

function reducer(state: UiConfig, action: BaseAction): UiConfig {
    return action.reduce(state);
}

let nextStackPosition: number = Object.values<Panel>(initPanels)
    .reduce((acc: number, next: Panel) =>
        (next.stackPosition > acc ? next.stackPosition : acc), 0);

export function useUiConfig(): [UiConfig, UiConfigApi] {

    const [uiConfig, dispatch] = useReducer(reducer, {
        panels: initPanels,
        layouts: initLayouts
    });

    const api: UiConfigApi = useMemo(
        () => ({
            panelToFront(key: OfAllKeyPanel) {

                dispatch(new PartialPanelUpdate(
                    key, (panel) => (
                        {
                            ...panel,
                            stackPosition: nextStackPosition++
                        }
                    )
                ))
            },
            togglePanelAttached(key: OfAllKeyPanel) {

                dispatch(new PartialPanelUpdate(
                    key, (panel) => (
                        {
                            ...panel,
                            detached: !panel.detached,
                            stackPosition: nextStackPosition++
                        }
                    ),
                    true
                ))
            },
            togglePanelVisible(key: OfAllKeyPanel) {

                dispatch(new PartialPanelUpdate(
                    key, (panel) => (
                        {
                            ...panel,
                            visible: !panel.visible,
                            stackPosition: nextStackPosition++
                        }
                    ),
                    true
                ))
            },
            movePanel(key: OfAllKeyPanel, x: number, y: number) {

                dispatch(new PartialPanelUpdate(
                    key, (panel) => (
                        {
                            ...panel,
                            x,
                            y
                        }
                    )
                ));
            },
            resizePanel(key: OfAllKeyPanel, x: number, y: number, w: number, h: number) {

                dispatch(new PartialPanelUpdate(
                    key, (panel) => (
                        {
                            ...panel,
                            x,
                            y,
                            w,
                            h
                        }
                    )
                ));
            },
            resetPanels() {
                dispatch(new ResetPanelsAction());
            },
            resetLayouts() {
                dispatch(new ResetLayoutsAction())
            },
            resetPanelsAndLayouts() {
                dispatch(new ResetPanelsAndLayoutsAction())
            },
            updateLayouts(payload: Layouts) {
                dispatch(new UpdateLayoutsAction(payload))
            }

        }),
        [dispatch]
    );

    return [uiConfig, api]
}