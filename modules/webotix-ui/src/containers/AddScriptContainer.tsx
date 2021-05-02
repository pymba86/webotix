import React, {useState} from "react";
import {Button} from "../elements/button";
import {Modal} from "../elements/modal";
import {Form} from "../elements/form";
import {Input} from "../elements/input";
import scriptService from "../modules/script/scriptService";
import {Script} from "../modules/script";
import {v4 as uuidv4} from "uuid"
import {ScriptsActionTypes} from "../store/scripts/types";

interface AddScriptProps {
    addScript: (script: Script) => ScriptsActionTypes;
    visible: boolean;
    onClose: () => void;
}

export const AddScriptContainer: React.FC<AddScriptProps> = (
    {
        addScript,
        visible,
        onClose
    }) => {

    const [name, setName] = useState<string>();

    const onSubmit = () => {
        if (name) {
            const script: Script = {
                id: uuidv4(),
                name: name,
                script: ""
            };
            scriptService.saveScript(script)
                .then(onClose)
                .then(() => addScript(script));
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
        <Modal visible={visible} closable={true}
               footer={footerMarkup}
               header={"Add script"}
               onClose={onClose}>
            <Form>
                <Form.Item label={"Name"} required={true}>
                    <Input placeholder={"Enter script name"}
                           onChange={setName}/>
                </Form.Item>
            </Form>
        </Modal>
    )
};