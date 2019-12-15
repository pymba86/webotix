import * as React from "react";
import {ActionProps, Alignment, classNames} from "../../../../utilities";
import styles from "../../Button.scss";

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

    protected getCommonButtonProps() {
        const {alignText, fill, large, loading, minimal, small, tabIndex} = this.props;
        const disabled = this.props.disabled || loading;

        const className = classNames(
            styles.button,
            this.state.isActive || this.props.active && styles,
            styles.fill,
            outline && styles.outline,
            destructive && styles.destructive,
            isDisabled && styles.disabled,
            loading && styles.loading,
            plain && styles.plain,
            pressed && !disabled && !url && styles.pressed,
            monochrome && styles.monochrome,
            fullWidth && styles.fullWidth,
            icon && children == null && styles.iconOnly,
            this.props.className
        );
    }
}
