import { clearAccessToken, getAccessToken } from "../auth/token-storage";

export const ADMIN_API_BASE_URL = "/admin-api/api";
const ACCESS_TOKEN_HEADER = "Access-Token";

interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

interface RequestOptions<TBody> {
    body?: TBody;
}

export class ApiError extends Error {
    readonly code: number;

    constructor(code: number, message: string) {
        super(message);
        this.name = "ApiError";
        this.code = code;
    }
}

export async function postJson<TResponse, TBody = unknown>(
    path: string,
    options: RequestOptions<TBody> = {}
) {
    const headers: HeadersInit = {
        "Content-Type": "application/json"
    };
    const token = getAccessToken();
    if (token) {
        headers[ACCESS_TOKEN_HEADER] = token;
    }

    const response = await fetch(`${ADMIN_API_BASE_URL}${path}`, {
        method: "POST",
        headers,
        body: options.body === undefined ? undefined : JSON.stringify(options.body)
    });

    const payload = (await response.json()) as ApiResponse<TResponse>;
    if (!response.ok || payload.code !== 0) {
        const code = payload.code ?? response.status;
        if (code === 401) {
            clearAccessToken();
        }
        throw new ApiError(code, payload.message || "请求失败");
    }

    return payload.data;
}
