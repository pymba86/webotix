import React from "react"

export interface AuthApi {
    authorised: boolean

    logout(): void;

    authenticatedRequest<T extends unknown>(
        responseGenerator: () => Promise<T>
    ): Promise<T>
}

export const AuthContext: React.Context<AuthApi> = React.createContext({} as AuthApi)
