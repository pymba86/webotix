import * as React from "react"
import {classNames, variationName} from "../../utilities/css";
import styles from './Button.scss';

export type Size = 'slim' | 'medium' | 'large';

export type TextAlign = 'left' | 'right' | 'center';

export interface ButtonProps {
    /** The content to display inside the button */
    children?: string | string[];
    /** A destination to link to, rendered in the href attribute of a link */
    url?: string;
    /** A unique identifier for the button */
    id?: string;
    /** Provides extra visual weight and identifies the primary action in a set of buttons */
    primary?: boolean;
    /** Indicates a dangerous or potentially negative action */
    destructive?: boolean;
    /** Disables the button, disallowing merchant interaction */
    disabled?: boolean;
    /** Replaces button text with a spinner while a background action is being performed */
    loading?: boolean;
    /** Changes the size of the button, giving it more or less padding*/
    size?: Size;
    /** Changes the inner text alignment of the button */
    textAlign?: TextAlign;
    /** Gives the button a subtle alternative to the default button styling, appropriate for certain backdrops */
    outline?: boolean;
    /** Gives the button the appearance of being pressed */
    pressed?: boolean;
    /** Allows the button to grow to the width of its container */
    fullWidth?: boolean;
    /** Displays the button with a disclosure icon. Defaults to `down` when set to true */
    disclosure?: 'down' | 'up' | boolean;
    /** Allows the button to submit a form */
    submit?: boolean;
    /** Renders a button that looks like a link */
    plain?: boolean;
    /** Makes `plain` and `outline` Button colors (text, borders, icons) the same as the current text color. Also adds an underline to `plain` Buttons */
    monochrome?: boolean;
    /** Forces url to open in a new tab */
    external?: boolean;
    /** Tells the browser to download the url instead of opening it. Provides a hint for the downloaded filename if it is a string value */
    download?: string | boolean;
    /** Icon to display to the left of the button content */
    icon?: React.ReactElement;
    /** Callback when clicked */
    onClick?(): void;
    /** Callback when button becomes focused */
    onFocus?(): void;
    /** Callback when focus leaves button */
    onBlur?(): void;
    /** Callback when a keypress event is registered on the button */
    onKeyPress?(event: React.KeyboardEvent<HTMLButtonElement>): void;
    /** Callback when a keyup event is registered on the button */
    onKeyUp?(event: React.KeyboardEvent<HTMLButtonElement>): void;
    /** Callback when a keydown event is registered on the button */
    onKeyDown?(event: React.KeyboardEvent<HTMLButtonElement>): void;
    /** Callback when mouse enter */
    onMouseEnter?(): void;
    /** Callback when element is touched */
    onTouchStart?(): void;
}

////////////////////////////////////////////////////////////

export const Button: React.FunctionComponent<ButtonProps>
    = ({
           id,
           url,
           disabled,
           loading,
           children,
           onClick,
           onFocus,
           onBlur,
           onKeyDown,
           onKeyPress,
           onKeyUp,
           onMouseEnter,
           onTouchStart,
           external,
           download,
           icon,
           primary,
           outline,
           destructive,
           disclosure,
           plain,
           monochrome,
           submit,
           size = 'medium',
           textAlign,
           fullWidth,
           pressed,
       }: ButtonProps) => {

    const isDisabled = disabled || loading;

    const className = classNames(
        styles.Button,
        primary && styles.primary,
        outline && styles.outline,
        destructive && styles.destructive,
        isDisabled && styles.disabled,
        loading && styles.loading,
        plain && styles.plain,
        pressed && !disabled && !url && styles.pressed,
        monochrome && styles.monochrome,
        size && size !== 'medium' && styles[variationName('size', size)],
        textAlign && styles[variationName('textAlign', textAlign)],
        fullWidth && styles.fullWidth,
        icon && children == null && styles.iconOnly,
    );

    return (
        <button className={className}>
            {children}
        </button>
    )
};

export default Button;
