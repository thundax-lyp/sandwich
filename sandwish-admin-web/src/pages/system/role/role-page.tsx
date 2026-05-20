import {
    DeleteOutlined,
    PlusOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Select, Space, Switch, Tag, Typography } from "antd";
import type { DataNode } from "antd/es/tree";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { useSandwishConfirm } from "@/components/sandwish-confirm-modal/hooks/use-sandwish-confirm";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import { RoleEdit } from "./components/role-edit";
import {
    addRole,
    removeRoles,
    listRoleMenus,
    listRoles,
    sortRoles,
    changeRoleInfo,
    changeRoleStatus
} from "./role-service";
import type { RoleSaveCommand } from "./role-service";
import type { RoleMenuNode, RoleMenuTreeNode, RoleRecord } from "./role-types";
import "./role-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    privilege: 120,
    status: 112,
    menuCount: 120,
    remarks: 280
};

interface RoleFilters {
    enable: "ALL" | "ENABLED" | "DISABLED";
}

const DEFAULT_ROLE_FILTERS: RoleFilters = {
    enable: "ALL"
};

const buildMenuTree = (menus: RoleMenuNode[]) => {
    const nodeMap = new Map<string, RoleMenuTreeNode>();
    const roots: RoleMenuTreeNode[] = [];

    menus.forEach((menu) => {
        nodeMap.set(menu.id, { ...menu });
    });

    nodeMap.forEach((menu) => {
        if (menu.parentId) {
            const parent = nodeMap.get(menu.parentId);
            if (parent) {
                parent.children = parent.children || [];
                parent.children.push(menu);
                return;
            }
        }
        roots.push(menu);
    });

    return roots;
};

const collectMenuIds = (menus: RoleMenuTreeNode[]): string[] => {
    return menus.flatMap((menu) => [
        menu.id,
        ...(menu.children ? collectMenuIds(menu.children) : [])
    ]);
};

const toTreeData = (menus: RoleMenuTreeNode[]): DataNode[] => {
    return menus.map((menu) => ({
        key: menu.id,
        title: (
            <Space size={8}>
                <span>{menu.name}</span>
                {menu.perms ? <Text type="secondary">{menu.perms}</Text> : null}
            </Space>
        ),
        children: menu.children ? toTreeData(menu.children) : undefined
    }));
};

const sortByMove = (
    roles: RoleRecord[],
    sourceRole: RoleRecord,
    targetRole: RoleRecord,
    position: SandwishTableSortPosition
) => {
    const sourceIndex = roles.findIndex((role) => role.id === sourceRole.id);
    const targetIndex = roles.findIndex((role) => role.id === targetRole.id);
    if (sourceIndex < 0 || targetIndex < 0) {
        return roles.map((role) => role.id);
    }

    const nextRoles = [...roles];
    const [movedRole] = nextRoles.splice(sourceIndex, 1);
    const nextTargetIndex = nextRoles.findIndex((role) => role.id === targetRole.id);
    nextRoles.splice(position === "before" ? nextTargetIndex : nextTargetIndex + 1, 0, movedRole);
    return nextRoles.map((role) => role.id);
};

