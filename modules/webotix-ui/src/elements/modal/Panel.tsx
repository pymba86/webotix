import {ModalProps, ModalState} from "./types";
import React from "react";
import classNames from 'classnames';
import {CSSTransition} from 'react-transition-group';
import {Icon} from "../icon";

export class ModalPanel extends React.Component<ModalProps, ModalState> {

    public static defaultProps = {
        prefixCls: 'ui-modal',
        closable: true
    };

    public readonly bodyRef: React.RefObject<HTMLDivElement>;

    constructor(props: ModalProps) {
        super(props);

        this.state = {
            visible: props.visible,
            bodyVisible: props.visible
        };

        this.bodyRef = React.createRef();
    }

    public componentDidUpdate(prevProps: ModalProps, prevState: ModalState) {
        if (this.props.visible && !prevProps.visible) {
            this.openModal();
        } else if (!this.props.visible && prevProps.visible) {
            this.closeModal();
        }

        if (this.state.visible && !prevState.visible) {
            setTimeout(() => {
                this.focusBody();
            }, 0);
        }
    }

    public render() {
        if (!this.state.visible) {
            return null;
        }

        const {
            prefixCls,
            className,
            closable,
            header,
            footer,
            children,
            big,
            scrolling
        } = this.props;

        return (
            <div className={`${prefixCls}`}>
                <CSSTransition timeout={500} in={this.state.bodyVisible} classNames={`${prefixCls}-mask`}>
                    <div className={`${prefixCls}-mask`}/>
                </CSSTransition>
                <CSSTransition
                    in={this.state.bodyVisible}
                    unmountOnExit
                    timeout={300}
                    classNames={`${prefixCls}-panel`}
                    onExited={this.handleExited}>

                    <div className={classNames(`${prefixCls}-panel`, {
                        [`${prefixCls}-panel-big`]: big
                    }, className)}
                         tabIndex={-1}
                         ref={this.bodyRef}>
                        {header && (
                            <div className={`${prefixCls}-header`}>
                                {header}
                                {closable && <div className={`${prefixCls}-close`}
                                                  onClick={this.handleClose}>
                                    <Icon type={"x"}/>
                                </div>}
                            </div>
                        )}
                        <div className={classNames(`${prefixCls}-body`, {
                            [`${prefixCls}-body-scroll`]: scrolling
                        })}>{children}</div>
                        {footer && (
                            <div className={`${prefixCls}-footer`}>
                                {footer}
                            </div>
                        )}
                    </div>
                </CSSTransition>
            </div>
        );
    }

    public openModal = () => {
        this.setState(
            {
                visible: true,
            },
            () => {
                this.setState({
                    bodyVisible: true,
                });
            },
        );
    };

    public closeModal = () => {
        this.setState({
            bodyVisible: false,
        });
    };

    public handleClose = () => {
        this.closeModal();
    };

    public handleExited = () => {

        const {onClose} = this.props;

        this.setState({
            visible: false,
        });

        if (typeof onClose === 'function') {
            onClose();
        }
    };

    public focusBody = () => {
        const bodyElement = this.bodyRef.current;
        if (bodyElement) {
            bodyElement.focus();
        }
    };

}
