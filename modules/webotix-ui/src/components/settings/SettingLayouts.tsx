import React from "react";
import {Modal} from "../../elements/modal";
import {Button} from "../../elements/button";
import {Form} from "../../elements/form";
import {Input} from "../../elements/input";
import {OfAllKeyPanel, Panel} from "../../config";
import {Checkbox, CheckboxGroup} from "../../elements/checkbox";

export interface SettingLayoutsProps {
    panels: Panel[];

    onReset(): void;

    onTogglePanelVisible(key: OfAllKeyPanel): void;

    onClose(): void;
}

export const SettingLayouts: React.FC<SettingLayoutsProps> = ({onClose}) => {

    const footerMarkup = (
        <React.Fragment>
            <Button variant={"primary"} onClick={onClose}>
                Ok
            </Button>
        </React.Fragment>
    );

    return (
        <Modal visible={true}
               closable={false}
               footer={footerMarkup}
               header={"Settings"}>

           <CheckboxGroup>
               <Checkbox type={"checkbox"}>213</Checkbox>
               <Checkbox type={"checkbox"}>213</Checkbox>
           </CheckboxGroup>

        </Modal>
    )
}