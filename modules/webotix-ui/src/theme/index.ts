import {defaultColors} from "./colors";
import baseStyled, { ThemedStyledInterface } from 'styled-components';


const iconSizes = {
    xs: "12px",
    sm: "16px",
    md: "20px",
    lg: "24px",
    xl: "32px"
};

export const defaultTheme = {
    ...defaultColors,
    iconSizes,
};

export type Theme = typeof defaultTheme;

export interface ThemeProps {
    theme: Theme
}

export type ThemeColors = typeof defaultColors.colors;

export const styled = baseStyled as ThemedStyledInterface<Theme>;

export default defaultTheme;
