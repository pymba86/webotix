import React, {ButtonHTMLAttributes} from 'react';
import classNames from "classnames";

export type  SectionTabProps = ButtonHTMLAttributes<HTMLButtonElement> & {
    prefixCls?: string;
    className?: string;
    selected?: boolean;
}

export const SectionTab: React.FC<SectionTabProps> = (
    {
        prefixCls = 'ui-section',
        selected = false,
        className,
        children,
        ...props
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-tab`]: true,
            [`${prefixCls}-tab-selected`]: selected,
        },
        className
    );

    return (
        <button {...props} className={classes}>
            {children}
        </button>
    );
};