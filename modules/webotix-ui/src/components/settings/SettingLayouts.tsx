import React from "react";
import {Modal} from "../../elements/modal";
import {Button} from "../../elements/button";
import {OfAllKeyPanel, Panel} from "../../config";
import {Checkbox, CheckboxGroup} from "../../elements/checkbox";

export interface SettingLayoutsProps {
    panels: Panel[];

    onReset(): void;

    onTogglePanelVisible(key: OfAllKeyPanel): void;

    onClose(): void;
}

export const SettingLayouts: React.FC<SettingLayoutsProps> = (
    {
        onClose,
        onReset,
        onTogglePanelVisible,
        panels
    }) => {

    const footerMarkup = (
        <React.Fragment>
            <Button variant={"danger"} onClick={onReset}>
                Reset
            </Button>
        </React.Fragment>
    );

    return (
        <Modal visible={true}
               closable={true}
               footer={footerMarkup}
               header={"Panels"}
               onClose={onClose}>

            <CheckboxGroup>
                {panels.map(panel => (
                    <Checkbox key={panel.key}
                              type={"checkbox"}
                              onChange={() => onTogglePanelVisible(panel.key)}
                              checked={panel.visible}>
                        {panel.title}
                    </Checkbox>
                ))}
            </CheckboxGroup>

        </Modal>
    )
};