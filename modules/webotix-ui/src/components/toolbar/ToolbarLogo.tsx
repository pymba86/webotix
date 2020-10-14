import React from "react";
import classNames from 'classnames';
import {Icon} from "../../elements/icon";
import {Link} from "react-router-dom";

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
            <Link to={"/"} className={classNames(`${prefixCls}-icon`)}
               onClick={onClick}>
                <Icon type={icon}/>
            </Link>
        </div>
    )
};