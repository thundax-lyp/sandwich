import { postFormData, postJson } from "@/api/http";
import type { Page } from "@/types/page";

export type SubmissionStatus = "SUBMITTED" | "APPROVED" | "REJECTED" | "CLOSED";
export type SubmissionSortDirection = "ASC" | "DESC";

export interface SubmissionPageRequest {
    pageNo?: number;
    pageSize?: number;
    status?: SubmissionStatus | null;
    submittedAtBegin?: string | null;
    submittedAtEnd?: string | null;
    sortDirection?: SubmissionSortDirection;
}

export interface SubmissionSaveRequest {
    title: string;
    content: string;
    imageObjectIds?: string[];
}

export interface SubmissionResponse {
    id: string;
    title: string;
    content: string;
    status?: SubmissionStatus | string | null;
    submittedAt?: string | null;
    imageObjectIds?: string[] | null;
}

export interface SubmissionStatusRequest {
    id: string;
    status: SubmissionStatus;
}

export interface SubmissionSortRequest {
    orderedIds: string[];
    sortDirection?: SubmissionSortDirection;
}

export interface StorageUploadResponse {
    id?: string | null;
    originalFilename?: string | null;
    extendName?: string | null;
    contentType?: string | null;
    contentUrl?: string | null;
    error?: string | null;
}

export const pageSubmissions = (request: SubmissionPageRequest = {}) => {
    return postJson<Page<SubmissionResponse>, SubmissionPageRequest>(
        "/submission/submission/page",
        {
            body: request
        }
    );
};

export const createSubmission = (request: SubmissionSaveRequest) => {
    return postJson<SubmissionResponse, SubmissionSaveRequest>("/submission/submission/create", {
        body: request
    });
};

export const changeSubmissionStatus = (request: SubmissionStatusRequest) => {
    return postJson<boolean, SubmissionStatusRequest>("/submission/submission/change-status", {
        body: request
    });
};

export const removeSubmissions = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/submission/submission/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const sortSubmissions = (request: SubmissionSortRequest) => {
    return postJson<boolean, SubmissionSortRequest>("/submission/submission/sort", {
        body: request
    });
};

export const uploadSubmissionImage = (file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return postFormData<StorageUploadResponse>("/submission/submission/image/upload", formData);
};
