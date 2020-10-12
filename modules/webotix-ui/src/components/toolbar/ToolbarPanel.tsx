import React from "react";
import classNames from 'classnames';
import {Panel} from "../../config";
import {Icon} from "../../elements/icon";

export interface ToolbarPanelProps {
    prefixCls?: string;
    className?: string;
    panel: Panel;
    onClick: () => void;
    margin: boolean;
}

export const ToolbarPanel: React.FC<ToolbarPanelProps> = (
    {
        prefixCls = 'ui-toolbar-panel',
        className,
        margin = false,
        onClick,
        panel
    }) => {

    const boxClasses = classNames(
        prefixCls,
        {
            [`${prefixCls}-margin`]: margin
        },
        className
    );

    return (
        <a className={boxClasses}
           title={"Show " + panel.title}
           onClick={onClick}>
            <Icon type={panel.icon}/>
        </a>
    )
};