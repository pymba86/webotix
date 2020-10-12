import React from 'react';
import {Modal} from "../../elements/modal";
import {Button} from "../../elements/button";

export interface ErrorPopupProps {
    message: string;

    onClose(): void;
}

export const ErrorPopup: React.FC<ErrorPopupProps> = ({message, onClose}) => {

    const footerMarkup = (
        <Button variant={"primary"} onClick={onClose}>
            Ok
        </Button>
    );

    return (
        <Modal visible={true}
               closable={true}
               header={"Error"}
               footer={footerMarkup}
               onClose={onClose}>
            {message}
        </Modal>
    )
};