import * as React from 'react';
import {ThemeProvider} from "./lib/styled";
import theme from "./theme";
import { GlobalStyle } from "./theme/global"

export default class App extends React.Component {

    public render() {
        return (
            <ThemeProvider theme={theme}>
                <>
                <GlobalStyle/>
                <h1>Webotix</h1>
                    </>
            </ThemeProvider>
        );
    }
}
