import type { RoleMenuResponse } from "./role-service";

export interface RoleMenuTreeNode extends RoleMenuResponse {
    children?: RoleMenuTreeNode[];
}
