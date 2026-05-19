export interface UserFormValues {
    loginName: string;
    loginPass: string;
    name: string;
    email?: string | null;
    mobile?: string | null;
    departmentId?: string | null;
    roleIds: string[];
    ranks: number;
    admin: boolean;
    enable: boolean;
}
