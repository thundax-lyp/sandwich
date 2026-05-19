import { postJson } from "@/api/http";

export interface MenuListRequest {
    parentId?: string | null;
    display?: boolean | null;
}

export interface MenuSaveRequest {
    id?: string | null;
    parentId?: string | null;
    name?: string | null;
    perms?: string | null;
    ranks?: number | null;
    display?: boolean | null;
    displayParams?: string | null;
    url?: string | null;
    remarks?: string | null;
}

export interface MenuMoveRequest {
    fromNodeId: string;
    toNodeId: string;
    type?: "after" | "before" | "inside" | "insideLast";
}

export interface MenuResponse {
    id: string;
    parentId?: string | null;
    name: string;
    perms?: string | null;
    ranks?: number | null;
    display?: boolean | null;
    displayParams?: string | null;
    url?: string | null;
    remarks?: string | null;
}

export const listMenus = (request: MenuListRequest = {}) => {
    return postJson<MenuResponse[], MenuListRequest>("/sys/menu/list", {
        body: request
    });
};

export const addMenu = (request: MenuSaveRequest) => {
    return postJson<MenuResponse, MenuSaveRequest>("/sys/menu/create", {
        body: request
    });
};

export const updateMenu = (request: MenuSaveRequest) => {
    return postJson<MenuResponse, MenuSaveRequest>("/sys/menu/update", {
        body: request
    });
};

export const removeMenus = (ids: string[]) => {
    return postJson<boolean, Array<{ id: string }>>("/sys/menu/delete", {
        body: ids.map((id) => ({ id }))
    });
};

export const moveMenu = (request: MenuMoveRequest) => {
    return postJson<boolean, MenuMoveRequest>("/sys/menu/move", {
        body: request
    });
};
