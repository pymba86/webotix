import React from 'react';
import classNames from "classnames";

export interface LayoutWrapperProps {
    prefixCls?: string;
    className?: string;
    mobile: boolean;
}

export const LayoutWrapper: React.FC<LayoutWrapperProps> = ({
                                                                prefixCls = 'ui-layout',
                                                                className,
                                                                mobile,
                                                                children
                                                            }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-wrapper`]: !mobile,
        },
        className
    );

    return (
        <div className={classes}>
            {children}
        </div>
    )
};