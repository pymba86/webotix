import styled, {css} from 'styled-components';

interface ButtonProps {
    color: string;
}

const ButtonBase = styled.button<ButtonProps>`
    color: ${({color}) => color}
`;

const Button = styled(ButtonBase).attrs<ButtonProps>(
    {color: "123"})(ButtonBase);