export const RolePage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useSandwishConfirm();
    const queryClient = useQueryClient();
    const canViewRole = hasPermission("sys:role:view") || hasPermission("sys:role:edit");
    const canEditRole = hasPermission("sys:role:edit");
    const [query, setQuery] = useState({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<RoleFilters>(DEFAULT_ROLE_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingRole, setEditingRole] = useState<RoleRecord | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const hasSelectedRoles = selectedRowKeys.length > 0;
    const hasActiveFilters = filters.enable !== "ALL";

    const roleQuery = useQuery({
        queryKey: ["role", "list", query],
        queryFn: () => listRoles(query),
        enabled: canViewRole,
        retry: false
    });
    const roleMenuQuery = useQuery({
        queryKey: ["role", "menu", "tree"],
        queryFn: listRoleMenus,
        enabled: canViewRole,
        retry: false
    });
    const roles = useMemo(() => roleQuery.data || [], [roleQuery.data]);
    const filteredRoles = useMemo(() => {
        const keyword = searchText.trim().toLowerCase();
        if (!keyword) {
            return roles;
        }
        return roles.filter((role) => {
            return (
                role.name?.toLowerCase().includes(keyword) ||
                role.remarks?.toLowerCase().includes(keyword)
            );
        });
    }, [roles, searchText]);
    const menuTree = useMemo(() => buildMenuTree(roleMenuQuery.data || []), [roleMenuQuery.data]);
    const treeData = useMemo(() => toTreeData(menuTree), [menuTree]);
    const expandedMenuIds = useMemo(() => collectMenuIds(menuTree), [menuTree]);

    const saveMutation = useMutation({
        mutationFn: (values: RoleSaveCommand) =>
            values.id ? changeRoleInfo(values) : addRole(values),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingRole(null);
            await queryClient.invalidateQueries({ queryKey: ["role", "list"] });
            messageApi.success("角色已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const statusMutation = useMutation({
        mutationFn: changeRoleStatus,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await queryClient.invalidateQueries({ queryKey: ["role", "list"] });
            messageApi.success("角色状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "状态更新失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: removeRoles,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await queryClient.invalidateQueries({ queryKey: ["role", "list"] });
            messageApi.success("角色已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const sortMutation = useMutation({
        mutationFn: sortRoles,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["role", "list"] });
            messageApi.success("角色排序已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序失败");
        }
    });

    const applyFilters = () => {
        setSelectedRowKeys([]);
        setQuery({
            enable: filters.enable === "ALL" ? undefined : filters.enable === "ENABLED"
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_ROLE_FILTERS);
        setSelectedRowKeys([]);
        setQuery({});
    };

    const openCreateEditor = () => {
        setEditingRole(null);
        setEditorOpen(true);
    };

    const openEditEditor = (role: RoleRecord) => {
        setEditingRole(role);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingRole(null);
    };

    const saveRole = (request: RoleSaveCommand) => {
        saveMutation.mutate(request);
    };

    const updateSingleStatus = (role: RoleRecord, enable: boolean) => {
        if (!canEditRole) {
            return;
        }
        statusMutation.mutate({ roles: [{ id: role.id, enable }] });
    };

    const batchUpdateStatus = (enable: boolean) => {
        statusMutation.mutate({
            roles: selectedRowKeys.map((id) => ({ id: String(id), enable }))
        });
    };

    const confirmDeleteRole = (role: RoleRecord) => {
        confirm.danger({
            title: "删除角色",
            message: `确认删除 ${role.name || ""}？`,
            description: "删除后需要重新新增。若角色仍有关联用户，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync([role.id])
        });
    };

    const batchDeleteRoles = () => {
        confirm.danger({
            title: "批量删除角色",
            message: `确认删除 ${selectedRowKeys.length} 个角色？`,
            description: "删除后需要重新新增。若角色仍有关联用户，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync(selectedRowKeys.map(String))
        });
    };

    const sortRole = (
        sourceRole: RoleRecord,
        targetRole: RoleRecord,
        position: SandwishTableSortPosition
    ) => {
        if (!canEditRole || sourceRole.id === targetRole.id) {
            return;
        }
        sortMutation.mutate({
            orderedIds: sortByMove(roles, sourceRole, targetRole, position),
            sortDirection: "ASC"
        });
    };

    const columns: SandwishTableProps<RoleRecord>["columns"] = [
        {
            title: "角色名称",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (name: string) => (
                <Space size={8}>
                    <SafetyCertificateOutlined className="role-name-icon" />
                    <Text strong>{name}</Text>
                </Space>
            )
        },
        {
            title: "权限级别",
            dataIndex: "admin",
            key: "admin",
            width: DEFAULT_COLUMN_WIDTHS.privilege,
            render: (admin?: boolean | null) =>
                admin ? <Tag className="role-admin-tag">管理权限</Tag> : <Tag>普通角色</Tag>
        },
        {
            title: "状态",
            dataIndex: "enable",
            key: "enable",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (enable: boolean | null | undefined, role) => (
                <Switch
                    checked={enable !== false}
                    checkedChildren="启用"
                    unCheckedChildren="禁用"
                    disabled={!canEditRole || statusMutation.isPending}
                    onChange={(checked) => updateSingleStatus(role, checked)}
                />
            )
        },
        {
            title: "菜单权限",
            key: "menuCount",
            width: DEFAULT_COLUMN_WIDTHS.menuCount,
            render: (_, role) => `${role.menus?.length || 0} 项`
        },
        {
            title: "备注",
            dataIndex: "remarks",
            key: "remarks",
            width: DEFAULT_COLUMN_WIDTHS.remarks,
            ellipsis: true,
            render: (remarks?: string | null) => remarks || null
        },
        {
            key: "actions",
            options: (role) => [
                {
                    key: "edit",
                    text: "编辑",
                    ariaLabel: `编辑 ${role.name}`,
                    disabled: !canEditRole,
                    onClick: () => openEditEditor(role)
                },
                { type: "divider" },
                {
                    key: "delete",
                    text: "删除",
                    type: "danger",
                    ariaLabel: `删除 ${role.name}`,
                    disabled: !canEditRole,
                    onClick: () => confirmDeleteRole(role)
                },
                { type: "divider" },
                {
                    key: "drag",
                    text: "拖动",
                    ariaLabel: `拖动 ${role.name}`,
                    disabled: !canEditRole || sortMutation.isPending,
                    onClick: () => undefined
                }
            ]
        }
    ];

    return (
        <>
            <ListPage<RoleRecord>
                pageClassName="role-page"
                title="角色管理"
                description="维护后台角色、角色状态和菜单权限。"
                subjectName="角色"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={setSearchText}
                filterActive={hasActiveFilters}
                filter={({ closeFilter }) => (
                    <div className="role-filter-form">
                        <label>
                            <span>状态</span>
                            <Select
                                value={filters.enable}
                                options={[
                                    { label: "全部", value: "ALL" },
                                    { label: "启用", value: "ENABLED" },
                                    { label: "禁用", value: "DISABLED" }
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
                            className="role-filter-search"
                            icon={<SearchOutlined />}
                            onClick={() => {
                                applyFilters();
                                closeFilter();
                            }}
                        >
                            查询
                        </Button>
                    </div>
                )}
                pageActions={
                    <>
                        <Button icon={<ReloadOutlined />} onClick={() => roleQuery.refetch()}>
                            刷新
                        </Button>
                        {canEditRole ? (
                            <Button
                                type="primary"
                                icon={<PlusOutlined />}
                                onClick={openCreateEditor}
                            >
                                新增角色
                            </Button>
                        ) : null}
                    </>
                }
                batchClassName="role-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            disabled={!canEditRole || !hasSelectedRoles}
                            loading={statusMutation.isPending}
                            onClick={() => batchUpdateStatus(true)}
                        >
                            启用
                        </Button>
                        <Button
                            disabled={!canEditRole || !hasSelectedRoles}
                            loading={statusMutation.isPending}
                            onClick={() => batchUpdateStatus(false)}
                        >
                            禁用
                        </Button>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditRole || !hasSelectedRoles}
                            loading={deleteMutation.isPending}
                            onClick={batchDeleteRoles}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="role-table"
                columns={columns}
                dataSource={filteredRoles}
                loading={roleQuery.isFetching || sortMutation.isPending}
                pagination={false}
                scroll={{ x: 1006 }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    getCheckboxProps: () => ({
                        disabled: !canEditRole
                    })
                }}
                locale={{
                    emptyText: roleQuery.isError
                        ? "角色列表加载失败，请确认权限和接口状态。"
                        : "暂无角色"
                }}
                onSort={sortRole}
                sortable={canEditRole}
            />

            <RoleEdit
                key={editorOpen ? editingRole?.id || "create" : "closed"}
                open={editorOpen}
                role={editingRole}
                treeData={treeData}
                expandedMenuIds={expandedMenuIds}
                saving={saveMutation.isPending}
                onClose={closeEditor}
                onSave={saveRole}
            />
        </>
    );
};
