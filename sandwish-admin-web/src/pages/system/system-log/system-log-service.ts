import { postJson } from "@/api/http";
import type { PageResponse } from "@/api/page-response";

export interface LogPageRequest {
    pageNo?: number;
    pageSize?: number;
    title?: string | null;
    userLoginName?: string | null;
    userName?: string | null;
    remoteAddr?: string | null;
    requestUri?: string | null;
    beginDate?: string | null;
    endDate?: string | null;
}

export interface LogDepartmentResponse {
    id?: string | null;
    name?: string | null;
    namePath?: string | null;
}

export interface LogUserResponse {
    id?: string | null;
    loginName?: string | null;
    name?: string | null;
    department?: LogDepartmentResponse | null;
}

export interface LogResponse {
    id: string;
    remarks?: string | null;
    createDate?: string | null;
    type?: string | null;
    title?: string | null;
    remoteAddr?: string | null;
    userAgent?: string | null;
    method?: string | null;
    requestUri?: string | null;
    requestParams?: string | null;
    createUser?: LogUserResponse | null;
}

export const pageLogs = (request: LogPageRequest = {}) => {
    return postJson<PageResponse<LogResponse>, LogPageRequest>("/sys/log/page", {
        body: request
    });
};
