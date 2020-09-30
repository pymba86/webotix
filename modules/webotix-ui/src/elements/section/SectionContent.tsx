import React from 'react';
import classNames from "classnames";

export type SectionContentScroll = 'horizontal' | 'vertical' | 'both';

export interface SectionContentProps {
    prefixCls?: string;
    className?: string;
    nopadding?: boolean;
    scroll?: SectionContentScroll;
}

export const SectionContent: React.FC<SectionContentProps> = (
    {
        prefixCls = 'ui-section',
        className,
        nopadding,
        scroll,
        children
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-content`]: true,
            [`${prefixCls}-content-padding`]: !nopadding,
            [`${prefixCls}-content-scroll-x`]: scroll == 'horizontal' || scroll == 'both',
            [`${prefixCls}-content-scroll-y`]: scroll == 'vertical' || scroll == 'both',
        },
        className
    );

    return (
        <section className={classes}>
            {children}
        </section>
    );
};