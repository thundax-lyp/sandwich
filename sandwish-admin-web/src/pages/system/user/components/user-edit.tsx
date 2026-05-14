import { CameraOutlined } from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Avatar, Button, Input, Select, Switch, Upload } from "antd";
import { useEffect, useMemo, useRef, useState } from "react";
import {
    listUserRoles,
    type CreateUserForm,
    type UserDepartmentResponse,
    type UserRoleResponse,
    type UserResponse
} from "../user-service";
import { useCurrentAccessToken } from "@/auth/hooks";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import { getCurrentUserInfo } from "@/service/current-user-service";
import type { CurrentUserInfoResponse } from "@/service/current-user-service";

interface UserEditProps {
    open?: boolean;
    title: string;
    saveText: string;
    user?: UserResponse | null;
    departments?: UserDepartmentResponse[];
    saving?: boolean;
    onClose: () => void;
    onSave?: () => void;
    onCreate?: (form: CreateUserForm) => void;
    onAvatarUpload?: (file: File) => Promise<unknown> | void;
    onRolesChange?: (roles: UserRoleResponse[]) => void;
}

interface UserAvatarProps {
    user: UserResponse;
    size?: number;
}

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const getInitials = (name?: string | null) => {
    const normalizedName = normalizeSearch(name) || "U";
    return Array.from(normalizedName.replace(/\s+/g, "")).slice(0, 2).join("");
};

const readUserName = (user: UserResponse) => {
    return normalizeSearch(user.name) || normalizeSearch(user.loginName) || `用户 ${user.id}`;
};

const readDepartmentName = (user: UserResponse) => {
    return user.department?.namePath || user.department?.name || "";
};

const readRoleIds = (roles?: UserRoleResponse[] | null) => {
    return (roles || []).map((role) => role.id);
};

const readRankValue = (user?: Pick<CurrentUserInfoResponse, "ranks" | "superAdmin"> | null) => {
    if (!user) {
        return -1;
    }
    if (user.superAdmin) {
        return 9;
    }
    return user.ranks ?? 0;
};

const maxCreatableRank = (user?: Pick<CurrentUserInfoResponse, "ranks" | "superAdmin"> | null) => {
    return Math.max(readRankValue(user) - 1, 0);
};

const rankOptions = (maxRank: number) => {
    return Array.from({ length: Math.max(maxRank, 0) + 1 }, (_, rank) => ({
        value: rank,
        label: String(rank)
    }));
};

const departmentOptions = (departments: UserDepartmentResponse[]) => {
    return departments.map((department) => ({
        value: department.id,
        label: department.namePath || department.name
    }));
};

const EMPTY_USER_ROLES: UserRoleResponse[] = [];

const DEFAULT_CREATE_USER_FORM: CreateUserForm = {
    loginName: "",
    loginPass: "",
    name: "",
    email: "",
    mobile: "",
    departmentId: null,
    roleIds: [],
    ranks: 0,
    admin: false,
    enable: true
};

export const UserAvatar = ({ user, size }: UserAvatarProps) => {
    const accessToken = useCurrentAccessToken();
    const userName = readUserName(user);

    return (
        <Avatar size={size} src={toAuthenticatedResourceUrl(user.avatar, accessToken)}>
            {user.avatar ? null : getInitials(userName)}
        </Avatar>
    );
};

