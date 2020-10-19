import React, {useContext, useEffect, useState} from "react";
import classNames from "classnames";
import {ScriptEditor} from "./ScriptEditor";
import {ScriptToolbar} from "./ScriptToolbar";
import {RootState} from "../../store/reducers";
import {Script} from "../../modules/script/types";
import * as scriptActions from "../../store/scripts/actions";
import {connect, ConnectedProps} from "react-redux";
import scriptService from "../../modules/script/scriptService";
import {ServerContext} from "../../modules/server/ServerContext";
import {SocketContext} from "../../modules/socket/SocketContext";
import {ServerCoin} from "../../modules/market";
import { v4 as uuidv4 } from "uuid";

export interface ScriptManagerProps {
    prefixCls?: string;
    className?: string;
    onControl?: () => void;
}

const mapState = (state: RootState) => ({
    selectedScript: state.scripts.selectedScript
});

const mapDispatch = {
    saveOrUpdateScript: (script: Script) => scriptActions.saveOrUpdateScript(script),
    removeScript: (id: string) => scriptActions.removeScript(id)
};

const connector = connect(mapState, mapDispatch);

type StateProps = ConnectedProps<typeof connector>;

type Props = StateProps & ScriptManagerProps;

const Manager: React.FC<Props> = (
    {
        prefixCls = 'ui-script-manager',
        className,
        selectedScript,
        saveOrUpdateScript,
        removeScript,
        onControl
    }) => {

    const serverApi = useContext(ServerContext);
    const socketApi = useContext(SocketContext);

    const selectedCoin = socketApi.selectedCoin;

    const [code, setCode] = useState("");

    useEffect(() => {
        if (selectedScript) {
            setCode(selectedScript.script)
        }
    }, [selectedScript]);

    const onSave = () => {
        if (selectedScript) {

            const script = {
                ...selectedScript,
                script: code
            };

            scriptService.saveScript(script)
                .then(() => saveOrUpdateScript(script))
        }
    };

    const onRemove = () => {
        if (selectedScript) {
            scriptService.deleteScript(selectedScript.id)
                .then(() => removeScript(selectedScript.id))
                .then(() => setCode(""))
        }
    };

    const onPlay = (coin: ServerCoin) => {
        if (selectedScript && selectedCoin) {
            serverApi.submitScriptJob({
                id: uuidv4(),
                name: selectedScript.name,
                script: code,
                ticker: {
                    counter: coin.counter,
                    exchange: coin.exchange,
                    base: coin.base
                }
            })
        }
    };

    return (
        <div className={classNames(prefixCls, className)}>

            <ScriptToolbar onControl={onControl}
                           name={selectedScript && selectedScript.name}
                           disabled={!selectedScript}
                           onSave={onSave}
                           onRemove={onRemove}
                           onPlay={onPlay}
                           coin={selectedCoin}
            />

            <ScriptEditor code={code} onUpdate={setCode}/>
        </div>
    )
};

export const ScriptManager = connector(Manager);