import {ModalProps} from "./types";
import React from "react";
import {createPortal} from "react-dom";
import {ModalPanel} from "./Panel";

export const Modal: React.FC<ModalProps> = (
    {
       visible,
       children,
       ...props
    }) => {

    const modal = (
        <ModalPanel visible={visible} {...props}>
            {children}
        </ModalPanel>
    );

    return visible ? createPortal(modal, document.body) : null;
}