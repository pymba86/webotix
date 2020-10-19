import React from "react";
import classNames from "classnames";
import {ScriptAction} from "./ScriptAction";

export interface ScriptToolbarProps {
    prefixCls?: string;
    className?: string;
    name?: string;
    onControl?: () => void;
}

export const ScriptToolbar: React.FC<ScriptToolbarProps> = (
    {
        prefixCls = 'ui-script-toolbar',
        className,
        onControl,
        name
    }) => {

    return (
        <div className={classNames(`${prefixCls}`, className)}>
            <div className={classNames(`${prefixCls}-title`)}>
                <ScriptAction onClick={onControl} icon={"menu"}/>
                <span>{name}</span>
            </div>
            <div className={classNames(`${prefixCls}-actions`)}>
                <ScriptAction onClick={() => {
                }} icon={"save"} disabled={true}/>
                <ScriptAction onClick={() => {
                }} icon={"play"}/>
            </div>
        </div>
    )
}