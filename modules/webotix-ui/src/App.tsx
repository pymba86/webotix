import React from 'react';
import {Link} from './elements/Link';
import {BrowserRouter} from "react-router-dom";

function App() {
    return (
        <div className="App">
            <header className="App-header">
                <p>
                    Edit <code>src/App.tsx</code> and save to reload!@#.
                </p>
                <BrowserRouter>
                    <Link to={'#'} color={"red"} m={3} fontSize={4}>
                        Ссылка
                    </Link>
                </BrowserRouter>
            </header>
        </div>
    );
}

export default App;
