import React, {ButtonHTMLAttributes} from 'react';
import classNames from "classnames";

export type SectionSpanProps = ButtonHTMLAttributes<HTMLSpanElement> & {
    prefixCls?: string;
    className?: string;
}

export const SectionSpan: React.FC<SectionSpanProps> = (
    {
        prefixCls = 'ui-section',
        className,
        children,
        ...props
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-span`]: true,
        },
        className
    );

    return (
        <span {...props} className={classes}>
            {children}
        </span>
    );
};