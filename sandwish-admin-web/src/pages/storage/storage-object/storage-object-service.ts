import { postJson } from "@/api/http";
import type { Page } from "@/types/page";

export interface StoragePageRequest {
    pageNo?: number;
    pageSize?: number;
    contentType?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    originalFilename?: string | null;
    remarks?: string | null;
}

export interface StorageResponse {
    id: string;
    originalFilename?: string | null;
    extendName?: string | null;
    contentType?: string | null;
    ownerId?: string | null;
    ownerType?: string | null;
    objectStatus?: string | null;
    referenceStatus?: string | null;
    remarks?: string | null;
    contentUrl?: string | null;
}

export interface StorageSortRequest {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC";
}

export const pageStorageObjects = (request: StoragePageRequest = {}) => {
    return postJson<Page<StorageResponse>, StoragePageRequest>("/storage/object/page", {
        body: request
    });
};

export const removeStorageObjects = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/storage/object/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const sortStorageObjects = (request: StorageSortRequest) => {
    return postJson<boolean, StorageSortRequest>("/storage/object/sort", {
        body: request
    });
};
