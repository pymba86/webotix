import styled, {css} from 'styled-components';
import {fontSize, color, space, fontFamily, ColorProps, FontSizeProps, FontFamilyProps, SpaceProps} from "styled-system"
import {darken, lighten} from "polished"
import Theme from "../../typings/theme";

interface Props extends ColorProps,
    FontSizeProps, FontFamilyProps, SpaceProps {
    bg?: | "black" | "white" | "disabled";
    theme: Theme;
}

const getButtonColor = (props: Props) =>
    props.bg ? props.theme.colors[props.bg] : props.theme.colors.link;

const Button = styled.button<Props>`
    ${({theme, bg}: Props) => css`
       text-transform: uppercase;
       font-weight: bold;
       border: 1px solid ${getButtonColor};
       border-radius: ${theme.radii[2] + "px"};
       background-color: ${getButtonColor};
        &:hover {
        cursor: pointer;
        background-color: ${props => darken(0.1, getButtonColor(props))};
        border: 1px solid ${props => darken(0.1, getButtonColor(props))};
      }
      &:active {
        background-color: ${props => lighten(0.1, getButtonColor(props))};
        border: 1px solid ${props => lighten(0.1, getButtonColor(props))};
      };
      &:disabled {
        cursor: auto;
        color: ${theme.colors.disabled};
        background-color: ${theme.colors.disabledBg};
        border: 1px solid ${theme.colors.disabledBg};
      };
      ${color}
      ${fontSize}
      ${fontFamily}
      ${space}
`}
`;

Button.displayName = "Button";
Button.defaultProps = {
    fontSize: 2,
    color: "white",
    mt: 2,
    p: 2,
    type: "button",
    fontFamily: "heading"
};

export default Button;
