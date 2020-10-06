import React from "react";
import classNames from 'classnames';

export interface ToolbarProps {
    prefixCls?: string;
    className?: string;
}

export const Toolbar: React.FC<ToolbarProps> = ({
                                                    prefixCls = 'ui-toolbar',
                                                    className
                                                }) => {

    const boxClasses = classNames(
        prefixCls,
        {
            [`${prefixCls}-box`]: true,
        },
        className
    );

    return (
        <div className={boxClasses}>toolbar</div>
    )
}