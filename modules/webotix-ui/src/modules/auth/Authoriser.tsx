import React, {useState, useEffect, ReactElement, useCallback, useMemo, ReactNode} from "react"
import {Login} from "./Login"
import LoginDetails from "./LoginDetails"
import authService from "./auth"
import {
    AuthContext,
    AuthApi
} from "./AuthContext"
import {clearXsrfToken, setXsrfToken} from "../common/fetch";

export interface AuthorizerProps {
    onError?(message: string): void

    children: ReactNode
}

/**
 * Self-contained authorisation/login component. Displays child components
 * only if authorised, otherwise shows the relevant login components.
 *
 * Provides a context API for logging out and performing API calls, handling
 * authentication errors by logging out.
 *
 * @param props
 */
export const Authorizer: React.FC<AuthorizerProps> = (props: AuthorizerProps) => {
    const [loading, setLoading] = useState(true)
    const [loggedIn, setLoggedIn] = useState(false)
    const [error, setError] = useState<string | undefined>(undefined)
    const authorised = loggedIn

    const propsOnError = props.onError
    const onError = useMemo(
        () => (message: string) => {
            setError(message)
            if (propsOnError) propsOnError(message)
        },
        [setError, propsOnError]
    )

    const checkConnected = useCallback(
        () =>
            (async function (): Promise<boolean> {
                try {
                    console.log("Testing access")
                    const success = await authService.checkLoggedIn()
                    if (success) {
                        console.log("Logged in")
                    } else {
                        console.log("Not logged in")
                    }
                    if (success) {
                        setLoggedIn(true)
                        setError(undefined)
                    }
                    return success
                } catch (e) {
                    setLoggedIn(false)
                    setError(e)
                }
                return false;
            })(),
        [setLoggedIn, setError]
    )

    const onLogin = useMemo(
        () =>
            async function (details: LoginDetails): Promise<void> {
                authService
                    .simpleLogin(details)
                    .then(({expiry, xsrf}) => {
                        try {
                            console.log("Setting XSRF token")
                            setXsrfToken(xsrf)
                        } catch (error) {
                            throw new Error("Malformed access token")
                        }
                        setLoggedIn(true)
                        setError(undefined)
                    })
                    .then(checkConnected)
                    .catch(error => {
                        console.log(`Login failed: ${error.message}`)
                        onError(error.message)
                    })
            },
        [checkConnected, setLoggedIn, setError, onError]
    )
    const logout = useMemo(
        () =>
            function (): void {
                console.log("Logging out")
                clearXsrfToken()
                setLoggedIn(false)
            },
        [setLoggedIn]
    )

    const authenticatedRequest = useMemo(
        () => async <T extends unknown>(
            responseGenerator: () => Promise<T>
        ): Promise<T> => {
            try {
                return await responseGenerator();
            } catch (error) {
                throw new Error("Server error (" + error + ")")
            }
        },
        [logout]
    )

    const api: AuthApi = useMemo(
        () => ({
            authorised,
            logout,
            authenticatedRequest
        }),
        [authorised, logout, authenticatedRequest]
    )

    useEffect(() => {
        checkConnected()
            .finally(() => setLoading(false))
    }, [checkConnected, onError])

    if (loading) {
        return null;
    } else if (!loggedIn) {
        return <Login error={error} onLogin={onLogin}/>
    } else {
        return <AuthContext.Provider value={api}>{props.children}</AuthContext.Provider>
    }
}
