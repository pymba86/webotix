import React, {ButtonHTMLAttributes} from 'react';
import classNames from "classnames";

export type  LayoutBoxProps = ButtonHTMLAttributes<HTMLDivElement> & {
    prefixCls?: string;
    className?: string;
}

export const LayoutBox: React.FC<LayoutBoxProps> = ({
                                                        prefixCls = 'ui-layout',
                                                        className,
                                                        children,
                                                        ...props
                                                    }) => {

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-box`]: true,
        },
        className
    );

    return (
        <div {...props} className={classes}>
            {children}
        </div>
    )
};