import React from "react";
import {ServerApi, ServerContext} from "./ServerContext"

export interface ServerApiProps {
    serverApi: ServerApi;
}

export function withServer<T extends ServerApiProps>(Component: React.ComponentType<T>) {
    return (props: T) => (
        <ServerContext.Consumer>
            {serverApi => <Component {...props} serverApi={serverApi}/>}
        </ServerContext.Consumer>
    );
}