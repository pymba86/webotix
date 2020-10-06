import * as React from 'react';
import {createPortal, findDOMNode} from 'react-dom';
import classNames from 'classnames';
import {CSSTransition} from 'react-transition-group';
import {Input, InputSize} from "../input";

export interface SelectProps {
    className?: string;
    prefixCls?: string;
    size?: InputSize;
    name?: string;
    placeholder?: string;
    prefix?: string;
    disabled?: boolean;
    prefixClassName?: string;
}

export interface SelectState {
    visible: boolean;
    position: {
        top: number;
        left: number;
    };
    inputValue: string;
    width: number;
}

export class Select extends React.PureComponent<SelectProps, SelectState> {

    public static defaultProps = {
        prefixCls: 'ui-select',
    };
    public readonly selectElement: React.RefObject<HTMLDivElement>;
    public readonly panelElement: React.RefObject<HTMLDivElement>;


    constructor(props: SelectProps) {
        super(props);
        this.selectElement = React.createRef();
        this.panelElement = React.createRef();
        this.state = {
            visible: false,
            inputValue: '',
            position: {
                top: 0,
                left: 0,
            },
            width: 0,
        };
    }

    public render() {
        const {
            prefixCls,
            children,
            disabled,
            prefix,
            name,
            size,
            prefixClassName,
            placeholder = 'Select',
            ...attributes
        } = this.props;
        const {inputValue, width, position, visible} = this.state;
        const iconClassName = classNames(`${prefixCls}-icon`, {
            [`${prefixCls}-icon-visible`]: visible,
        });

        const panelStyle = {
            ...position,
            width
        };

        const selectNode = (
            <CSSTransition
                in={visible}
                unmountOnExit
                timeout={300}
                classNames={`${prefixCls}-panel`}
                onEntered={this.bindDocumentClick}
                onExited={this.clearDocumentClick}
                onEnter={this.handleEnter}
            >
                <div className={`${prefixCls}-panel`} style={panelStyle} ref={this.panelElement}>
                    {children}
                </div>
            </CSSTransition>
        );

        return (
            <React.Fragment>
                <Input
                    placeholder={ placeholder}
                    value={inputValue}
                    disabled={disabled}
                    size={size}
                    name={name}
                    onChange={this.handleInputChange}
                    onFocus={this.handleInputFocus}
                    prefix={prefix}
                    prefixClassName={prefixClassName}
                    suffix="chevron-down"
                    wrapperProps={{
                        ...attributes,
                        ref: this.selectElement,
                    }}
                    suffixClassName={iconClassName}
                />
                {!disabled && createPortal(selectNode, document.body)}
            </React.Fragment>
        );
    }

    public bindDocumentClick = () => {
        document.addEventListener('click', this.handleDocumentClick);
    };

    public clearDocumentClick = () => {
        document.removeEventListener('click', this.handleDocumentClick);
    };

    public handleExited = () => {
        this.clearDocumentClick();
    };

    public handleDocumentClick = (event: any) => {
        const el = findDOMNode(this.selectElement.current) as Element;
        const contentEl = this.panelElement.current;
        const targetEl = event.target;
        if (
            !(
                targetEl === el ||
                (el && el.contains(targetEl)) ||
                targetEl === contentEl ||
                (contentEl && contentEl.contains(targetEl))
            )
        ) {
            this.setState({
                visible: false,
                inputValue: '',
            });
        }
    };

    public getPosition = () => {
        const el = findDOMNode(this.selectElement.current) as Element;
        const rect = el.getBoundingClientRect();
        const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
        const scrollLeft = document.documentElement.scrollLeft || document.body.scrollLeft;
        const left = scrollLeft + rect.left;
        const top = scrollTop + rect.top + rect.height;

        return {
            left,
            top,
        };
    };

    public handleEnter = () => {
        const position = this.getPosition();
        const el = findDOMNode(this.selectElement.current) as Element;
        const width = el.clientWidth || 0;
        this.setState({
            position,
            width
        });
    };

    public handleInputFocus = () => {
        this.setState({
            visible: true,
            inputValue: '',
        });
    };

    public handleInputChange = (value: string) => {
        this.setState({
            inputValue: value,
        });
    };


}
