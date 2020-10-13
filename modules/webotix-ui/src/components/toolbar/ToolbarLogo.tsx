import React from "react";
import classNames from 'classnames';
import {Icon} from "../../elements/icon";

export interface ToolbarLogoProps {
    prefixCls?: string;
    className?: string;
    onClick?: () => void;
    icon: string;
}

export const ToolbarLogo: React.FC<ToolbarLogoProps> = (
    {
        prefixCls = 'ui-toolbar-logo',
        className,
        onClick,
        icon
    }) => {

    return (
        <div className={classNames(`${prefixCls}-container`, className)}>
            <a className={classNames(`${prefixCls}-icon`)}
               onClick={onClick}>
                <Icon type={icon}/>
            </a>
        </div>
    )
};