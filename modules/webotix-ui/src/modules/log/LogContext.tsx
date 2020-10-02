import React, {useMemo, useRef, useState} from 'react';
import {showBrowserNotification} from "../common/browser";
import {ErrorPopup} from "./ErrorPopup";

const ERROR = "ERROR"
const ALERT = "ALERT"
const INFO = "INFO"
const TRACE = "TRACE"
export type LogLevel = typeof ERROR | typeof ALERT | typeof INFO | typeof TRACE;

export interface LogRequest {
    message: string
    level: LogLevel
}

export interface LogEntry extends LogRequest {
    dateTime: Date
}

export interface LogApi {
    logs: Array<LogEntry>

    errorPopup(message: string): void

    localError(message: string): void

    localAlert(message: string): void

    localMessage(message: string): void

    trace(message: string): void

    add(entry: LogRequest): void

    clear(): void
}

export const LogContext = React.createContext<LogApi>({} as LogApi);

export const LogManager: React.FC = ({children}) => {

    const [error, setError] = useState<string | null>(null);
    const [logs, setLogs] = useState<LogEntry[]>([]);

    const last = useRef<LogEntry>();

    const add = useMemo(
        () => (request: LogRequest) => {

            if (!last.current || last.current.message !== request.message) {
                if (request.level === ALERT || request.level === ERROR) {
                    showBrowserNotification("webotix", request.message)
                }
            }

            if ("dateTime" in request) {
                last.current = request as LogEntry
            } else {
                last.current = {
                    ...request,
                    dateTime: new Date()
                }
            }
            setLogs([...logs, last.current])
        },
        [logs]
    )

    const api: LogApi = useMemo(
        () => ({
            logs,
            errorPopup: (message: string) => {
                setError(message);
            },
            localError: (message: string) => add({message, level: ERROR}),
            localAlert: (message: string) => add({message, level: ALERT}),
            localMessage: (message: string) => add({message, level: INFO}),
            trace: (message: string) => add({message, level: TRACE}),
            add,
            clear: () => {
                setLogs([])
            }
        }),
        [add, logs]
    )

    return (
        <LogContext.Provider value={api}>
            <React.Fragment>
                {error !== null ? <ErrorPopup message={error}
                                              onClose={() => setError(null)}/> : null}
                {children}
            </React.Fragment>
        </LogContext.Provider>
    )

}