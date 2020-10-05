import React from 'react';
import {FrameworkContainer} from "./FrameworkContainer";
import {Server} from "./modules/server/Server";
import {LogManager} from "./modules/log/LogContext";
import {BrowserRouter} from "react-router-dom";

function App() {
    return (
        <BrowserRouter>
            <LogManager>
                <Server>
                    <FrameworkContainer/>
                </Server>
            </LogManager>
        </BrowserRouter>
    );
}

export default App;
