import { postJson } from "@/api/http";
import type { PageResponse } from "@/api/page-response";

export type OpenClientStatus = "ENABLED" | "DISABLED";

export interface OpenClientPageRequest {
    pageNo?: number;
    pageSize?: number;
    name?: string | null;
    status?: OpenClientStatus | null;
}

export interface OpenClientSaveRequest {
    id?: string | null;
    name: string;
    ipWhitelist?: string | null;
    expiredAt?: string | null;
    remarks?: string | null;
    permissions?: string[] | null;
}

export interface OpenClientStatusRequest {
    id: string;
    status: OpenClientStatus;
}

export interface OpenClientSecretResetRequest {
    id: string;
}

export interface OpenClientIdRequest {
    id: string;
}

export interface OpenClientResponse {
    id: string;
    name: string;
    status?: OpenClientStatus | string | null;
    apiKey?: string | null;
    ipWhitelist?: string | null;
    expiredAt?: string | null;
    remarks?: string | null;
    permissions?: string[] | null;
}

export interface OpenClientSecretResponse {
    id: string;
    apiKey?: string | null;
    apiSecret?: string | null;
}

export const pageOpenClients = (request: OpenClientPageRequest = {}) => {
    return postJson<PageResponse<OpenClientResponse>, OpenClientPageRequest>("/open/client/page", {
        body: request
    });
};

export const getOpenClient = (request: OpenClientIdRequest) => {
    return postJson<OpenClientResponse, OpenClientIdRequest>("/open/client/get", {
        body: request
    });
};

export const createOpenClient = (request: OpenClientSaveRequest) => {
    return postJson<OpenClientSecretResponse, OpenClientSaveRequest>("/open/client/create", {
        body: request
    });
};

export const updateOpenClient = (request: OpenClientSaveRequest) => {
    return postJson<OpenClientResponse, OpenClientSaveRequest>("/open/client/update", {
        body: request
    });
};

export const changeOpenClientStatus = (request: OpenClientStatusRequest) => {
    return postJson<boolean, OpenClientStatusRequest>("/open/client/change-status", {
        body: request
    });
};

export const resetOpenClientSecret = (request: OpenClientSecretResetRequest) => {
    return postJson<OpenClientSecretResponse, OpenClientSecretResetRequest>(
        "/open/client/secret/reset",
        {
            body: request
        }
    );
};
