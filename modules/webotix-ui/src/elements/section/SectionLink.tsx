import React, {ButtonHTMLAttributes} from 'react';
import classNames from "classnames";

export type SectionLinkProps = ButtonHTMLAttributes<HTMLAnchorElement> & {
    prefixCls?: string;
    className?: string;
}

export const SectionLink: React.FC<SectionLinkProps> = (
    {
        prefixCls = 'ui-section',
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