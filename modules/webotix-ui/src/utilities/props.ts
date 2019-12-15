import * as React from "react";

import {Intent} from "./intent";

/**
 * Базовый класс свойств для всех компонентов
 */
export interface Props {
    /** Список названий css классов через пробел */
    className?: string;
}

/**
 * Свойство компонента цвет действия
 */
export interface IntentProps {
    /** Визуальное отображение цвета действия */
    intent?: Intent;
}

export interface ActionProps extends IntentProps, Props {
    /** Является ли действие интерактивным */
    disabled?: boolean;

    /** Обработчик события нажатия */
    onClick?: (event: React.MouseEvent<HTMLElement>) => void;

    /** Текст действия */
    text?: React.ReactNode;
}
