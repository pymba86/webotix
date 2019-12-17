import * as React from "react"
import {AbstractButton, ButtonProps} from "./abstractButton";
import {removeNonHTMLProps} from "../../utilities";

export {ButtonProps};

export class Button extends AbstractButton<React.ButtonHTMLAttributes<HTMLButtonElement>> {

    public static displayName = `Button`;

    public render() {
        return (
            <button type="button" {...removeNonHTMLProps(this.props)} {...this.getCommonButtonProps()}>
                {this.renderChildren()}
            </button>
        );
    }
}