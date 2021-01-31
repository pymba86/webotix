import React, {useEffect} from 'react';
import {FrameworkContainer} from "./FrameworkContainer";
import {Server} from "./modules/server/Server";
import {LogApi, LogManager, LogContext} from "./modules/log/LogContext";
import {BrowserRouter} from "react-router-dom";
import {Socket} from "./modules/socket/Socket";
import {Market} from "./modules/market/Market";
import {Provider as ReduxProvider} from "react-redux";
import {compose, createStore, applyMiddleware} from "redux";
import thunk from "redux-thunk";

import {reducers} from "./store/reducers";
import {Authorizer} from "./modules/auth/Authoriser";
import {AuthApi, AuthContext} from "./modules/auth/AuthContext";
import exchangeService from "./modules/market/exchangeService";
import * as coinsActions from "./store/coins/actions";
import {CoinPriceList} from "./store/coins/types";

const store = createStore(
    reducers,
    compose(
        applyMiddleware(thunk)
    )
)

const StoreManagement: React.FC<{ auth: AuthApi; logApi: LogApi }> = ({auth, logApi}) => {
    // Load state on successful authorisation
    const logTrace = logApi.trace;
    const errorPopup = logApi.errorPopup;
    useEffect(() => {
        const syncFunction: any = () => {
            return async (dispatch: any) => {
                await logTrace("Fetching server status");
                auth.authenticatedRequest(
                    () => exchangeService.fetchReferencePrices())
                    .then((list: CoinPriceList) => {
                        dispatch(coinsActions.setReferencePrices(list));
                    })
                    .catch((error: Error) => errorPopup("Could not fetch coin metadata: " + error.message));
            }
        }
        if (auth.authorised) {
            store.dispatch(syncFunction())
        }
    }, [auth, logTrace, errorPopup])

    return <></>
}

const ConnectedStoreManagement: React.FC<any> = () => (
    <AuthContext.Consumer>
        {(auth: AuthApi) => (
            <LogContext.Consumer>
                {(logApi: LogApi) => <StoreManagement auth={auth} logApi={logApi}/>}
            </LogContext.Consumer>
        )}
    </AuthContext.Consumer>
)

function App() {
    return (
        <BrowserRouter>
            <LogManager>
                <ReduxProvider store={store}>
                    <Authorizer>
                        <ConnectedStoreManagement/>
                        <Server>
                            <Socket>
                                <Market>
                                    <FrameworkContainer/>
                                </Market>
                            </Socket>
                        </Server>
                    </Authorizer>
                </ReduxProvider>
            </LogManager>
        </BrowserRouter>
    );
}

export default App;
