import Cookies from "cookies-js"

const ACCESS_TOKEN = "accessToken";
const X_XSRF_TOKEN = "x-xsrf-token";

let xsrfToken = localStorage.getItem(X_XSRF_TOKEN);

const defaultSettings = { method: "GET" };

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

export async function getWeb(url: string): Promise<Response> {
    return fetch(url);
}

export async function get(url: string): Promise<Response> {
    return fetch("/api/" + url, action("GET"))
}

export async function put(url: string, content?: string): Promise<Response> {
    return fetch("/api/" + url, action("PUT", content))
}

export async function post(url: string, content?: string): Promise<Response> {
    return fetch("/api/" + url, action("POST", content))
}

export async function del(url: string, content?: string): Promise<Response> {
    return fetch("/api/" + url, action("DELETE", content))
}

function action(method: string, content?: string): RequestInit {
    return {
        ...defaultSettings,
        body: content,
        mode: "cors",
        redirect: "follow",
        headers: xsrfToken
            ? {
               // [X_XSRF_TOKEN]: xsrfToken,
                "Content-type": "application/json"
            }
            : {
                "Content-type": "application/json"
            },
        method
    }
}


