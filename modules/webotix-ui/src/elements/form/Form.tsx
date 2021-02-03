import {FormProps} from "./types";
import React from "react";
import classNames from 'classnames';
import {FormItem} from "./FormItem";
import {FormGroup} from "./FormGroup";

export class Form extends React.Component<FormProps> {

    public static Item: typeof FormItem;
    public static Group: typeof FormGroup;

    public static defaultProps = {
        prefixCls: 'ui-form'
    };

    public render() {

        const {
            children,
            prefixCls,
            className,
            ...props
        } = this.props;

        const classes = classNames(prefixCls, className);

        return (
            <form {...props} className={classes}>
                {children}
            </form>
        )
    }
}
