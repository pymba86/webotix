import React from 'react';
import classNames from "classnames";

export interface SectionBoxProps {
    prefixCls?: string;
    className?: string;
}

export const SectionBox: React.FC<SectionBoxProps> = (
    {
        prefixCls = 'ui-section',
        className,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-box`]: true,
        },
        className
    );

    return (
        <section className={classes}>
            {children}
        </section>
    );
};