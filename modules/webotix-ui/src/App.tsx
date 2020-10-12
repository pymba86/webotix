import React from 'react';
import {FrameworkContainer} from "./FrameworkContainer";
import {Server} from "./modules/server/Server";
import {LogManager} from "./modules/log/LogContext";
import {BrowserRouter} from "react-router-dom";
import {Socket} from "./modules/socket/Socket";
import {Market} from "./modules/market/Market";

function App() {
    return (
        <BrowserRouter>
            <LogManager>
                <Market>
                    <Server>
                        <Socket>
                            <FrameworkContainer/>
                        </Socket>
                    </Server>
                </Market>
            </LogManager>
        </BrowserRouter>
    );
}

export default App;
