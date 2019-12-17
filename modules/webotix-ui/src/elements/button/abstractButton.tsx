import * as React from "react";
import {ActionProps, Alignment, alignmentClass, intentClass} from "../../utilities";
import classNames from "classnames";
import {safeInvoke} from "../../utilities/invoke";
import {isKeyboardClick} from "../../utilities/keys";

export interface ButtonProps extends ActionProps {

    /**
     * Если установлено значение «истина»,
     * кнопка будет отображаться в активном состоянии.
     */
    active?: boolean;

    /**
     * Выравнивание текста внутри кнопки.
     */
    alignText?: Alignment;

    /**
     * Ссылка на элемент для обработки состояния элемента
     */
    elementRef?: (ref: HTMLElement | null) => any;

    /**
     * Должна ли эта кнопка расширяться, чтобы заполнить
     * свой контейнер
     */
    fill?: boolean;

    /**
     * Должна ли кнопка использовать большой стиль
     */
    large?: boolean;

    /**
     * Если установлено значение «истина»,
     * в центре кнопке появиться спиннер
     */
    loading?: boolean;

    /**
     * Должна ли кнопка использовать минимальный стиль
     */
    minimal?: boolean;

    /**
     * Должна ли кнопка использовать маленький стиль
     */
    small?: boolean;

    /**
     * Html атрибут кнопки
     */
    type?: "submit" | "reset" | "button";
}

export interface ButtonState {
    isActive: boolean;
}

export abstract class AbstractButton<H extends React.HtmlHTMLAttributes<any>>
    extends React.PureComponent<ButtonProps & H, ButtonState> {

    public state = {
        isActive: false,
    };

    protected buttonRef: HTMLElement | null = null;

    protected refHandlers = {
        button: (ref: HTMLElement) => {
            this.buttonRef = ref;
            safeInvoke(this.props.elementRef, ref);
        },
    };

    private currentKeyDown: number | null = null;

    protected getCommonButtonProps() {
        const {alignText, fill, large, loading, minimal, small, tabIndex} = this.props;
        const disabled = this.props.disabled || loading;

        const className = classNames(
            'button',
            {
                ['active']: this.state.isActive || this.props.active,
                ['disabled']: disabled,
                ['fill']: fill,
                ['large']: large,
                ['loading']: loading,
                ['minimal']: minimal,
                ['small']: small
            },
            alignmentClass(alignText),
            intentClass(this.props.intent),
            this.props.className
        );

        return {
            className,
            disabled,
            onClick: disabled ? undefined : this.props.onClick,
            onKeyDown: this.handleKeyDown,
            onKeyUp: this.handleKeyUp,
           // ref: this.refHandlers.button,
            tabIndex: disabled ? -1 : tabIndex
        }
    }

    protected handleKeyDown = (e: React.KeyboardEvent<any>) => {
        if (isKeyboardClick(e.which)) {
            e.preventDefault();
            if (e.which !== this.currentKeyDown) {
                this.setState({isActive: true});
            }
        }
        this.currentKeyDown = e.which;
        safeInvoke(this.props.onKeyDown, e);
    };

    protected handleKeyUp = (e: React.KeyboardEvent<any>) => {
        if (isKeyboardClick(e.which)) {
            this.setState({isActive: false});
            if (this.buttonRef)
                this.buttonRef.click();
        }
        this.currentKeyDown = null;
        safeInvoke(this.props.onKeyUp, e);
    };

    protected renderChildren(): React.ReactNode {
        const { children, text } = this.props;
        return [
                <span key="text" className={'button-text'}>
                    {text}
                    {children}
                </span>
        ];
    }
}
