import React from "react";

export interface ModalProps {
    prefixCls?: string;
    visible: boolean;
    closable?: boolean;
    className?: string;
    header?: React.ReactNode;
    footer?: React.ReactNode;
}

export interface ModalState {
    visible: boolean;
    bodyVisible: boolean;
}