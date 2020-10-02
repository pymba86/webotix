import React from 'react';
import {FrameworkContainer} from "./FrameworkContainer";
import {Server} from "./modules/server/Server";
import {LogManager} from "./modules/log/LogContext";

function App() {
    return (
        <LogManager>
            <Server>
                <FrameworkContainer/>
            </Server>
        </LogManager>
    );
}

export default App;
