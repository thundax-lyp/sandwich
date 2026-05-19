import type { MenuResponse } from "./menu-service";

export interface MenuTableNode extends MenuResponse {
    children?: MenuTableNode[];
}
