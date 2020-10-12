import React from "react";
import classNames from 'classnames';

export interface ToolbarSpaceProps {
    prefixCls?: string;
    className?: string;
    box?: boolean;
    border?: boolean;
    leftAlign?: boolean;
}

export const ToolbarSpace: React.FC<ToolbarSpaceProps> = (
    {
        prefixCls = 'ui-toolbar-space',
        className,
        box = false,
        border = false,
        leftAlign =false,
        children
    }) => {

    const boxClasses = classNames(
        prefixCls,
        {
            [`${prefixCls}-box`]: box,
            [`${prefixCls}-border`]: border,
            [`${prefixCls}-align`]: leftAlign,
        },
        className
    );

    return (
        <div className={boxClasses}>{children}</div>
    )
};