import color from "color";

// Colors Utils
export function lighten(c: string, amount: number) {
    return color(c)
        .lighten(amount)
        .hsl()
        .string()
}

export function alpha(c: string, amount: number) {
    return color(c)
        .alpha(amount)
        .hsl()
        .string();
}

export function fade(c: string, amount: number) {
    return color(c)
        .fade(amount)
        .hsl()
        .string();
}
