import {createGlobalStyle, css} from "../lib/styled"

export const GlobalStyle = createGlobalStyle`
  ${({ theme }) => css`
    
    body {
      font-family: ${theme.fonts.sans};
      color: ${theme.colors.fore}
      background-color: ${theme.colors.fore};
    }
    
    html {
      box-sizing: border-box;
    }
    
        *,
    ::after,
    ::before {
      box-sizing: inherit;
    }
    
    h1 {
  color: white;
  font-size: 22px;
  font-weight: bold;
  }
    
  `}
`;
