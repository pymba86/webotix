import * as React from 'react';
import classNames from 'classnames';
import {CSSTransition} from 'react-transition-group';
import {FormItemProps} from "./types";

export const FormItem: React.FC<FormItemProps> = ({
                                                      prefixCls = 'ui-form',
                                                      labelAlign = 'top', status,
                                                      message, invalid, label, children,
                                                      className, labelClassName, required
                                                  }: FormItemProps) => {

    const itemClassName = classNames(
        {
            [`${prefixCls}-item`]: true,
            [`${prefixCls}-item-top`]: labelAlign === 'top',
            [`${prefixCls}-item-required`]: required,
        },
        className,
    );

    const labelClsName = classNames(
        {
            [`${prefixCls}-item-label`]: true,
            [`${prefixCls}-item-label-required`]: required,
            [`${prefixCls}-item-label-${labelAlign}`]: true,
        },
        labelClassName,
    );

    const messageNode = (
        <CSSTransition
            in={!!(message && invalid)}
            timeout={216}
            unmountOnExit
            classNames={`${prefixCls}-item-message`}
        >
            <div
                className={classNames(
                    `${prefixCls}-item-message`,
                    `${prefixCls}-item-message-${status}`,
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
            <div className={`${prefixCls}-item-control`}>
                {children}
                {messageNode}
            </div>
        </div>
    );
};