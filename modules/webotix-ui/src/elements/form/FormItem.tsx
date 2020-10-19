import * as React from 'react';
import classNames from 'classnames';
import {CSSTransition} from 'react-transition-group';
import {FormItemProps} from "./types";

export const FormItem: React.FC<FormItemProps> = ({
                                                      prefixCls = 'ui-form-item',
                                                      labelAlign = 'top', status,
                                                      message, invalid, label, children,
                                                      className, labelClassName, required
                                                  }: FormItemProps) => {

    const itemClassName = classNames(
        prefixCls,
        {
            [`${prefixCls}-top`]: labelAlign === 'top',
            [`${prefixCls}-required`]: required,
        },
        className,
    );

    const labelClsName = classNames(
        {
            [`${prefixCls}-label`]: true,
            [`${prefixCls}-label-required`]: required,
            [`${prefixCls}-label-${labelAlign}`]: true,
        },
        labelClassName,
    );

    const messageNode = (
        <CSSTransition
            in={!!(message && invalid)}
            timeout={216}
            unmountOnExit
            classNames={`${prefixCls}-message`}
        >
            <div
                className={classNames(
                    `${prefixCls}-message`,
                    `${prefixCls}-message-${status}`,
                )}
            >
                {message}
            </div>
        </CSSTransition>
    );

    return (
        <div className={itemClassName}>
            <div className={labelClsName}>
                {label}
            </div>
            <div className={`${prefixCls}-control`}>
                {children}
                {messageNode}
            </div>
        </div>
    );
};