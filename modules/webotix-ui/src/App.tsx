import React from 'react';
import {FrameworkContainer} from "./FrameworkContainer";
import {Server} from "./modules/server/Server";
import {LogManager} from "./modules/log/LogContext";
import {BrowserRouter} from "react-router-dom";
import {Socket} from "./modules/socket/Socket";
import {Market} from "./modules/market/Market";
import {Provider as ReduxProvider} from "react-redux";
import {compose, createStore, applyMiddleware} from "redux";
import thunk from "redux-thunk";

import {reducers} from "./store/reducers";

const store = createStore(
    reducers,
    compose(
        applyMiddleware(thunk)
    )
)

function App() {
    return (
        <BrowserRouter>
            <LogManager>
                <ReduxProvider store={store}>
                    <Server>
                        <Socket>
                            <Market>
                                <FrameworkContainer/>
                            </Market>
                        </Socket>
                    </Server>
                </ReduxProvider>
            </LogManager>
        </BrowserRouter>
    );
}

export default App;
