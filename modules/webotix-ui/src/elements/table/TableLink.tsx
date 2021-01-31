import React, {ButtonHTMLAttributes} from 'react';
import classNames from "classnames";

export type TableLinkColor = 'sell' | 'buy';

export type TableLinkProps = ButtonHTMLAttributes<Omit<HTMLAnchorElement, 'color'>> & {
    prefixCls?: string;
    className?: string;
    color?: TableLinkColor;
}

export const TableLink: React.FC<TableLinkProps> = (
    {
        prefixCls = 'ui-table',
        className,
        children,
        color,
        ...props
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-link`]: true,
            [`${prefixCls}-color-${color}`]: color,
        },
        className
    );

    return (
        <a {...props} className={classes}>
            {children}
        </a>
    );
};
