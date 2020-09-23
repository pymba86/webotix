import styled from 'styled-components';
import {
    Link as ReactLink,
    LinkProps as ReactLinkProps
} from "react-router-dom"
import {
    fontSize, color, fontWeight, space,
    ColorProps, FontSizeProps, FontWeightProps, SpaceProps
} from "styled-system"
import * as React from "react";

export type LinkProps = ReactLinkProps
    & ColorProps
    & FontSizeProps
    & FontWeightProps
    & SpaceProps;

export const Link = styled(ReactLink)<LinkProps>`
    cursor: pointer;
    ${color}
    ${fontSize}
    ${fontWeight}
    ${space}
`;
