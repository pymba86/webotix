import * as React from 'react';
import classNames from 'classnames';

export type InputSize = 'small' | 'default' | 'large';

export type InputProps<> = {
    className?: string;
    prefixCls?: string;
    size?: InputSize;
    name?: string;
    disabled?: boolean;
    value?: string;
    defaultValue?: string;
    placeholder?: string;
    type?: 'text' | 'password' | 'number';
    digits?: number;
    prefix?: string;
    prefixClassName?: string;
    suffix?: string;
    suffixClassName?: string;
    wrapperProps?: any;
    wrapperRef?: React.RefObject<HTMLDivElement>;
    onChange?: (value: string) => void;
    onBlur?: (event: React.ChangeEvent<HTMLInputElement>) => void;
    onFocus?: (event: React.ChangeEvent<HTMLInputElement>) => void;
};

function fixControlledValue<T>(value: T) {
    if (typeof value === 'undefined' || value === null) {
        return '';
    }
    return value;
}

export class Input extends React.Component<InputProps> {

    public static defaultProps = {
        prefixCls: 'ui-input',
        size: 'default',
        type: 'text',
    };

    public static getDerivedStateFromProps(nextProps: InputProps) {

        if ('value' in nextProps) {
            return {
                value: nextProps.value,
            };
        }
        return null;
    }

    constructor(props: InputProps) {
        super(props);
        this.state = {
            value: props.value,
        };
    }

    public render() {
        const {
            children,
            className,
            disabled,
            size,
            type,
            prefixCls,
            prefix,
            suffix,
            prefixClassName,
            suffixClassName,
            wrapperProps = {},
            wrapperRef,
            ...attributes
        } = this.props;
        const value = this.props.value;

        const inputClassName = classNames(
            prefixCls,
            {
                [`${prefixCls}-${size}`]: size,
                [`${prefixCls}-prefix`]: prefix,
                [`${prefixCls}-suffix`]: suffix
            },
            className,
        );
        const inputType = type === 'password' ? 'password' : 'text';
        if ('value' in this.props) {
            attributes.value = fixControlledValue(value);
            delete attributes.defaultValue;
        }
        const inputNode = (
            <input
                {...attributes}
                className={inputClassName}
                readOnly={disabled}
                type={inputType}
                onChange={this.handleChange}
                onBlur={this.handleBlur}
                onFocus={this.handleFocus}
            />
        );
        const { className: wrapperClassName, ...otherWrapperProps } = wrapperProps;

        return prefix || suffix ? (
            <div className={classNames(`${prefixCls}-wrapper`, wrapperClassName)} {...otherWrapperProps}>
                {inputNode}
            </div>
        ) : (
            inputNode
        );
    }

    public handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { onChange } = this.props;
        const { value } = event.target;

        if (this.invalidNumber(value)) {
            return;
        }

        if (onChange) {
            onChange(value);
        }
    };

    public handleBlur = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { value } = event.target;
        const { onBlur } = this.props;
        if (onBlur) {
            onBlur(event);
        }
        if (this.invalidNumber(value)) {
            return;
        }
    };

    public handleFocus = (event: React.ChangeEvent<HTMLInputElement>) => {
        const { onFocus } = this.props;
        if (onFocus) {
            onFocus(event);
        }
    };

    public invalidNumber = (value: string) => {
        const { digits, type } = this.props;
        if (type !== 'number') {
            return false;
        }

        let reg = '^-?\\d*';
        if (digits === undefined) {
            reg += '\\.?\\d*';
        } else if (digits > 0) {
            reg += `\\.?\\d{0,${digits}}`;
        }
        reg += '$';
        return !new RegExp(reg).test(value);
    };
}