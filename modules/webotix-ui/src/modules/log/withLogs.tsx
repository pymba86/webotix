import React from "react";
import {LogApi, LogContext} from "./LogContext";

export interface LogApiProps {
    logApi: LogApi;
}

export function withLog<T extends LogApiProps>(Component: React.ComponentType<T>) {
    return (props: T) => (
        <LogContext.Consumer>
            {logApi => <Component {...props} logApi={logApi}/>}
        </LogContext.Consumer>
    );
}