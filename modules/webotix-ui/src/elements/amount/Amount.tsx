import React, {useCallback, useContext, useEffect, useMemo, useRef, useState} from 'react';
import {formatNumber} from "../../modules/common/number";
import classNames from 'classnames';
import {Coin} from "../../modules/market";
import {Icon} from "../icon";
import {CoinMetadata} from "../../modules/server";
import {ServerContext} from "../../modules/server/ServerContext";
import {FrameworkContext} from "../../FrameworkContainer";

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
    onClick?: (value: number) => void;
    deriveScale?: (coin: CoinMetadata) => number;
}

export const Amount: React.FC<AmountProps> = ({
                                                  prefixCls = 'ui-amount',
                                                  className, value, color,
                                                  onClick,
                                                  name,
                                                  heading = false,
                                                  deriveScale,
                                                  coin,
                                                  icon,
                                                  noValue, noflash
                                              }) => {

    const serverApi = useContext(ServerContext);
    const frameworkApi = useContext(FrameworkContext);

    const [movement, setMovement] = useState<MovementType>(null);

    const [initialValue, setInitialValue] = useState<number | undefined>(value);

    const scale = useMemo(() => {
        const meta = deriveScale && coin ? serverApi.coinMetadata.get(coin.key) : undefined;
        return meta && deriveScale ? deriveScale(meta) : -1;
    }, [coin, deriveScale, serverApi])

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
    }, [noflash, value]);

    const handleClick = useCallback(() => {
        if (onClick && value) {
            onClick(value);
        } else if (value) {
            frameworkApi
                .populateLastFocusedField(Number(formatNumber(value, scale, "")));
        }
    }, [onClick, value, scale, frameworkApi]);

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