import React from "react";
import classNames from 'classnames';
import {Icon} from "../../elements/icon";

export interface ToolbarLinkProps {
    prefixCls?: string;
    className?: string;
    onClick: () => void;
    icon: string;
}

export const ToolbarLink: React.FC<ToolbarLinkProps> = (
    {
        prefixCls = 'ui-toolbar-link',
        className,
        onClick,
        icon
    }) => {

    return (
        <a className={classNames(prefixCls, className)}
           onClick={onClick}>
            <Icon type={icon}/>
        </a>
    )
};