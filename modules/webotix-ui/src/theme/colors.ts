import open from "open-color";
import color from "color";
import {alpha, lighten} from "./utils";

export const defaultScales = {
    gray: open.gray,
    blue: open.blue,
    green: open.green,
    red: open.red,
    orange: open.orange,
    yellow: open.yellow,
    teal: open.teal,
    cyan: open.cyan,
    lime: open.lime,
    pink: open.pink,
    violet: open.violet,
    indigo: open.indigo
};

////////////////////////////////////////////////////////////

export type ScalesType = typeof defaultScales;

export type ScalesColor = keyof ScalesType;

export type PaletteItem = {
    lightest: string;
    light: string;
    base: string;
    dark: string;
}

export type BackgroundPaletteMode = {
    tint1: string;
    tint2: string;
    overlay: string;
    layer: string;
    default: string;
}

export type BorderPaletteMode = {
    default: string;
    muted: string;
}

export type TextPaletteMode = {
    heading: string;
    muted: string;
    default: string;
}

export type PaletteMode = {
    background: BackgroundPaletteMode;
    border: BorderPaletteMode;
    text: TextPaletteMode;
}

export type PaletteType = Record<ScalesColor, PaletteItem>;

export type PaletteAppearances = {
    none: PaletteItem;
    primary: PaletteItem;
    success: PaletteItem;
    danger: PaletteItem;
    warning: PaletteItem;
}

export type PaletteAppearance = keyof PaletteAppearances;

////////////////////////////////////////////////////////////

function defaultGeneratePalette(scales: ScalesType): PaletteType {
    return {
        gray: {
            lightest: scales.gray[1],
            light: scales.gray[4],
            base: scales.gray[8],
            dark: scales.gray[9]
        },
        blue: {
            lightest: scales.blue[1],
            light: scales.blue[5],
            base: scales.blue[8],
            dark: scales.blue[9]
        },
        red: {
            lightest: scales.red[1],
            light: scales.red[6],
            base: scales.red[8],
            dark: scales.red[9]
        },
        orange: {
            lightest: scales.orange[1],
            light: scales.orange[4],
            base: scales.orange[8],
            dark: scales.orange[9]
        },
        yellow: {
            lightest: scales.yellow[1],
            light: scales.yellow[4],
            base: scales.yellow[8],
            dark: scales.yellow[9]
        },
        green: {
            lightest: scales.green[1],
            light: scales.green[5],
            base: scales.green[8],
            dark: scales.green[9]
        },
        teal: {
            lightest: scales.teal[1],
            light: scales.teal[4],
            base: scales.teal[8],
            dark: scales.teal[9]
        },
        violet: {
            lightest: scales.violet[1],
            light: scales.violet[4],
            base: scales.violet[8],
            dark: scales.violet[9]
        },
        cyan: {
            lightest: scales.cyan[1],
            light: scales.cyan[4],
            base: scales.cyan[8],
            dark: scales.cyan[9]
        },
        indigo: {
            lightest: scales.indigo[1],
            light: scales.indigo[4],
            base: scales.indigo[8],
            dark: scales.indigo[9]
        },
        lime: {
            lightest: scales.lime[1],
            light: scales.lime[4],
            base: scales.lime[8],
            dark: scales.lime[9]
        },
        pink: {
            lightest: scales.pink[1],
            light: scales.pink[4],
            base: scales.pink[8],
            dark: scales.pink[9]
        }

    };
}

function defaultGenerateLightMode(scales: ScalesType): PaletteMode {
    return {
        background: {
            tint1: scales.gray[1],
            tint2: scales.gray[3],
            overlay: alpha(scales.gray[9], 0.6),
            layer: "white",
            default: "white"
        },
        border: {
            default: alpha(scales.gray[9], 0.12),
            muted: alpha(scales.gray[9], 0.08)
        },
        text: {
            heading: scales.gray[9],
            muted: color(scales.gray[7])
                .lighten(0.3)
                .hex()
                .toString(),
            default: scales.gray[9],
        }
    }
}

function defaultGenerateDarkMode(scales: ScalesType): PaletteMode {
    const base = scales.gray[9];
    return {
        background: {
            tint1: lighten(base, 0.5),
            tint2: lighten(base, 0.7),
            overlay: alpha(scales.gray[7], 0.8),
            layer: lighten(base, 0.2),
            default: base
        },
        border: {
            default: alpha(scales.gray[9], 0.12),
            muted: alpha(scales.gray[9], 0.08)
        },
        text: {
            heading: scales.gray[9],
            muted: color(scales.gray[7])
                .lighten(0.3)
                .hex()
                .toString(),
            default: scales.gray[9],
        }
    }
}

function defaultGenerateIntents(palette: PaletteType): PaletteAppearances {
    return {
        none: palette.gray,
        primary: palette.blue,
        success: palette.green,
        danger: palette.red,
        warning: palette.yellow
    };
}

export function  generateColorsFromScales(scales: ScalesType) {

    const {
        generateIntents,
        generatePalette,
        generateLightMode,
        generateDarkMode
    } = {
        generateIntents: defaultGenerateIntents,
        generatePalette: defaultGeneratePalette,
        generateLightMode: defaultGenerateLightMode,
        generateDarkMode: defaultGenerateDarkMode
    };

    const palette = generatePalette(scales);
    const intent = generateIntents(palette);

    const modes = {
        light: {
            mode: "light",
            ...generateLightMode(scales),
            palette,
            scales,
            intent
        },
        dark: {
            mode: "dark",
            ...generateDarkMode(scales),
            palette,
            scales,
            intent
        }
    };

    return {
        colors: modes.light,
        modes
    };
}

export const defaultColors = generateColorsFromScales(defaultScales);
