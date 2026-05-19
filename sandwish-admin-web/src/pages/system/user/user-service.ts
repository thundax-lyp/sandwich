import { postFormData, postJson } from "@/api/http";
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

export interface UserSaveRequest {
    id?: string | null;
    remarks?: string | null;
    loginName?: string | null;
    loginPass?: string | null;
    token?: string | null;
    ranks?: number | null;
    name?: string | null;
    email?: string | null;
    mobile?: string | null;
    admin?: boolean | null;
    enable?: boolean | null;
    department?: { id: string } | null;
    roles?: Array<{ id: string }> | null;
}

export const pageUsers = (request: UserPageRequest = {}) => {
    return postJson<PageResponse<UserResponse>, UserPageRequest>("/sys/user/page", {
        body: request
    });
};

export const listUserDepartments = () => {
    return postJson<UserDepartmentResponse[]>("/sys/user/department/tree");
};

export const listUserRoles = () => {
    return postJson<UserRoleResponse[]>("/sys/user/role/list");
};

export const changeUserStatus = (request: UserStatusRequest[]) => {
    return postJson<boolean, UserStatusRequest[]>("/sys/user/enable", {
        body: request
    });
};

export const removeUsers = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/user/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const createUser = (request: UserSaveRequest) => {
    return postJson<UserResponse, UserSaveRequest>("/sys/user/create", {
        body: request
    });
};

export const changeUserInfo = (request: UserSaveRequest) => {
    return postJson<UserResponse, UserSaveRequest>("/sys/user/update", {
        body: request
    });
};

export const uploadUserAvatar = (id: string, avatar: File) => {
    const body = new FormData();
    body.append("id", id);
    body.append("avatar", avatar);
    return postFormData<boolean>("/sys/user/avatar/upload", body);
};
