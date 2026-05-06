export const ADMIN_API_BASE_URL = "/admin-api/api";

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
    const response = await fetch(`${ADMIN_API_BASE_URL}${path}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: options.body === undefined ? undefined : JSON.stringify(options.body)
    });

    const payload = (await response.json()) as ApiResponse<TResponse>;
    if (!response.ok || payload.code !== 0) {
        throw new ApiError(payload.code ?? response.status, payload.message || "请求失败");
    }

    return payload.data;
}
