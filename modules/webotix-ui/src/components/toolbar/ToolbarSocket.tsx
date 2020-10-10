import React from "react";
import classNames from 'classnames';
import {Icon} from "../../elements/icon";

export interface ToolbarSocketProps {
    prefixCls?: string;
    className?: string;
    connected: boolean;
}

export const ToolbarSocket: React.FC<ToolbarSocketProps> = (
    {
        prefixCls = 'ui-toolbar-socket',
        className,
        connected
    }) => {

    const boxClasses = classNames(
        prefixCls,
        {
            [`${prefixCls}-connected`]: connected
        },
        className
    );

    return (
        <span className={boxClasses}>
            <Icon type={"wifi"}/>
        </span>
    )
};