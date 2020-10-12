import React from 'react';
import classNames from 'classnames';

export type CheckboxGroupProps = {
    prefixCls?: string;
    className?: string;
}

export const CheckboxGroup: React.FC<CheckboxGroupProps> = ({
                                                      prefixCls = 'ui-checkbox-group',
                                                      className,
                                                      children,
                                                  }) => (
    <div className={classNames(prefixCls, className)}>
        {children}
    </div>
)