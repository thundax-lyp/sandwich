import { postJson } from "@/api/http";

export interface DepartmentListRequest {
    parentId?: string | null;
    name?: string | null;
    remarks?: string | null;
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
