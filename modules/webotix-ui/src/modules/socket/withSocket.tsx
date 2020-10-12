import React from "react";
import {SocketApi, SocketContext} from "./SocketContext";

export interface SocketApiProps {
    socketApi: SocketApi;
}

export function withContext<T extends SocketApiProps>(Component: React.ComponentType<T>) {
    return (props: T) => (
        <SocketContext.Consumer>
            {socketApi => <Component {...props} socketApi={socketApi}/>}
        </SocketContext.Consumer>
    );
}