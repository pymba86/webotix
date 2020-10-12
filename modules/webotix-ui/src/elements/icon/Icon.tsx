import React from 'react';
import classNames from "classnames";

export interface IconProps {
    prefixCls?: string;
    type: string;
    className?: string;
    disabled?: boolean;
    onClick?: (e: React.MouseEvent) => void;
}

export const Icon: React.FC<IconProps> = (
    {
        prefixCls = 'ui-icon',
        className,
        type,
        disabled = false,
        children,
        ...props
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-${type}`]: true,
            [`${prefixCls}-disabled`]: disabled,
        },
        className
    );

    return (
        <i {...props} className={classes}/>
    );
};