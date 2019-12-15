export const Alignment = {
    center: "center" as "center",
    left: "left" as "left",
    right: "right" as "right"
};

export type Alignment = typeof Alignment[keyof typeof Alignment];
