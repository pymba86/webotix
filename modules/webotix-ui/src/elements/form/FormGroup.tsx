import * as React from 'react';
import classNames from 'classnames';
import {FormGroupProps} from "./types";

export const FormGroup: React.FC<FormGroupProps> = (
    {
        prefixCls = 'ui-form-group',
        children,
        className
    }) => {

    return (
        <div className={classNames(prefixCls, className)}>
            {children}
        </div>
    );
};
