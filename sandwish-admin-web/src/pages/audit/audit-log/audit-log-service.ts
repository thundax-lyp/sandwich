import { postJson } from "@/api/http";
import type { PageResponse } from "@/api/page-response";

export interface AuditLogPageRequest {
    pageNo?: number;
    pageSize?: number;
    objectType?: string | null;
    objectId?: string | null;
    action?: string | null;
    operatorType?: string | null;
    operatorId?: string | null;
    source?: string | null;
    requestId?: string | null;
    beginDate?: string | null;
    endDate?: string | null;
}

export interface AuditOptionResponse {
    value: string;
    label: string;
}

export interface AuditOptionsResponse {
    objectTypes?: AuditOptionResponse[];
    actions?: AuditOptionResponse[];
    operatorTypes?: AuditOptionResponse[];
}

export interface AuditFieldResponse {
    fieldName?: string | null;
    fieldLabel?: string | null;
    beforeDisplayValue?: string | null;
    afterDisplayValue?: string | null;
}

export interface AuditSnapshotFieldResponse {
    fieldName?: string | null;
    fieldLabel?: string | null;
    displayValue?: string | null;
    valueType?: string | null;
    sensitive?: boolean | null;
}

export interface AuditSnapshotResponse {
    objectType?: string | null;
    objectId?: string | null;
    displayName?: string | null;
    fields?: AuditSnapshotFieldResponse[] | null;
}

export interface AuditLogResponse {
    id: string;
    objectType?: string | null;
    objectId?: string | null;
    objectDisplayName?: string | null;
    objectTypeLabel?: string | null;
    version?: number | null;
    action?: string | null;
    actionLabel?: string | null;
    operatorType?: string | null;
    operatorTypeLabel?: string | null;
    operatorId?: string | null;
    operatorName?: string | null;
    source?: string | null;
    requestId?: string | null;
    traceId?: string | null;
    remoteAddr?: string | null;
    summary?: string | null;
    occurredAt?: string | null;
    changedFields?: AuditFieldResponse[] | null;
}

export interface AuditLogDetailResponse extends AuditLogResponse {
    idempotencyKey?: string | null;
    previousVersion?: number | null;
    beforeSnapshot?: AuditSnapshotResponse | null;
    afterSnapshot?: AuditSnapshotResponse | null;
}

export const pageAuditLogs = (request: AuditLogPageRequest = {}) => {
    return postJson<PageResponse<AuditLogResponse>, AuditLogPageRequest>("/audit/log/page", {
        body: request
    });
};

export const getAuditLogDetail = (id: string) => {
    return postJson<AuditLogDetailResponse, { id: string }>("/audit/log/detail", {
        body: { id }
    });
};

export const getAuditOptions = () => {
    return postJson<AuditOptionsResponse, Record<string, never>>("/audit/log/options", {
        body: {}
    });
};
