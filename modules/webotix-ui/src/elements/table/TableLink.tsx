import React, {ButtonHTMLAttributes} from 'react';
import classNames from "classnames";

export type TableLinkProps = ButtonHTMLAttributes<HTMLAnchorElement> & {
    prefixCls?: string;
    className?: string;
}

export const TableLink: React.FC<TableLinkProps> = (
    {
        prefixCls = 'ui-table',
        className,
        children,
        ...props
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-link`]: true,
        },
        className
    );

    return (
        <a {...props} className={classes}>
            {children}
        </a>
    );
};