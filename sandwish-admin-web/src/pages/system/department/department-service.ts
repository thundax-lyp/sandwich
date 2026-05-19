import { postJson } from "@/api/http";

export interface DepartmentListRequest {
    parentId?: string | null;
    name?: string | null;
    remarks?: string | null;
}

export interface DepartmentSaveRequest {
    id?: string | null;
    parentId?: string | null;
    name?: string | null;
    shortName?: string | null;
    remarks?: string | null;
}

export interface DepartmentMoveRequest {
    fromNodeId: string;
    toNodeId: string;
    type?: "after" | "before" | "inside" | "insideLast";
}

export interface DepartmentResponse {
    id: string;
    parentId?: string | null;
    name: string;
    shortName?: string | null;
    namePath?: string | null;
    remarks?: string | null;
}

export const listDepartments = (request: DepartmentListRequest = {}) => {
    return postJson<DepartmentResponse[], DepartmentListRequest>("/sys/department/list", {
        body: request
    });
};

export const addDepartment = (request: DepartmentSaveRequest) => {
    return postJson<DepartmentResponse, DepartmentSaveRequest>("/sys/department/create", {
        body: request
    });
};

export const updateDepartment = (request: DepartmentSaveRequest) => {
    return postJson<DepartmentResponse, DepartmentSaveRequest>("/sys/department/update", {
        body: request
    });
};

export const removeDepartments = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/department/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const moveDepartment = (request: DepartmentMoveRequest) => {
    return postJson<boolean, DepartmentMoveRequest>("/sys/department/move", {
        body: request
    });
};
