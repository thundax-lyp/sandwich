import { ADMIN_API_BASE_URL, postJson } from "./http";

export interface LoginFormResponse {
    loginToken: string;
    refreshToken: string;
    expireSeconds: number;
    publicKey: string;
}

export interface LoginRequest {
    loginToken: string;
    userName: string;
    password: string;
    captcha?: string;
}

export interface AccessTokenResponse {
    token: string;
    refreshToken?: string;
}

export interface LogoutRequest {
    token: string;
}

export function createLoginForm() {
    return postJson<LoginFormResponse>("/auth/form");
}

export function refreshCaptcha(loginToken: string) {
    return postJson<{ refreshed: boolean }, { loginToken: string }>("/auth/captcha/refresh", {
        body: { loginToken }
    });
}

export function login(request: LoginRequest) {
    return postJson<AccessTokenResponse, LoginRequest>("/auth/login", {
        body: request
    });
}

export function logout(request: LogoutRequest) {
    return postJson<boolean, LogoutRequest>("/auth/logout", {
        body: request
    });
}

export function buildCaptchaUrl(loginToken: string, version: number) {
    const params = new URLSearchParams({
        loginToken,
        width: "150",
        height: "40",
        _: String(version)
    });

    return `${ADMIN_API_BASE_URL}/auth/captcha?${params.toString()}`;
}
