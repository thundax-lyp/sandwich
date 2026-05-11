import { postJson } from "@/api/http";
import type { PageResponse } from "@/api/page-response";

export interface DictPageRequest {
    pageNo?: number;
    pageSize?: number;
    type?: string | null;
    label?: string | null;
    remarks?: string | null;
}

export interface DictSaveRequest {
    id?: number | null;
    type?: string | null;
    label?: string | null;
    value?: string | null;
    remarks?: string | null;
}

export interface DictResponse {
    id: number;
    type: string;
    label: string;
    value: string;
    remarks?: string | null;
}

export const pageDictionaries = (request: DictPageRequest = {}) => {
    return postJson<PageResponse<DictResponse>, DictPageRequest>("/sys/dict/page", {
        body: request
    });
};

export const addDictionary = (request: DictSaveRequest) => {
    return postJson<DictResponse, DictSaveRequest>("/sys/dict/create", {
        body: request
    });
};

export const updateDictionary = (request: DictSaveRequest) => {
    return postJson<DictResponse, DictSaveRequest>("/sys/dict/update", {
        body: request
    });
};

export const deleteDictionaries = (ids: number[]) => {
    return postJson<boolean, Array<{ id: number }>>("/sys/dict/delete", {
        body: ids.map((id) => ({ id }))
    });
};
