import React from 'react';
import classNames from "classnames";

export interface SectionIconProps {
    prefixCls?: string;
    className?: string;
}

export const SectionIcon: React.FC<SectionIconProps> = (
    {
        prefixCls = 'ui-section',
        className,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-icon`]: true,
        },
        className
    );

    return (
        <div className={classes}>
            {children}
        </div>
    );
};