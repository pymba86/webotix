import Theme from "../typings/theme";
import {lighten} from "polished";


const theme: Theme = {
    fontSizes: [11, 12, 13, 16, 24, 36],
    fonts: {
        sans: "Trebuchet MS, Tahoma, Arial, sans-serif",
        mono: "Menlo, monospace",
        heading: "system-ui, sans-serif"
    },
    colors: {
        fore: "#aaa",
        black: "#000",
        white: "#fff",
        backgrounds: [
            "#131722",
            "#282b38",
            "#2F3241",
            "#343747",
            lighten(0.1, "#343747")
        ]
    }
};

export default theme;
