import type { DepartmentResponse } from "./department-service";

export interface DepartmentTableNode extends DepartmentResponse {
    children?: DepartmentTableNode[];
}
