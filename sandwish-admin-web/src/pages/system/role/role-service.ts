import { postJson } from "@/api/http";
import type { RoleMenuNode, RoleRecord } from "./role-types";

export interface RoleQuery {
    enable?: boolean | null;
}

export interface RoleMenuCommand {
    id: string;
}

export interface RoleSaveCommand {
    id?: string | null;
    name?: string | null;
    admin?: boolean | null;
    enable?: boolean | null;
    remarks?: string | null;
    menus?: RoleMenuCommand[] | null;
}

export interface RoleStatusCommand {
    roles: Array<{
        id: string;
        enable?: boolean | null;
    }>;
}

export interface RoleSortCommand {
    orderedIds: string[];
    sortDirection?: "ASC" | "DESC";
}

export const listRoles = (request: RoleQuery = {}) => {
    return postJson<RoleRecord[], RoleQuery>("/sys/role/list", {
        body: request
    });
};

export const listRoleMenus = () => {
    return postJson<RoleMenuNode[]>("/sys/role/menu/tree");
};

export const addRole = (request: RoleSaveCommand) => {
    return postJson<RoleRecord, RoleSaveCommand>("/sys/role/create", {
        body: request
    });
};

export const changeRoleInfo = (request: RoleSaveCommand) => {
    return postJson<RoleRecord, RoleSaveCommand>("/sys/role/update", {
        body: request
    });
};

export const changeRoleStatus = (request: RoleStatusCommand) => {
    return postJson<boolean, Array<{ id: string; enable?: boolean | null }>>("/sys/role/enable", {
        body: request.roles
    });
};

export const sortRoles = (request: RoleSortCommand) => {
    return postJson<boolean, RoleSortCommand>("/sys/role/sort", {
        body: request
    });
};

export const removeRoles = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/role/delete", {
        body: ids.map((id) => ({ id }))
    });
};
