import {Layout} from "react-grid-layout"
import Immutable from "seamless-immutable"
import {getFromLS, saveToLS} from "./modules/common/localStorage";

const VERSION = 1;

interface BasePanel {
    title: string;
    icon: string;
    key: string;
    visible: boolean;
    detached: boolean;
    position: number;
}

export interface OfAllPanels<T> {
    jobs: T;
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

interface Breakpoints<T> {
    lg: T;
    md: T;
    sm: T;
}

export type Panel = BasePanel & DragPanel;

interface Meta {
    version: number;
}


const basePanels: AllKeyedPanels = Immutable({
    jobs: {
        key: "jobs",
        title: "Jobs",
        icon: "tasks",
        visible: true,
        detached: false,
        position: 0,
        x: 0,
        y: 0,
        h: 0,
        w: 0
    }
});

const baseLayouts: AllKeyedLayouts = Immutable({
    lg: {
        jobs: {i: "jobs", x: 26, y: 300, w: 14, h: 9}
    },
    md: {
        jobs: {i: "coins", x: 20, y: 100, w: 12, h: 11},
    },
    sm: {
        jobs: {i: "jobs", x: 0, y: 700, w: 4, h: 6},
    }
});

export const breakpoints: Breakpoints<number> = {
    lg: 1630,
    md: 992,
    sm: 0
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

