import * as React from "react"
import {AbstractButton, ButtonProps as Props} from "./abstractButton";
import {removeNonHTMLProps} from "../../utilities";

export type ButtonProps = Props;

export class Button extends AbstractButton<React.ButtonHTMLAttributes<HTMLButtonElement>> {

    public static displayName = 'Button';

    public render() {
        return (
            <button type="button" {...removeNonHTMLProps(this.props)} {...this.getCommonButtonProps()}>
                {this.renderChildren()}
            </button>
        );
    }
}

export class AnchorButton extends AbstractButton<React.AnchorHTMLAttributes<HTMLAnchorElement>> {

    public static displayName = 'AnchorButton';

    public render() {
        const {href, tabIndex = 0} = this.props;
        const commonProps = this.getCommonButtonProps();

        return (
            <a
                role="button"
                {...removeNonHTMLProps(this.props)}
                {...commonProps}
                href={commonProps.disabled ? undefined : href}
                tabIndex={commonProps.disabled ? -1 : tabIndex}
            >
                {this.renderChildren()}
            </a>
        )
    }
}
