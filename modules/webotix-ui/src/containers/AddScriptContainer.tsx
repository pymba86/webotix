import React, {useState} from "react";
import {RouteComponentProps} from "react-router";
import {Button} from "../elements/button";
import {Modal} from "../elements/modal";
import {Form} from "../elements/form";
import {Input} from "../elements/input";
import scriptService from "../modules/script/scriptService";
import {RootState} from "../store/reducers";
import * as scriptActions from "store/scripts/actions"
import {connect, ConnectedProps} from "react-redux";
import {Script} from "../modules/script/types";
import { v4 as uuidv4 } from "uuid"

const mapState = (state: RootState) => ({
    selectedScript: state.scripts.selectedScript
});

const mapDispatch = {
    addScript: (script: Script) => scriptActions.addScript(script)
};

const connector = connect(mapState, mapDispatch);

type StateProps = ConnectedProps<typeof connector>;

type AddScriptProps = StateProps & RouteComponentProps;

const AddScript: React.FC<AddScriptProps> = ({history, addScript}) => {

    const [name, setName] = useState<string>();

    const onSubmit = () => {
        if (name) {
            const script: Script = {
                id: uuidv4(),
                name: name,
                script: ""
            };
            scriptService.saveScript(script)
                .then(() => {
                    addScript(script);
                    history.push("/");
                });
        }
    };

    const footerMarkup = (
        <Button variant={"primary"}
                disabled={!name || name.length === 0}
                onClick={onSubmit}>
            Add
        </Button>
    );

    return (
        <Modal visible={true} closable={true}
               footer={footerMarkup}
               header={"Add script"}
               onClose={() => history.push("/")}>

            <Form>
                <Form.Item label={"Name"} required={true}>
                    <Input placeholder={"Enter script name"}
                           onChange={setName}/>
                </Form.Item>
            </Form>
        </Modal>
    )
};

export const AddScriptContainer = connector(AddScript);