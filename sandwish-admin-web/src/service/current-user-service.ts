import { postJson } from "../api/http";

export interface CurrentUserInfoResponse {
    id: string;
    loginName: string;
    ranks?: number | null;
    name?: string | null;
    email?: string | null;
    mobile?: string | null;
    avatar?: string | null;
    admin?: boolean | null;
    superAdmin?: boolean | null;
}

export interface CurrentUserMenuResponse {
    id: string;
    parentId?: string | null;
    name: string;
    url?: string | null;
    icon?: string | null;
    displayParams?: string | null;
}

export interface CurrentUserPermsResponse {
    perms?: string[] | null;
}

export const getCurrentUserInfo = () => {
    return postJson<CurrentUserInfoResponse>("/sys/current-user/info");
};

export const listCurrentUserMenus = () => {
    return postJson<CurrentUserMenuResponse[]>("/sys/current-user/menus");
};

export const listCurrentUserPerms = () => {
    return postJson<CurrentUserPermsResponse>("/sys/current-user/perms");
};
