import * as React from 'react';
import FrameworkContainer from "./containers/Framework/FrameworkContainer";
import defaultTheme from "./theme";
import {defaultScales, generateColorsFromScales} from "./theme/colors";
import {ThemeProvider} from "styled-components";

const colors = generateColorsFromScales(defaultScales);

const theme = {
    ...defaultTheme,
    ...colors
};


export default class App extends React.Component {

    public render() {
        return (
            <ThemeProvider theme={theme}>
                <FrameworkContainer/>
            </ThemeProvider>
        );
    }
}
