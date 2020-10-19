import React from "react";
import classNames from 'classnames';
import {Icon} from "../../elements/icon";

export interface ScriptActionProps {
    prefixCls?: string;
    className?: string;
    onClick?: () => void;
    icon: string;
    disabled?: boolean;
}

export const ScriptAction: React.FC<ScriptActionProps> = (
    {
        prefixCls = 'ui-script-toolbar-action',
        className,
        onClick,
        icon,
        disabled
    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-disabled`]: disabled,
        },
        className
    );

    return (
        <a className={classes} onClick={onClick}>
            <Icon type={icon}/>
        </a>
    )
};