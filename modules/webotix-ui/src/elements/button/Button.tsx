import React, {ButtonHTMLAttributes} from 'react';
import classNames from 'classnames';

export type ButtonType = 'primary' | 'success' | 'danger' | 'warning' | 'link';

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
    prefixCls?: string;
    variant?: ButtonType;
    outline?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
                                                  prefixCls = 'ui-btn',
                                                  variant = 'primary',
                                                  outline, className,
                                                  children, ...props
                                              }: ButtonProps) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-${variant}`]: !outline,
            [`${prefixCls}-outline-${variant}`]: outline,
        },
        className
    );

    return (
        <button {...props} className={classes}>
            {children}
        </button>
    )
};