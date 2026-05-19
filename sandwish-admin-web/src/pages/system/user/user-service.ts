import { postFormData, postJson } from "@/api/http";
import type { Page } from "@/types/page";
import type { UserDepartmentNode, UserRecord, UserRoleRecord } from "./user-types";

export interface UserPageQuery {
    pageNo?: number;
    pageSize?: number;
    departmentId?: string | null;
    loginName?: string | null;
    name?: string | null;
    enable?: boolean | null;
    orderBy?: string | null;
}

export interface UserStatusCommand {
    id: string;
    enable?: boolean | null;
}

export interface UserSaveCommand {
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

export const pageUsers = (request: UserPageQuery = {}) => {
    return postJson<Page<UserRecord>, UserPageQuery>("/sys/user/page", {
        body: request
    });
};

export const listUserDepartments = () => {
    return postJson<UserDepartmentNode[]>("/sys/user/department/tree");
};

export const listUserRoles = () => {
    return postJson<UserRoleRecord[]>("/sys/user/role/list");
};

export const changeUserStatus = (request: UserStatusCommand[]) => {
    return postJson<boolean, UserStatusCommand[]>("/sys/user/enable", {
        body: request
    });
};

export const removeUsers = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/user/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const createUser = (request: UserSaveCommand) => {
    return postJson<UserRecord, UserSaveCommand>("/sys/user/create", {
        body: request
    });
};

export const changeUserInfo = (request: UserSaveCommand) => {
    return postJson<UserRecord, UserSaveCommand>("/sys/user/update", {
        body: request
    });
};

export const uploadUserAvatar = (id: string, avatar: File) => {
    const body = new FormData();
    body.append("id", id);
    body.append("avatar", avatar);
    return postFormData<boolean>("/sys/user/avatar/upload", body);
};
