import React from 'react';
import {Modal} from "../../elements/modal";
import {Button} from "../../elements/button";

export interface ErrorPopupProps {
    message: string;

    onClose(): void;
}

export const ErrorPopup: React.FC<ErrorPopupProps> = ({message, onClose}) => {

    const headerMarkup = (
        <div>Login</div>
    );

    const footerMarkup = (
        <Button variant={"primary"} onClick={onClose}>
            Ok
        </Button>
    );

    return (
        <Modal visible={true}
               closable={true}
               header={headerMarkup}
               footer={footerMarkup}>
            {message}
        </Modal>
    )
}