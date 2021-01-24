import Cookies from "cookies-js"

const ACCESS_TOKEN = "accessToken";
const X_XSRF_TOKEN = "x-xsrf-token";

let xsrfToken = localStorage.getItem(X_XSRF_TOKEN);

const defaultSettings = {method: "GET"};

export function setAccessToken(token: string, expires: boolean): void {
    Cookies.set(ACCESS_TOKEN, token, {
        expires,
        path: "/",
        secure: window.location.protocol === "https:"
    })
}

export function setXsrfToken(token: string): void {
    xsrfToken = token;
    localStorage.setItem(X_XSRF_TOKEN, token);
}

export function clearXsrfToken(): void {
    xsrfToken = null;
    localStorage.removeItem(X_XSRF_TOKEN);
}

export async function getWeb<T>(url: string): Promise<T> {
    return api(url);
}

export async function get<T>(url: string): Promise<T> {
    return api("/api/" + url, action("GET"))
}

export async function put<T>(url: string, content?: string): Promise<T> {
    return api("/api/" + url, action("PUT", content))
}

export async function post<T>(url: string, content?: string): Promise<T> {
    return api("/api/" + url, action("POST", content))
}

export async function del<T>(url: string, content?: string): Promise<T> {
    return api("/api/" + url, action("DELETE", content))
}

function api<T>(input: RequestInfo, init?: RequestInit): Promise<T> {
    return fetch(input, init)
        .then(response => {
            if (!response.ok) {
                throw new Error(response.statusText);
            }

            return response.text()
                .then(data => data ? JSON.parse(data) : null);
        })
}

function action(method: string, content?: string): RequestInit {
    return {
        ...defaultSettings,
        body: content,
        mode: "cors",
        redirect: "follow",
        headers: xsrfToken
            ? {
                [X_XSRF_TOKEN]: xsrfToken,
                "Content-type": "application/json"
            }
            : {
                "Content-type": "application/json"
            },
        method
    }
}


