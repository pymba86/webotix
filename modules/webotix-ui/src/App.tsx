import * as React from 'react';
import {ThemeProvider} from "./common/styled";
import theme from "./theme";
import {GlobalStyle} from "./theme/global"
import Button from "./elements/Button";

export default class App extends React.Component {

    public render() {
        return (
            <ThemeProvider theme={theme}>
                <React.Fragment>
                    <GlobalStyle/>
                    <Button>Webotix</Button>
                </React.Fragment>
            </ThemeProvider>
        );
    }
}
