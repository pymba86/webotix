import {get, post} from "../common/fetch";
import {Exchange} from "../market";

class AuthService {

    async checkLoggedIn(): Promise<boolean> {
        const response = await get<Exchange[]>("exchanges")
        return Array.isArray(response)
    }

    async simpleLogin(credentials: any) {

        try {
            const response: any = await post("auth/login", JSON.stringify(credentials))

            return this.check(response);

        } catch (error) {
            throw new Error(error)
        }
    }

    check(response: any): any {
        if (response.success) {
            return response
        }
        throw new Error("Login failed")
    }

    async config() {
        const result: any = await get("auth/config")
        return await result.json()
    }
}

export default new AuthService()
