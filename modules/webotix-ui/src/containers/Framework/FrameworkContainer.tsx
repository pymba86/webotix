import * as React from "react";
import Framework from "./Framework";
import {Navbar} from "../../elements";
import ErrorBoundary from "../../components/ErrorBoundary";


export default class FrameworkContainer extends React.Component {

    render() {
        return (
            <>
                <ErrorBoundary>
                    <Navbar>Navbar</Navbar>
                </ErrorBoundary>
                <ErrorBoundary>
                    <Framework isMobile={false}/>
                </ErrorBoundary>
            </>
        )
    }
}
