import React, {useContext, useEffect} from "react";
import {RouteComponentProps} from "react-router";
import {Modal} from "../elements/modal";
import {Select} from "../elements/select";
import {LogContext} from "../modules/log/LogContext";
import {Script} from "../modules/script/types";
import scriptService from "../modules/script/scriptService";
import {RootState} from "../store/reducers";
import * as scriptActions from "store/scripts/actions"
import {connect, ConnectedProps} from "react-redux";

const mapState = (state: RootState) => ({
    selectedScript: state.scripts.selectedScript
});

const mapDispatch = {
    selectScript: (script: Script) => scriptActions.selectScript(script)
};

const connector = connect(mapState, mapDispatch);

type StateProps = ConnectedProps<typeof connector>;

type ScriptControlProps = StateProps & RouteComponentProps;

const ScriptControl: React.FC<ScriptControlProps> = ({history, selectScript}) => {

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
        history.push("/");
    };

    return (
        <Modal visible={true}
               closable={true}
               header={"Scripts"}
               onClose={() => history.push("/")}>

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

export const ScriptControlContainer = connector(ScriptControl);