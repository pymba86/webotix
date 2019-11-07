export default interface Theme {
    fontSizes: number[],
    fonts: {
        sans: string;
        mono: string;
        heading: string;
    },
    radii: number[],
    space: number[],
    colors: {
        fore: string;
        link: string;
        black: string;
        white: string;
        disabled: string;
        disabledBg: string;
        backgrounds: string[];
    },
    panelBreakpoints: {
        lg: number,
        md: number,
        sm: number
    }
}
