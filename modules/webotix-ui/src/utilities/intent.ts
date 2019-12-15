/**
 * Базовые цвета действий
 */
export const Intent = {
    none: "none" as "none",
    primary: "primary" as "primary",
    success: "success" as "success",
    warning: "warning" as "warning",
    danger: "danger" as "danger"
};

export type Intent = typeof Intent[keyof typeof Intent];
