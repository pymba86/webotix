import React from 'react';
import classNames from "classnames";

export interface SectionHeadingBoxProps {
    prefixCls?: string;
    className?: string;
}

export const SectionHeadingBox: React.FC<SectionHeadingBoxProps> = (
    {
        prefixCls = 'ui-section',
        className,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-heading-box`]: true,
        },
        className
    );

    return (
        <div className={classes}>
            {children}
        </div>
    );
};