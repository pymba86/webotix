import React, {useContext, useEffect} from "react";
import {Modal} from "../elements/modal";
import {Select} from "../elements/select";
import {LogContext} from "../modules/log/LogContext";
import {Script} from "../modules/script";
import scriptService from "../modules/script/scriptService";
import {ScriptsActionTypes} from "../store/scripts/types";

interface ScriptControlProps {
    selectScript: (script: Script) => ScriptsActionTypes;
    visible: boolean;
    onClose: () => void;
}

export const ScriptControlContainer: React.FC<ScriptControlProps> = (
    {
        selectScript,
        visible,
        onClose
    }
) => {

    const logApi = useContext(LogContext);

    const [scripts, setScripts] = React.useState<Script[]>([]);
    const [loading, setLoading] = React.useState<boolean>(true);

    useEffect(() => {

        logApi.trace("Fetching scripts");

        scriptService.fetchScripts()
            .then((scripts: Array<Script>) => {
                setScripts(scripts);
                logApi.trace(scripts.length + " scripts fetched");
                setLoading(false);
            })
            .catch(error => logApi.errorPopup(error.message));
    }, []);

    const onChangeScript = (script: Script) => {
        selectScript(script);
        onClose();
    };

    return (
        <Modal visible={visible}
               closable={true}
               header={"Scripts"}
               onClose={onClose}>

            <Select placeholder={"Select script"}
                    loading={loading}
                    options={scripts}
                    onChange={onChangeScript}
                    getOptionKey={script => script.id}
                    getOptionLabel={script => script.name}
            />
        </Modal>
    )
};