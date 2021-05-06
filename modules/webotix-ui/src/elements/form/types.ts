import React from "react";

export type FormAlign = 'right' | 'left' | 'top';
export type FormItemStatus = 'error' | 'default' | 'success' | 'warning';

export type FormItemProps = React.HTMLAttributes<HTMLElement> & {
    prefixCls?: string;
    className?: string;
    required?: boolean;
    labelAlign?: FormAlign;
    labelClassName?: string;
    label?: string;
    message?: string;
    invalid?: boolean;
    validate?: boolean;
    status?: FormItemStatus;
};

export interface FormGroupProps {
    prefixCls?: string;
    className?: string;
}

export interface FormProps {
    prefixCls?: string;
    className?: string;
}
