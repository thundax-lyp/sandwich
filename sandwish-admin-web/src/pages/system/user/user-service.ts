import { postJson } from "@/api/http";
import type { PageResponse } from "@/api/page-response";

export interface UserPageRequest {
    pageNo?: number;
    pageSize?: number;
    departmentId?: string | null;
    loginName?: string | null;
    name?: string | null;
    enable?: boolean | null;
    orderBy?: string | null;
}

export interface UserDepartmentResponse {
    id: string;
    parentId?: string | null;
    name: string;
    shortName?: string | null;
    namePath?: string | null;
}

export interface UserRoleResponse {
    id: string;
    name: string;
}

export interface UserResponse {
    id: string;
    remarks?: string | null;
    loginName?: string | null;
    ranks?: number | null;
    name: string;
    email?: string | null;
    mobile?: string | null;
    avatar?: string | null;
    superAdmin?: boolean | null;
    admin?: boolean | null;
    enable?: boolean | null;
    department?: UserDepartmentResponse | null;
    roles?: UserRoleResponse[] | null;
}

export interface UserStatusRequest {
    id: string;
    enable?: boolean | null;
}

export interface UserSortRequest {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC";
}

export const pageUsers = (request: UserPageRequest = {}) => {
    return postJson<PageResponse<UserResponse>, UserPageRequest>("/sys/user/page", {
        body: request
    });
};

export const listUserDepartments = () => {
    return postJson<UserDepartmentResponse[]>("/sys/user/department/tree");
};

export const updateUserStatus = (request: UserStatusRequest[]) => {
    return postJson<boolean, UserStatusRequest[]>("/sys/user/enable", {
        body: request
    });
};

export const sortUsers = (request: UserSortRequest) => {
    return postJson<boolean, UserSortRequest>("/sys/user/sort", {
        body: request
    });
};

export const deleteUsers = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/user/delete", {
        body: ids.map((id) => ({ id }))
    });
};
