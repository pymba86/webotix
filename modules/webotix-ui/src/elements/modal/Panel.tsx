import {ModalProps, ModalState} from "./types";
import React from "react";
import classNames from 'classnames';
import {CSSTransition} from 'react-transition-group';

const ESC_KEY: string = 'Escape';

export class ModalPanel extends React.Component<ModalProps, ModalState> {

    public static defaultProps = {
        prefixCls: 'ui-modal',
        closable: true
    }

    public readonly bodyRef: React.RefObject<HTMLDivElement>;

    constructor(props: ModalProps) {
        super(props);

        this.state = {
            visible: props.visible,
            bodyVisible: props.visible
        }

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
        } = this.props;

        return (
            <div className={`${prefixCls}`}>
                <CSSTransition timeout={300} in={this.state.bodyVisible} classNames={`${prefixCls}-mask`}>
                    <div className={`${prefixCls}-mask`}/>
                </CSSTransition>
                <CSSTransition
                    in={this.state.bodyVisible}
                    unmountOnExit
                    timeout={300}
                    classNames={`${prefixCls}-panel`}
                    onExited={this.handleExited}>

                    <div className={classNames(`${prefixCls}-panel`, className)}
                         onKeyDown={closable ? this.handleKeydown : undefined}
                         tabIndex={-1}
                         ref={this.bodyRef}>
                        {header && (
                            <div className={`${prefixCls}-header`}>
                                {header}
                                {closable && <div className={`${prefixCls}-close`}
                                                  onClick={this.handleClose}>x
                                </div>}
                            </div>
                        )}
                        <div className={`${prefixCls}-body`}>{children}</div>
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
        this.setState({
            visible: false,
        });
    };

    public focusBody = () => {
        const bodyElement = this.bodyRef.current;
        if (bodyElement) {
            bodyElement.focus();
        }
    };

    public handleKeydown = (event: React.KeyboardEvent) => {
        if (event.key === ESC_KEY) {
            event.stopPropagation();
            this.closeModal();
        }
    };
}
