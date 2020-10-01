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
        prefixCls, `${prefixCls}-heading-box`, className
    );

    const headingClassName = classNames(
        prefixCls, `${prefixCls}-heading`, className
    );

    return (
        <div className={classes}>
            <h3 className={headingClassName}>{children}</h3>
        </div>
    );
};