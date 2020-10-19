import React from "react";
import classNames from "classnames";
import {ScriptAction} from "./ScriptAction";
import {ServerCoin} from "../../modules/market";

export interface ScriptToolbarProps {
    prefixCls?: string;
    className?: string;
    onControl?: () => void;
    name?: string;
    onSave: () => void;
    onRemove: () => void;
    onPlay: (coin: ServerCoin) => void;
    disabled?: boolean;
    coin?: ServerCoin;
}

export const ScriptToolbar: React.FC<ScriptToolbarProps> = (
    {
        prefixCls = 'ui-script-toolbar',
        className,
        onControl,
        onSave,
        onRemove,
        onPlay,
        name,
        coin,
        disabled
    }) => {

    const handlePlay = () => {
        if (coin) {
            onPlay(coin);
        }
    };

    return (
        <div className={classNames(`${prefixCls}`, className)}>
            <div className={classNames(`${prefixCls}-title`)}>
                <ScriptAction onClick={onControl} icon={"menu"}/>
                <span>{name}</span>
            </div>
            <div className={classNames(`${prefixCls}-actions`)}>

                <ScriptAction onClick={onSave}
                              icon={"save"} disabled={disabled}/>

                <ScriptAction onClick={onRemove}
                              icon={"trash"} disabled={disabled}/>

                <ScriptAction onClick={handlePlay}
                              icon={"play"} disabled={coin === undefined || disabled}/>

            </div>
        </div>
    )
};