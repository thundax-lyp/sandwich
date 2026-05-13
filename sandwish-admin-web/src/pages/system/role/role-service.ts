import { postJson } from "@/api/http";

export interface RoleQueryRequest {
    enable?: boolean | null;
}

export interface RoleMenuRequest {
    id: string;
}

export interface RoleSaveRequest {
    id?: string | null;
    name?: string | null;
    admin?: boolean | null;
    enable?: boolean | null;
    remarks?: string | null;
    menus?: RoleMenuRequest[] | null;
}

export interface RoleStatusRequest {
    id: string;
    enable?: boolean | null;
}

export interface RoleSortRequest {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC";
}

export interface RoleMenuResponse {
    id: string;
    parentId?: string | null;
    name: string;
    perms?: string | null;
}

export interface RoleResponse {
    id: string;
    name: string;
    admin?: boolean | null;
    enable?: boolean | null;
    remarks?: string | null;
    menus?: RoleMenuResponse[] | null;
}

export const listRoles = (request: RoleQueryRequest = {}) => {
    return postJson<RoleResponse[], RoleQueryRequest>("/sys/role/list", {
        body: request
    });
};

export const listRoleMenus = () => {
    return postJson<RoleMenuResponse[]>("/sys/role/menu/tree");
};

export const addRole = (request: RoleSaveRequest) => {
    return postJson<RoleResponse, RoleSaveRequest>("/sys/role/create", {
        body: request
    });
};

export const updateRole = (request: RoleSaveRequest) => {
    return postJson<RoleResponse, RoleSaveRequest>("/sys/role/update", {
        body: request
    });
};

export const updateRoleStatus = (request: RoleStatusRequest[]) => {
    return postJson<boolean, RoleStatusRequest[]>("/sys/role/enable", {
        body: request
    });
};

export const sortRoles = (request: RoleSortRequest) => {
    return postJson<boolean, RoleSortRequest>("/sys/role/sort", {
        body: request
    });
};

export const deleteRoles = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/role/delete", {
        body: ids.map((id) => ({ id }))
    });
};
