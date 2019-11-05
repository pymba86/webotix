export default interface Theme {
    fontSizes: number[];
    fonts: {
        sans: string;
        mono: string;
        heading: string;
    }
    colors: {
        fore: string;
        black: string;
        white: string;
        backgrounds: string[];
    }
}
