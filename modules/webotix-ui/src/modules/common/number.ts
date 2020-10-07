export const isValidNumber = (val: any) =>
    !isNaN(val) && val !== "" && val !== null;

export const isValidOtp = (val: any) => !isNaN(val) && val.length === 6;

export const formatNumber = (
    x: any,
    scale: number,
    undefinedValue: string
): string => {
    if (!isValidNumber(x)) return undefinedValue;
    const negative = x < 0;
    if (scale < 0) {
        const split = negative
            ? (-x).toString().split("-")
            : x.toString().split("-");
        if (split.length > 1) {
            return negative
                ? Number(-x).toFixed(split[1])
                : Number(x).toFixed(split[1])
        } else {
            return negative ? -split[0] : split[0]
        }
    } else {
        return Number(x).toFixed(scale)
    }
};