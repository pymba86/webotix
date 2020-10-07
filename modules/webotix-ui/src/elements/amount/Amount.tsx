import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import {formatNumber} from "../../modules/common/number";
import classNames from 'classnames';
import {Coin} from "../../modules/market";

export type MovementType = "up" | "down" | null;

export type AmountColor = "buy" | "sell";

export interface AmountProps {
    noflash?: boolean;
    prefixCls?: string;
    className?: string;
    noValue?: string;
    value?: number;
    color?: AmountColor;
    coin?: Coin;
    scale: number;
    bare?: boolean;
    onClick?: (value: number) => void;
}

export const Amount: React.FC<AmountProps> = ({
                                                  prefixCls = 'ui-amount',
                                                  className, value, color,
                                                  onClick, bare,
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
            [`${prefixCls}-bare`]: bare,
            [`${prefixCls}-${color}`]: color,
            [`${prefixCls}-${movement}`]: movement,
            [`${prefixCls}-noflash`]: noflash,
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
    }, [value]);

    const handleClick = useCallback(() => {
        if (onClick && value) {
            onClick(value);
        }
    }, [onClick, value]);

    return (
        <span className={classes} onClick={handleClick}>
            {value ? formatNumber(value, scale, emptyValue) : "--"}
        </span>
    )
};