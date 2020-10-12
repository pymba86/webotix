import React from "react";

export interface ModalProps {
    prefixCls?: string;
    visible: boolean;
    closable?: boolean;
    className?: string;
    header?: React.ReactNode;
    footer?: React.ReactNode;
    onClose?: () => void;
}

export interface ModalState {
    visible: boolean;
    bodyVisible: boolean;
}