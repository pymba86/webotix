import Theme from "../typings/theme";
import {darken, lighten} from "polished";


const theme: Theme = {
    fontSizes: [11, 12, 13, 16, 24, 36],
    fonts: {
        sans: "Trebuchet MS, Tahoma, Arial, sans-serif",
        mono: "Menlo, monospace",
        heading: "system-ui, sans-serif"
    },
    radii: [0,1,4],
    colors: {
        fore: "#aaa",
        link: "#3BB3E4",
        black: "#000",
        white: "#fff",
        disabled: darken(0.3, "#aaa"),
        disabledBg: lighten(0.03, "#2F3241"),
        backgrounds: [
            "#131722",
            "#282b38",
            "#2F3241",
            "#343747",
            lighten(0.1, "#343747")
        ]
    },
    panelBreakpoints: {
        lg: 1630,
        md: 992,
        sm: 0
    },
    space: [0, 4, 8, 16, 32, 64, 128],
};

export default theme;
