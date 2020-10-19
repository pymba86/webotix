import React from "react";
import classNames from "classnames";
import {ScriptEditor} from "./ScriptEditor";
import {ScriptToolbar} from "./ScriptToolbar";

export interface ScriptManagerProps {
    prefixCls?: string;
    className?: string;
    onControl?: () => void;
}

export const ScriptManager: React.FC<ScriptManagerProps> = (
    {
        prefixCls = 'ui-script-manager',
        className,
        onControl
    }) => {

    return (
        <div className={classNames(prefixCls, className)}>

            <ScriptToolbar onControl={onControl}
                           name={"MACD/RSI/25/2.0"}/>

            <ScriptEditor/>
        </div>
    )
}