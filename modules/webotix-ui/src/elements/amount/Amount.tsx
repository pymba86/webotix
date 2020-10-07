import React, {useState} from 'react';
import {formatNumber} from "../../modules/common/number";
import classNames from 'classnames';

import Timeout = NodeJS.Timeout;

export type MovementType = "up" | "down" | "none";

export type AmountColor = "buy" | "sell";

export interface AmountProps {
    noflash?: boolean;
    prefixCls?: string;
    noValue?: string;
    value: number;
    color: AmountColor;
    scale: number;
    bare?: boolean;
    onClick: (value: number) => void;
}

export interface AmountState {
    movement: MovementType;
}

export const Amount: React.FC<AmountProps> = ({prefixCls, value}) => {

    const [movement, setMovement] = useState<AmountState>({movement: "none"});

    const noValue = this.props.noValue ? this.props.noValue : "...";

    const classes = classNames(
        prefixCls,
        {
            [`${prefixCls}-${variant}`]: !outline,
            [`${prefixCls}-outline-${variant}`]: outline,
        },
        className
    );

    return (
        <BareAmountValue
            px={this.props.noflash ? 0 : 1}
            movement={this.state.movement}
            onClick={this.onClick}
            color={this.props.color}
            className={this.props.className}
        >
            {this.props.children === "--" ? "--" : formatNumber(this.props.value, this.props.scale, noValue)}
        </BareAmountValue>
    )
}