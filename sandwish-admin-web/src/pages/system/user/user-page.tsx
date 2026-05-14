import {
    ApartmentOutlined,
    DeleteOutlined,
    EditOutlined,
    MoreOutlined,
    PlusOutlined,
    PoweroffOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Dropdown, Input, Select, Space, Tag, Tree, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useEffect, useMemo, useRef, useState } from "react";
import type { Key } from "react";
import { sm2 } from "sm-crypto";
import { createLoginForm } from "@/api/auth-api";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { SandwishConfirmModal } from "@/components/sandwish-confirm-modal";
import type { SandwishTableProps } from "@/components/sandwish-table";
import { getCurrentUserInfo } from "@/service/current-user-service";
import type { CurrentUserInfoResponse } from "@/service/current-user-service";
import { UserAvatar, UserEdit } from "./components/user-edit";
import {
    createUser,
    deleteUsers,
    listUserDepartments,
    pageUsers,
    updateUser,
    uploadUserAvatar,
    updateUserStatus
} from "./user-service";
import type {
    CreateUserForm,
    UserDepartmentResponse,
    UserPageRequest,
    UserResponse,
    UserRoleResponse,
    UserSaveRequest
} from "./user-service";
import "./user-page.css";

const { Text } = Typography;

const ALL_DEPARTMENT_ID = "all";
const DEFAULT_PAGE_NO = 1;
const DEFAULT_PAGE_SIZE = 10;
const DEPARTMENT_PANEL_BOTTOM_GAP = 8;

const DEFAULT_COLUMN_WIDTHS = {
    name: 230,
    loginName: 160,
    department: 190,
    roles: 190,
    status: 120,
    ranks: 90,
    actions: 116
};

type UserFilterStatus = "ALL" | "ENABLED" | "DISABLED";

interface UserFilters {
    loginName: string;
    enable: UserFilterStatus;
}

const DEFAULT_USER_FILTERS: UserFilters = {
    loginName: "",
    enable: "ALL"
};

const EMPTY_USERS: UserResponse[] = [];
const EMPTY_DEPARTMENTS: UserDepartmentResponse[] = [];

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readUserName = (user: UserResponse) => {
    return normalizeSearch(user.name) || normalizeSearch(user.loginName) || `用户 ${user.id}`;
};

const readDepartmentName = (user: UserResponse) => {
    return user.department?.namePath || user.department?.name || "";
};

const readRoleNames = (user: UserResponse) => {
    return (user.roles || []).map((role) => role.name).filter(Boolean);
};

const statusLabel = (user: UserResponse) => {
    return user.enable === false ? "禁用" : "启用";
};

const statusClassName = (user: UserResponse) => {
    return user.enable === false ? "user-status-inactive" : "user-status-active";
};

const rankLabel = (user: UserResponse) => {
    if (user.superAdmin || user.ranks === 9) {
        return "超级管理员";
    }
    return `等级 ${user.ranks ?? 0}`;
};

const rankClassName = (user: UserResponse) => {
    return user.superAdmin || user.ranks === 9 ? "user-rank-super-admin" : "user-rank-badge";
};

const roleClassName = (user: UserResponse, index: number) => {
    if (user.admin || user.superAdmin) {
        return "user-role-admin";
    }
    return index === 0 ? "user-role-editor" : "user-role-viewer";
};

const readRankValue = (user?: Pick<UserResponse, "ranks" | "superAdmin"> | null) => {
    if (!user) {
        return -1;
    }
    if (user.superAdmin) {
        return 9;
    }
    return user.ranks ?? 0;
};

const canManageUserByRank = (
    currentUser: CurrentUserInfoResponse | undefined,
    targetUser: UserResponse
) => {
    if (currentUser?.superAdmin) {
        return true;
    }
    return readRankValue(targetUser) < readRankValue(currentUser);
};

