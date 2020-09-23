import React from 'react';
import styled, {css} from 'styled-components';

interface ContainerProps {
    isFilled: boolean;
}

declare module 'styled-components' {
    export interface DefaultTheme {
        borderRadius: string

        colors: {
            main: string
            secondary: string
        }
    }
}

const Container = styled.div<ContainerProps>`
  border-radius: 10px;
  background: ${({theme}) => theme.colors.main};
  ${({isFilled, theme}) => isFilled && css`
    border: ${theme.colors.main}`};
`

function App() {
    return (
        <div className="App">
            <header className="App-header">
                <p>
                    Edit <code>src/App.tsx</code> and save to reload!@#.
                </p>
                <a
                    className="App-link"
                    href="https://reactjs.org"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    Learn React
                </a>
            </header>
        </div>
    );
}

export default App;
