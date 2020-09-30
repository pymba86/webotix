import React from 'react';
import classNames from "classnames";

export interface LayoutBoxProps {
    prefixCls?: string;
    className?: string;
}

export const LayoutBox: React.FC<LayoutBoxProps> = ({
                                                        prefixCls = 'ui-layout',
                                                        className,
                                                        children
                                                    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-box`]: true,
        },
        className
    );

    return (
        <div className={classes}>
            {children}
        </div>
    )
};