const buildDepartmentTree = (departments: UserDepartmentResponse[]): DataNode[] => {
    const rootDepartment: UserDepartmentResponse = {
        id: ALL_DEPARTMENT_ID,
        parentId: null,
        name: "全部部门",
        shortName: "全部"
    };
    const allDepartments = [rootDepartment, ...departments];
    const childrenByParentId = new Map<string | null | undefined, UserDepartmentResponse[]>();
    allDepartments.forEach((department) => {
        const parentId =
            department.parentId || (department.id === ALL_DEPARTMENT_ID ? null : ALL_DEPARTMENT_ID);
        const children = childrenByParentId.get(parentId) || [];
        children.push(department);
        childrenByParentId.set(parentId, children);
    });

    const toNode = (department: UserDepartmentResponse): DataNode => ({
        key: department.id,
        title: (
            <span className="user-department-node">
                <span>{department.name}</span>
            </span>
        ),
        children: childrenByParentId.get(department.id)?.map(toNode)
    });

    return (childrenByParentId.get(null) || []).map(toNode);
};

const collectTreeKeys = (nodes: DataNode[]): Key[] => {
    return nodes.flatMap((node) => [
        node.key,
        ...(node.children ? collectTreeKeys(node.children) : [])
    ]);
};

const toEnableQueryValue = (enable: UserFilterStatus) => {
    if (enable === "ENABLED") {
        return true;
    }
    if (enable === "DISABLED") {
        return false;
    }
    return undefined;
};

