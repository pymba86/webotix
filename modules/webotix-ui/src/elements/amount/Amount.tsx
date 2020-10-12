import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {formatNumber} from "../../modules/common/number";
import classNames from 'classnames';
import {Coin} from "../../modules/market";
import {Icon} from "../icon";

export type MovementType = "up" | "down" | null;

export type AmountColor = "buy" | "sell";

export interface AmountProps {
    noflash?: boolean;
    prefixCls?: string;
    className?: string;
    noValue?: string;
    name?: string;
    value?: number;
    heading?: boolean;
    color?: AmountColor;
    coin?: Coin;
    icon?: string;
    scale: number;
    onClick?: (value: number) => void;
}

export const Amount: React.FC<AmountProps> = ({
                                                  prefixCls = 'ui-amount',
                                                  className, value, color,
                                                  onClick,
                                                  name,
                                                  heading = false,
                                                  icon,
                                                  scale, noValue, noflash
                                              }) => {

    const [movement, setMovement] = useState<MovementType>(null);

    const [initialValue, setInitialValue] = useState<number | undefined>(value);

    const timeout = useRef<number | null>(null);

    const emptyValue = useMemo(
        () => noValue ? noValue : "...",
        [noValue]);

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-bare`]: true,
            [`${prefixCls}-${color}`]: color,
            [`${prefixCls}-${movement}`]: movement,
            [`${prefixCls}-noflash`]: noflash,
            [`${prefixCls}-heading`]: heading,
        },
        className
    );

    useEffect(() => {

        if (!noflash) {

            let movement: MovementType = null;

            if (Number(value) > Number(initialValue)) {
                movement = "up";
            } else if (Number(value) < Number(initialValue)) {
                movement = "down";
            }

            if (movement) {

                if (timeout.current !== null) {
                    clearTimeout(timeout.current);
                }

                setMovement(movement);

                timeout.current = window.setTimeout(
                    () => {
                        timeout.current = null;
                        setMovement(null);
                    }, 2100)
            }
            if (initialValue !== value) {
                setInitialValue(value);
            }
        }
        return () => {
            if (timeout.current)
                clearTimeout(timeout.current);
        }
    }, [initialValue, noflash, value]);

    const handleClick = useCallback(() => {
        if (onClick && value) {
            onClick(value);
        }
    }, [onClick, value]);

    if (heading) {
        return (
            <div className={classNames(prefixCls, `${prefixCls}-container`)}>
                <div className={classNames(prefixCls, `${prefixCls}-key`)}>
                    {name} {icon && <Icon type={icon}/>}
                </div>
                <div className={classes} onClick={handleClick}>
                    {value ? formatNumber(value, scale, emptyValue) : "--"}
                </div>
            </div>
        )
    } else {
        return (
            <span className={classes} onClick={handleClick}>
            {value ? formatNumber(value, scale, emptyValue) : "--"}
        </span>
        )
    }
};