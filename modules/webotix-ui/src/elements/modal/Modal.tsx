import {ModalProps} from "./types";
import React from "react";
import {createPortal} from "react-dom";
import {ModalPanel} from "./Panel";


export class Modal extends React.Component<ModalProps> {

    public render() {
        const node = <ModalPanel {...this.props}/>;
        return createPortal(node, document.body);
    }
}