export const UserPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const departmentPanelRef = useRef<HTMLDivElement | null>(null);
    const [query, setQuery] = useState<UserPageRequest>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<UserFilters>(DEFAULT_USER_FILTERS);
    const [selectedDepartmentId, setSelectedDepartmentId] = useState(ALL_DEPARTMENT_ID);
    const [activeUser, setActiveUser] = useState<UserResponse | null>(null);
    const [userEditorOpen, setUserEditorOpen] = useState(false);
    const [deletingUser, setDeletingUser] = useState<UserResponse | null>(null);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const hasSelectedUsers = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.loginName.trim()) || filters.enable !== "ALL";
    const canEditUser = hasPermission("sys:user:edit");
    const isCreatingUser = userEditorOpen && !activeUser?.id;

    const userQuery = useQuery({
        queryKey: ["user", "page", query],
        queryFn: () => pageUsers(query),
        retry: false
    });
    const departmentQuery = useQuery({
        queryKey: ["user", "department", "tree"],
        queryFn: () => listUserDepartments(),
        retry: false
    });
    const currentUserQuery = useQuery({
        queryKey: ["current-user", "info"],
        queryFn: getCurrentUserInfo,
        retry: false
    });
    const pageData = userQuery.data;
    const users = useMemo(() => pageData?.records ?? EMPTY_USERS, [pageData?.records]);
    const totalCount = pageData?.count ?? pageData?.totalCount ?? 0;
    const departments = useMemo(
        () => departmentQuery.data ?? EMPTY_DEPARTMENTS,
        [departmentQuery.data]
    );
    const departmentTreeData = useMemo(() => buildDepartmentTree(departments), [departments]);
    const departmentTreeKeys = useMemo(
        () => collectTreeKeys(departmentTreeData),
        [departmentTreeData]
    );
    useEffect(() => {
        const departmentPanel = departmentPanelRef.current;
        if (!departmentPanel) {
            return undefined;
        }

        let frame = 0;
        const updateFloatingBounds = () => {
            frame = 0;
            const floatingContainer =
                departmentPanel.closest<HTMLElement>(".list-page-table-aside") ?? departmentPanel;
            const tableArea =
                departmentPanel.closest<HTMLElement>(".list-page-table-area") ?? floatingContainer;
            const topbar = document.querySelector(".topbar")?.getBoundingClientRect();
            const sidebar = document.querySelector(".sidebar")?.getBoundingClientRect();
            const stickyTop = Math.ceil((topbar?.bottom ?? 76) + 12);
            const bottomInset = Math.max(
                12,
                Math.round(
                    window.innerHeight -
                        (sidebar?.bottom ?? window.innerHeight - 12) +
                        DEPARTMENT_PANEL_BOTTOM_GAP
                )
            );
            const floatingTop = Math.max(
                stickyTop,
                Math.ceil(floatingContainer.getBoundingClientRect().top)
            );

            floatingContainer.style.setProperty("--user-department-sticky-top", `${stickyTop}px`);
            floatingContainer.style.setProperty(
                "--user-department-floating-top",
                `${floatingTop}px`
            );
            floatingContainer.style.setProperty(
                "--user-department-floating-bottom",
                `${bottomInset}px`
            );
            tableArea.style.setProperty("--user-department-sticky-top", `${stickyTop}px`);
            tableArea.style.setProperty("--user-department-floating-bottom", `${bottomInset}px`);
        };
        const scheduleUpdate = () => {
            if (frame) {
                return;
            }
            frame = window.requestAnimationFrame(updateFloatingBounds);
        };

        updateFloatingBounds();
        window.addEventListener("scroll", scheduleUpdate, { passive: true });
        window.addEventListener("resize", scheduleUpdate);

        const observer =
            typeof ResizeObserver === "undefined" ? null : new ResizeObserver(scheduleUpdate);
        const topbarElement = document.querySelector(".topbar");
        const sidebarElement = document.querySelector(".sidebar");
        if (observer && topbarElement) {
            observer.observe(topbarElement);
        }
        if (observer && sidebarElement) {
            observer.observe(sidebarElement);
        }

        return () => {
            if (frame) {
                window.cancelAnimationFrame(frame);
            }
            window.removeEventListener("scroll", scheduleUpdate);
            window.removeEventListener("resize", scheduleUpdate);
            observer?.disconnect();
        };
    }, [departmentTreeData]);

    const invalidateUserPage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["user", "page"] });
    };

    const statusMutation = useMutation({
        mutationFn: updateUserStatus,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await invalidateUserPage();
            messageApi.success("用户状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "状态更新失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: deleteUsers,
        onSuccess: async () => {
            setDeletingUser(null);
            setSelectedRowKeys([]);
            await invalidateUserPage();
            messageApi.success("用户已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const avatarUploadMutation = useMutation({
        mutationFn: ({ id, avatar }: { id: string; avatar: File }) => uploadUserAvatar(id, avatar),
        onSuccess: async (_, variables) => {
            const refreshedUsers = await userQuery.refetch();
            const refreshedUser = refreshedUsers.data?.records?.find(
                (user) => user.id === variables.id
            );
            if (refreshedUser) {
                setActiveUser(refreshedUser);
            }
            messageApi.success("头像已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "头像上传失败");
        }
    });
    const updateMutation = useMutation({
        mutationFn: updateUser,
        onSuccess: async (savedUser) => {
            setActiveUser(savedUser);
            await invalidateUserPage();
            setUserEditorOpen(false);
            setActiveUser(null);
            messageApi.success("用户已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "更新失败");
        }
    });
    const createMutation = useMutation({
        mutationFn: async (form: CreateUserForm) => {
            const loginForm = await createLoginForm();
            const encryptedPassword = sm2.doEncrypt(form.loginPass, loginForm.publicKey, 0);
            return createUser(
                toCreateUserSaveRequest(form, encryptedPassword, loginForm.loginToken)
            );
        },
        onSuccess: async () => {
            setUserEditorOpen(false);
            await invalidateUserPage();
            messageApi.success("用户已新增");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "新增失败");
        }
    });

    const updateQuery = (nextQuery: Partial<UserPageRequest>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => ({
            ...currentQuery,
            ...nextQuery,
            pageNo: nextQuery.pageNo || DEFAULT_PAGE_NO
        }));
    };

    const searchUsers = (value: string) => {
        setSearchText(value);
        updateQuery({ name: normalizeSearch(value), pageNo: DEFAULT_PAGE_NO });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_USER_FILTERS);
        updateQuery({
            loginName: undefined,
            enable: undefined,
            pageNo: DEFAULT_PAGE_NO
        });
    };

    const applyFilters = () => {
        updateQuery({
            loginName: normalizeSearch(filters.loginName),
            enable: toEnableQueryValue(filters.enable),
            pageNo: DEFAULT_PAGE_NO
        });
    };

    const selectDepartment = (keys: Key[]) => {
        const nextDepartmentId = String(keys[0] || ALL_DEPARTMENT_ID);
        setSelectedDepartmentId(nextDepartmentId);
        updateQuery({
            departmentId: nextDepartmentId === ALL_DEPARTMENT_ID ? undefined : nextDepartmentId,
            pageNo: DEFAULT_PAGE_NO
        });
    };

    const deleteUser = () => {
        if (!deletingUser) {
            return;
        }
        deleteMutation.mutate([deletingUser.id]);
    };

    const batchDeleteUsers = () => {
        if (!hasSelectedUsers || !canEditUser) {
            return;
        }
        deleteMutation.mutate(selectedRowKeys.map(String));
    };

    const batchUpdateStatus = (enable: boolean) => {
        if (!hasSelectedUsers || !canEditUser) {
            return;
        }
        statusMutation.mutate(selectedRowKeys.map((id) => ({ id: String(id), enable })));
    };

    const openCreateUser = () => {
        const selectedDepartment = departments.find(
            (department) => department.id === selectedDepartmentId
        );
        setActiveUser(
            selectedDepartment
                ? {
                      id: "",
                      name: "",
                      department: selectedDepartment,
                      roles: []
                  }
                : {
                      id: "",
                      name: "",
                      roles: []
                  }
        );
        setUserEditorOpen(true);
    };

    const changeEditingUserRoles = (roles: UserRoleResponse[]) => {
        if (!activeUser) {
            return;
        }
        setActiveUser({
            ...activeUser,
            roles
        });
    };

    const toUserSaveRequest = (user: UserResponse): UserSaveRequest => ({
        id: user.id,
        remarks: user.remarks,
        loginName: user.loginName,
        ranks: user.ranks,
        name: user.name,
        email: user.email,
        mobile: user.mobile,
        admin: user.admin,
        enable: user.enable,
        department: user.department?.id ? { id: user.department.id } : null,
        roles: (user.roles || []).map((role) => ({ id: role.id }))
    });

    const toCreateUserSaveRequest = (
        form: CreateUserForm,
        encryptedPassword: string,
        token: string
    ): UserSaveRequest => ({
        loginName: normalizeSearch(form.loginName),
        loginPass: encryptedPassword,
        token,
        ranks: form.ranks,
        name: normalizeSearch(form.name),
        email: normalizeSearch(form.email),
        mobile: normalizeSearch(form.mobile),
        admin: form.admin,
        enable: form.enable,
        department: form.departmentId ? { id: form.departmentId } : null,
        roles: form.roleIds.map((roleId) => ({ id: roleId }))
    });

    const saveCreatingUser = (form: CreateUserForm) => {
        if (!normalizeSearch(form.loginName)) {
            messageApi.error("请填写登录名");
            return;
        }
        if (!form.loginPass) {
            messageApi.error("请填写登录密码");
            return;
        }
        if (!normalizeSearch(form.name)) {
            messageApi.error("请填写姓名");
            return;
        }
        if (!form.departmentId) {
            messageApi.error("请选择部门");
            return;
        }
        createMutation.mutate(form);
    };

    const saveEditingUser = () => {
        if (!activeUser) {
            return;
        }
        updateMutation.mutate(toUserSaveRequest(activeUser));
    };

    const columns: SandwishTableProps<UserResponse>["columns"] = [
        {
            title: "用户",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (_, user) => {
                const userName = readUserName(user);
                return (
                    <Space size={10}>
                        <UserAvatar user={user} />
                        <div className="user-name-cell">
                            <Text strong>{userName}</Text>
                            {user.email ? <Text type="secondary">{user.email}</Text> : null}
                        </div>
                    </Space>
                );
            }
        },
        {
            title: "登录名",
            dataIndex: "loginName",
            key: "loginName",
            width: DEFAULT_COLUMN_WIDTHS.loginName,
            render: (loginName?: string | null) => loginName || null
        },
        {
            title: "部门",
            key: "department",
            width: DEFAULT_COLUMN_WIDTHS.department,
            render: (_, user) => readDepartmentName(user) || null
        },
        {
            title: "角色",
            key: "roles",
            width: DEFAULT_COLUMN_WIDTHS.roles,
            render: (_, user) => {
                const roleNames = readRoleNames(user);
                if (!roleNames.length) {
                    return null;
                }
                return (
                    <Space size={[4, 4]} wrap>
                        {roleNames.map((roleName, index) => (
                            <Tag key={roleName} className={roleClassName(user, index)}>
                                {roleName}
                            </Tag>
                        ))}
                    </Space>
                );
            }
        },
        {
            title: "状态",
            dataIndex: "enable",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (_, user) => <Tag className={statusClassName(user)}>{statusLabel(user)}</Tag>
        },
        {
            title: "级别",
            dataIndex: "ranks",
            key: "ranks",
            width: DEFAULT_COLUMN_WIDTHS.ranks,
            render: (_, user) => <Tag className={rankClassName(user)}>{rankLabel(user)}</Tag>
        },
        {
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, user) => {
                const userName = readUserName(user);
                const canManageCurrentUser = canManageUserByRank(currentUserQuery.data, user);
                const editDisabled = !canEditUser || !canManageCurrentUser;
                const deleteDisabled = !canEditUser || !canManageCurrentUser;
                return (
                    <div className="sandwish-table-row-actions">
                        <Space.Compact className="sandwish-table-row-actions-inline">
                            <Button
                                aria-label={`编辑 ${userName}`}
                                className="sandwish-table-row-action"
                                disabled={editDisabled}
                                icon={<EditOutlined />}
                                type="text"
                                onClick={() => {
                                    setActiveUser(user);
                                    setUserEditorOpen(true);
                                }}
                            />
                            <Button
                                aria-label={`删除 ${userName}`}
                                className="sandwish-table-row-action"
                                disabled={deleteDisabled}
                                icon={<DeleteOutlined />}
                                type="text"
                                danger
                                onClick={() => setDeletingUser(user)}
                            />
                        </Space.Compact>
                        <Dropdown
                            menu={{
                                items: [
                                    {
                                        key: "edit",
                                        disabled: editDisabled,
                                        icon: <EditOutlined />,
                                        label: "编辑"
                                    },
                                    {
                                        key: "delete",
                                        danger: true,
                                        disabled: deleteDisabled,
                                        icon: <DeleteOutlined />,
                                        label: "删除"
                                    }
                                ],
                                onClick: ({ key }) => {
                                    if (key === "edit") {
                                        setActiveUser(user);
                                        setUserEditorOpen(true);
                                    }
                                    if (key === "delete") {
                                        setDeletingUser(user);
                                    }
                                }
                            }}
                            trigger={["click"]}
                        >
                            <Button
                                aria-label={`展开 ${userName} 操作`}
                                className="sandwish-table-row-action sandwish-table-row-action-more"
                                icon={<MoreOutlined />}
                                type="text"
                            />
                        </Dropdown>
                    </div>
                );
            }
        }
    ];

    return (
        <>
            <ListPage<UserResponse>
                pageClassName="user-page"
                title="用户管理"
                description="管理后台用户、角色与权限状态。"
                subjectName="用户"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={searchUsers}
                filterActive={hasActiveFilters}
                filterClassName="user-filter-panel"
                filter={() => (
                    <div className="user-filter-form">
                        <label>
                            <span>登录名</span>
                            <Input
                                allowClear
                                placeholder="developer"
                                value={filters.loginName}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        loginName: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>状态</span>
                            <Select<UserFilterStatus>
                                value={filters.enable}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    { value: "ENABLED", label: "启用" },
                                    { value: "DISABLED", label: "禁用" }
                                ]}
                                onChange={(enable) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        enable
                                    }))
                                }
                            />
                        </label>
                        <Button onClick={resetFilters} disabled={!hasActiveFilters}>
                            重置
                        </Button>
                        <Button
                            className="user-filter-search"
                            icon={<SearchOutlined />}
                            onClick={applyFilters}
                        >
                            查询
                        </Button>
                    </div>
                )}
                pageActions={
                    <Space wrap>
                        <Button
                            type="primary"
                            icon={<PlusOutlined />}
                            disabled={!canEditUser}
                            onClick={openCreateUser}
                        >
                            新增用户
                        </Button>
                        <Button
                            icon={<ReloadOutlined />}
                            loading={userQuery.isFetching || departmentQuery.isFetching}
                            onClick={() => {
                                userQuery.refetch();
                                departmentQuery.refetch();
                            }}
                        >
                            刷新
                        </Button>
                    </Space>
                }
                batchClassName="user-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            className="user-batch-neutral"
                            icon={<PoweroffOutlined />}
                            disabled={!hasSelectedUsers || !canEditUser}
                            loading={statusMutation.isPending}
                            onClick={() => batchUpdateStatus(false)}
                        >
                            禁用
                        </Button>
                        <Button
                            className="user-batch-enable"
                            icon={<PoweroffOutlined />}
                            disabled={!hasSelectedUsers || !canEditUser}
                            loading={statusMutation.isPending}
                            onClick={() => batchUpdateStatus(true)}
                        >
                            启用
                        </Button>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!hasSelectedUsers || !canEditUser}
                            loading={deleteMutation.isPending}
                            onClick={batchDeleteUsers}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="user-table"
                columns={columns}
                dataSource={users}
                loading={userQuery.isFetching}
                pagination={{
                    current: query.pageNo || DEFAULT_PAGE_NO,
                    pageSize: query.pageSize || DEFAULT_PAGE_SIZE,
                    total: totalCount,
                    showSizeChanger: true,
                    showTotal: (total) => `${total} 个用户`,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: (user) => ({
                        disabled: !canEditUser || !canManageUserByRank(currentUserQuery.data, user)
                    })
                }}
                tableAsidePlacement="left"
                tableAside={
                    <div className="user-department-panel" ref={departmentPanelRef}>
                        <div className="user-department-panel-head">
                            <Space size={8}>
                                <ApartmentOutlined />
                                <Text strong>部门</Text>
                            </Space>
                        </div>
                        <Tree
                            key={departmentTreeKeys.join(",")}
                            blockNode
                            defaultExpandedKeys={departmentTreeKeys}
                            selectedKeys={[selectedDepartmentId]}
                            treeData={departmentTreeData}
                            onSelect={selectDepartment}
                        />
                    </div>
                }
            />

            <UserEdit
                open={userEditorOpen}
                title={isCreatingUser ? "新增用户" : "编辑用户"}
                saveText={isCreatingUser ? "新增用户" : "更新用户"}
                user={activeUser}
                departments={departments}
                saving={isCreatingUser ? createMutation.isPending : updateMutation.isPending}
                onClose={() => {
                    setUserEditorOpen(false);
                    setActiveUser(null);
                }}
                onCreate={saveCreatingUser}
                onSave={saveEditingUser}
                onAvatarUpload={(avatar) => {
                    if (activeUser?.id) {
                        return avatarUploadMutation.mutateAsync({ id: activeUser.id, avatar });
                    }
                    return undefined;
                }}
                onRolesChange={changeEditingUserRoles}
            />

            <SandwishConfirmModal
                title="删除用户"
                open={Boolean(deletingUser)}
                message={`确认删除 ${deletingUser ? readUserName(deletingUser) : ""}？`}
                description="删除后需要重新新增。若用户存在安全约束，接口会按后端校验结果拦截。"
                okText="删除"
                confirmLoading={deleteMutation.isPending}
                cancelText="取消"
                onCancel={() => {
                    if (!deleteMutation.isPending) {
                        setDeletingUser(null);
                    }
                }}
                onOk={deleteUser}
            />
        </>
    );
};
