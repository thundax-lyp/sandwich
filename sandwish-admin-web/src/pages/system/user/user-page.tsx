import {
    ApartmentOutlined,
    CameraOutlined,
    DeleteOutlined,
    EditOutlined,
    HolderOutlined,
    MoreOutlined,
    PoweroffOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    Avatar,
    Button,
    Dropdown,
    Input,
    Select,
    Space,
    Tag,
    Tree,
    Typography,
    message
} from "antd";
import type { DataNode } from "antd/es/tree";
import { useEffect, useMemo, useRef, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { SandwishConfirmModal } from "@/components/sandwish-confirm-modal";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import {
    deleteUsers,
    listUserDepartments,
    pageUsers,
    sortUsers,
    updateUserStatus
} from "./user-service";
import type { UserDepartmentResponse, UserPageRequest, UserResponse } from "./user-service";
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

const sortByMove = (
    users: UserResponse[],
    sourceUser: UserResponse,
    targetUser: UserResponse,
    position: SandwishTableSortPosition
) => {
    const sourceIndex = users.findIndex((user) => user.id === sourceUser.id);
    const targetIndex = users.findIndex((user) => user.id === targetUser.id);
    if (sourceIndex < 0 || targetIndex < 0) {
        return users;
    }

    const nextUsers = [...users];
    const [movedUser] = nextUsers.splice(sourceIndex, 1);
    const nextTargetIndex = nextUsers.findIndex((user) => user.id === targetUser.id);
    nextUsers.splice(position === "before" ? nextTargetIndex : nextTargetIndex + 1, 0, movedUser);
    return nextUsers;
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
    const [messageApi, contextHolder] = message.useMessage();
    const queryClient = useQueryClient();
    const departmentPanelRef = useRef<HTMLDivElement | null>(null);
    const [query, setQuery] = useState<UserPageRequest>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<UserFilters>(DEFAULT_USER_FILTERS);
    const [selectedDepartmentId, setSelectedDepartmentId] = useState(ALL_DEPARTMENT_ID);
    const [editingUser, setEditingUser] = useState<UserResponse | null>(null);
    const [deletingUser, setDeletingUser] = useState<UserResponse | null>(null);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const hasSelectedUsers = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.loginName.trim()) || filters.enable !== "ALL";
    const canEditUser = hasPermission("sys:user:edit");

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

    const sortMutation = useMutation({
        mutationFn: sortUsers,
        onSuccess: async () => {
            await invalidateUserPage();
            messageApi.success("用户顺序已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序失败");
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

    const moveUser = (
        sourceUser: UserResponse,
        targetUser: UserResponse,
        position: SandwishTableSortPosition
    ) => {
        if (!canEditUser || sourceUser.id === targetUser.id) {
            return;
        }
        const nextUsers = sortByMove(users, sourceUser, targetUser, position);
        sortMutation.mutate({
            orderedIds: nextUsers.map((user) => user.id)
        });
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
                        <Avatar src={user.avatar || undefined}>
                            {user.avatar ? null : getInitials(userName)}
                        </Avatar>
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
                const editDisabled = !canEditUser;
                const deleteDisabled = !canEditUser || Boolean(user.superAdmin);
                return (
                    <div className="sandwish-table-row-actions">
                        <Space.Compact className="sandwish-table-row-actions-inline">
                            <Button
                                aria-label={`编辑 ${userName}`}
                                className="sandwish-table-row-action"
                                disabled={editDisabled}
                                icon={<EditOutlined />}
                                type="text"
                                onClick={() => setEditingUser(user)}
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
                        <button
                            aria-label={`拖动排序 ${userName}`}
                            className="sandwish-table-row-action sandwish-table-row-drag-handle"
                            disabled={!canEditUser}
                            type="button"
                        >
                            <HolderOutlined />
                        </button>
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
                                        setEditingUser(user);
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
            {contextHolder}
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
                loading={userQuery.isFetching || sortMutation.isPending}
                onSort={moveUser}
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
                        disabled: Boolean(user.superAdmin)
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
                sortable={canEditUser}
            />

            <SandwishDrawer
                className="user-edit-drawer"
                title="编辑用户"
                open={Boolean(editingUser)}
                size="small"
                onClose={() => setEditingUser(null)}
                extra={<Button size="small">−</Button>}
                footer={
                    <div className="user-edit-footer">
                        <Button onClick={() => setEditingUser(null)}>取消</Button>
                        <Button type="primary" onClick={() => setEditingUser(null)}>
                            更新用户
                        </Button>
                    </div>
                }
            >
                {editingUser ? (
                    <div className="user-edit-form">
                        <div className="user-edit-avatar">
                            <Avatar size={64} src={editingUser.avatar || undefined}>
                                {editingUser.avatar ? null : getInitials(readUserName(editingUser))}
                            </Avatar>
                            <Button size="small" shape="circle" icon={<CameraOutlined />} />
                        </div>
                        <label>
                            <span>姓名</span>
                            <Input value={readUserName(editingUser)} readOnly />
                        </label>
                        <label>
                            <span>登录名</span>
                            <Input value={editingUser.loginName || ""} readOnly />
                        </label>
                        <label>
                            <span>邮箱</span>
                            <Input value={editingUser.email || ""} readOnly />
                        </label>
                        <label>
                            <span>手机</span>
                            <Input value={editingUser.mobile || ""} readOnly />
                        </label>
                        <label>
                            <span>部门</span>
                            <Input value={readDepartmentName(editingUser)} readOnly />
                        </label>
                        <label>
                            <span>角色</span>
                            <Select
                                mode="multiple"
                                value={readRoleNames(editingUser)}
                                options={readRoleNames(editingUser).map((roleName) => ({
                                    value: roleName,
                                    label: roleName
                                }))}
                            />
                        </label>
                    </div>
                ) : null}
            </SandwishDrawer>

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
