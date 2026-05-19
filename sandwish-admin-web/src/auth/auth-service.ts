import { ADMIN_API_BASE_URL, postJson } from "@/api/http";

export interface LoginFormResponse {
    loginToken: string;
    refreshToken: string;
    expiredAt: number;
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
    expireAt?: number;
}

export interface LogoutRequest {
    token: string;
}

export interface TokenRefreshRequest {
    clientId?: string;
    refreshToken: string;
}

export const createLoginForm = () => {
    return postJson<LoginFormResponse>("/auth/session/pre-auth-session");
};

export const refreshCaptcha = (loginToken: string) => {
    return postJson<{ refreshed: boolean }, { loginToken: string }>("/auth/captcha/refresh", {
        body: { loginToken }
    });
};

export const login = (request: LoginRequest) => {
    return postJson<AccessTokenResponse, LoginRequest>("/auth/session/login", {
        body: request
    });
};

export const refreshAccessToken = (request: TokenRefreshRequest) => {
    return postJson<AccessTokenResponse, TokenRefreshRequest>("/auth/session/token/refresh", {
        body: request
    });
};

export const logout = (request: LogoutRequest) => {
    return postJson<boolean, LogoutRequest>("/auth/session/logout", {
        body: request
    });
};

export const getCaptchaUrl = (loginToken: string, version: number) => {
    const params = new URLSearchParams({
        loginToken,
        width: "150",
        height: "40",
        _: String(version)
    });

    return `${ADMIN_API_BASE_URL}/auth/captcha?${params.toString()}`;
};