export const UserEdit = ({
    open,
    title,
    saveText,
    user,
    departments = [],
    saving,
    onClose,
    onSave,
    onCreate,
    onAvatarUpload,
    onRolesChange
}: UserEditProps) => {
    const [avatarUploading, setAvatarUploading] = useState(false);
    const [createForm, setCreateForm] = useState<CreateUserForm>(DEFAULT_CREATE_USER_FORM);
    const createInitializedRef = useRef(false);
    const editing = Boolean(user?.id);
    const visible = Boolean(open);
    const creating = visible && !editing;
    const updateForm = (values: Partial<CreateUserForm>) => {
        setCreateForm((currentForm) => ({ ...currentForm, ...values }));
    };
    const userRoleQuery = useQuery({
        queryKey: ["user", "role", "list"],
        queryFn: () => listUserRoles(),
        enabled: visible,
        retry: false
    });
    const currentUserQuery = useQuery({
        queryKey: ["current-user", "info"],
        queryFn: getCurrentUserInfo,
        enabled: creating && visible,
        retry: false
    });
    const createMaxRank = maxCreatableRank(currentUserQuery.data);
    const roleById = useMemo(() => {
        const roleById = new Map<string, UserRoleResponse>();
        [...(userRoleQuery.data ?? EMPTY_USER_ROLES), ...(user?.roles ?? [])].forEach((role) => {
            if (role?.id) {
                roleById.set(role.id, role);
            }
        });
        return roleById;
    }, [user?.roles, userRoleQuery.data]);
    const roleOptions = useMemo(() => {
        return Array.from(roleById.values()).map((role) => ({
            value: role.id,
            label: role.name || role.id
        }));
    }, [roleById]);
    useEffect(() => {
        if (!creating || !visible) {
            createInitializedRef.current = false;
            return;
        }
        if (createInitializedRef.current) {
            setCreateForm((currentForm) =>
                currentForm.ranks > createMaxRank
                    ? { ...currentForm, ranks: createMaxRank }
                    : currentForm
            );
            return;
        }
        createInitializedRef.current = true;
        setCreateForm({
            ...DEFAULT_CREATE_USER_FORM,
            departmentId: user?.department?.id || null,
            ranks: createMaxRank
        });
    }, [createMaxRank, creating, user?.department?.id, visible]);

    const selectRoles = (roleIds: string[]) => {
        onRolesChange?.(
            roleIds.map((roleId) => roleById.get(roleId) ?? { id: roleId, name: roleId })
        );
    };

    const saveForm = () => {
        if (creating) {
            onCreate?.(createForm);
            return;
        }
        onSave?.();
    };

    return (
        <SandwishDrawer
            className="user-edit-drawer"
            title={title}
            open={visible}
            size="small"
            onClose={onClose}
            extra={creating ? null : <Button size="small">−</Button>}
            footer={
                <div className="user-edit-footer">
                    <Button disabled={saving} onClick={onClose}>
                        取消
                    </Button>
                    <Button type="primary" loading={saving} onClick={saveForm}>
                        {saveText}
                    </Button>
                </div>
            }
        >
            {!creating && user ? (
                <div className="user-edit-form">
                    <div className="user-edit-avatar">
                        <UserAvatar user={user} size={64} />
                        <Upload
                            accept="image/*"
                            showUploadList={false}
                            beforeUpload={(file) => {
                                const uploadResult = onAvatarUpload?.(file);
                                if (uploadResult) {
                                    setAvatarUploading(true);
                                    Promise.resolve(uploadResult)
                                        .finally(() => setAvatarUploading(false))
                                        .catch(() => undefined);
                                }
                                return Upload.LIST_IGNORE;
                            }}
                        >
                            <Button
                                size="small"
                                shape="circle"
                                icon={<CameraOutlined />}
                                loading={avatarUploading}
                            />
                        </Upload>
                    </div>
                    <label>
                        <span>姓名</span>
                        <Input value={readUserName(user)} readOnly />
                    </label>
                    <label>
                        <span>登录名</span>
                        <Input value={user.loginName || ""} readOnly />
                    </label>
                    <label>
                        <span>邮箱</span>
                        <Input value={user.email || ""} readOnly />
                    </label>
                    <label>
                        <span>手机</span>
                        <Input value={user.mobile || ""} readOnly />
                    </label>
                    <label>
                        <span>部门</span>
                        <Input value={readDepartmentName(user)} readOnly />
                    </label>
                    <label>
                        <span>角色</span>
                        <Select
                            mode="multiple"
                            value={readRoleIds(user.roles)}
                            options={roleOptions}
                            loading={userRoleQuery.isFetching}
                            placeholder="选择角色"
                            onChange={selectRoles}
                        />
                    </label>
                </div>
            ) : null}
            {creating ? (
                <div className="user-edit-form">
                    <label>
                        <span>登录名</span>
                        <Input
                            value={createForm.loginName}
                            placeholder="lin.zhiyuan"
                            onChange={(event) => updateForm({ loginName: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>登录密码</span>
                        <Input.Password
                            value={createForm.loginPass}
                            placeholder="设置初始密码"
                            onChange={(event) => updateForm({ loginPass: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>姓名</span>
                        <Input
                            value={createForm.name}
                            placeholder="用户姓名"
                            onChange={(event) => updateForm({ name: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>邮箱</span>
                        <Input
                            value={createForm.email || ""}
                            placeholder="name@example.com"
                            onChange={(event) => updateForm({ email: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>手机</span>
                        <Input
                            value={createForm.mobile || ""}
                            placeholder="手机号"
                            onChange={(event) => updateForm({ mobile: event.target.value })}
                        />
                    </label>
                    <label>
                        <span>部门</span>
                        <Select
                            value={createForm.departmentId || undefined}
                            options={departmentOptions(departments)}
                            placeholder="选择部门"
                            showSearch
                            optionFilterProp="label"
                            onChange={(departmentId) => updateForm({ departmentId })}
                        />
                    </label>
                    <label>
                        <span>角色</span>
                        <Select
                            mode="multiple"
                            value={createForm.roleIds}
                            options={roleOptions}
                            loading={userRoleQuery.isFetching}
                            placeholder="选择角色"
                            onChange={(roleIds) => updateForm({ roleIds })}
                        />
                    </label>
                    <label>
                        <span>等级</span>
                        <Select
                            value={createForm.ranks}
                            options={rankOptions(createMaxRank)}
                            onChange={(ranks) => updateForm({ ranks })}
                        />
                    </label>
                    <label className="user-edit-switch-row">
                        <span>管理员</span>
                        <Switch
                            checked={createForm.admin}
                            onChange={(admin) => updateForm({ admin })}
                        />
                    </label>
                    <label className="user-edit-switch-row">
                        <span>启用</span>
                        <Switch
                            checked={createForm.enable}
                            onChange={(enable) => updateForm({ enable })}
                        />
                    </label>
                </div>
            ) : null}
        </SandwishDrawer>
    );
};
