import React from 'react';
import {FrameworkContainer} from "./FrameworkContainer";
import {Server} from "./modules/server/Server";

function App() {
    return (
        <Server>
            <FrameworkContainer/>
        </Server>
    );
}

export default App;
