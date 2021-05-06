import React, {useState} from 'react';
import classNames from 'classnames';

export type CheckboxProps = {
    prefixCls?: string;
    className?: string;
    checked?: boolean;
    onChange?: (active: boolean) => void;
    type: 'checkbox' | 'radio';
    value?: string;
}

export const Checkbox: React.FC<CheckboxProps> = ({
                                                      prefixCls = 'ui-checkbox',
                                                      checked = false,
                                                      className,
                                                      type,
                                                      value,
                                                      children,
                                                      onChange,
                                                      ...props
                                                  }) => {

    const [active, setActive] = useState(checked);

    const checkboxClassName = classNames(
        prefixCls,
        {
            [`${prefixCls}-checked`]: active,
        },
        className
    );

    return (
        <label className={checkboxClassName}>
            <input {...props}
                   className={`${prefixCls}-input`}
                   checked={active}
                   type={type}
                   onChange={() => {
                       const toggle = !active;
                       setActive(toggle);
                       if (onChange) {
                           onChange(toggle);
                       }
                   }}
                   value={value}
            />
            <i className={`${prefixCls}-indicator`}/>
            {!!children && <span>{children}</span>}
        </label>
    )
};