import * as React from "react"
import {PaletteAppearance} from "../../theme/colors";
import {styled, Theme, ThemeProps} from "../../theme";
import color from "color";
import {css} from "styled-components";

export type ButtonSize = "xs" | "sm" | "md" | "lg";

export type ButtonVariant = "solid" | "outline";

export type ButtonAppearance = PaletteAppearance;

export type ButtonType = "button" | "reset" | "submit";

export interface ButtonStyleProps {
    leftIcon?: React.ReactNode;
    rightIcon?: React.ReactNode;
    disabled?: boolean;
    loading?: boolean;
    active?: boolean;
    full?: boolean;
    size?: ButtonSize;
    variant?: ButtonVariant;
    appearance: ButtonAppearance;
    type?: ButtonType;
}

export interface ButtonProps extends ButtonStyleProps {
    children?: React.ReactNode;
    ref?: React.Ref<HTMLButtonElement>;
    component?: React.ElementType;
}

export type ButtonRef = HTMLButtonElement;

////////////////////////////////////////////////////////////

const getTextColor = (background: string, theme: Theme) => {
    return color(background).isDark() ? "white" : theme.modes.light.text.default;
};

const variantStyle = ({theme, ...props}: ButtonStyleProps & ThemeProps) => {

    const {appearance} = props;

    switch (appearance) {
        case "none":
            return css`
                      background-color: white;
                      color: ${getTextColor(theme.colors.intent.none.lightest, theme)};
                    `;
        case "primary":
            return css`
                      background-color: ${theme.colors.intent.primary.base};
                      color: ${getTextColor(theme.colors.intent.primary.base, theme)};
                    `;
        case "success":
            return css`
                      background-color: ${theme.colors.intent.success.base};
                      color: ${getTextColor(theme.colors.intent.success.base, theme)};
                    `;
        case "warning":
            return css`
                      background-color: ${theme.colors.intent.warning.base};
                      color: ${getTextColor(theme.colors.intent.warning.base, theme)};
                    `;
        case "danger":
            return css`background-color: ${theme.colors.intent.danger.base};
                      color: ${getTextColor(theme.colors.intent.danger.base, theme)};
                    `;
    }
};


const baseStyle = (props: ButtonStyleProps) => css`
  appearance: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 250ms;
  user-select: none;
  position: relative;
  white-space: nowrap;
  vertical-align: middle;
  line-height: 1.2;
  outline: none;
  border:none;
`;

const disabledStyle = (props: ButtonStyleProps) => props.disabled && css`
  cursor: not-allowed;
  opacity: 40%;
`;


export const ButtonStyle = styled.button<ButtonStyleProps>`
  ${baseStyle}
  ${disabledStyle}
  ${variantStyle}
  
`;

////////////////////////////////////////////////////////////

export const Button: React.RefForwardingComponent<ButtonRef, ButtonProps>
    = React.forwardRef(({
                            children,
                            component = "button",
                            size = "md",
                            appearance = "primary",
                            type = "button",
                            variant = "solid",
                            active,
                            full,
                            loading,
                            leftIcon,
                            rightIcon,
                            ...props
                        }: ButtonProps, ref) => {

        return (
            <ButtonStyle ref={ref} as={component} appearance={appearance}  {...props}>
                ${children}
            </ButtonStyle>
        )
    }
);

export default Button;
