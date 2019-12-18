import classNames from "classnames";
import * as React from "react";
import {HTMLDivProps, Props} from "../../utilities";

export interface NavbarProps extends Props, HTMLDivProps {
    fixedToTop?: boolean;
}

export class Navbar extends React.PureComponent<NavbarProps, {}> {

    public static displayName = 'Navbar';

    public render() {

        const {children, className, fixedToTop, ...htmlProps} = this.props;
        const classes = classNames('navbar', {['fixed-top']: fixedToTop}, className);

        return (
            <div className={classes} {...htmlProps}>
                {children}
            </div>
        );
    }

}


