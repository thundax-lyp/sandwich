import {
    DeleteOutlined,
    EditOutlined,
    HolderOutlined,
    MoreOutlined,
    PlusOutlined,
    ReloadOutlined,
    SafetyCertificateOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button, Dropdown, Form, Input, Select, Space, Switch, Tag, Tree, Typography, message } from "antd";
import type { DataNode } from "antd/es/tree";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { SandwishConfirmModal } from "@/components/sandwish-confirm-modal";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import {
    addRole,
    deleteRoles,
    listRoleMenus,
    listRoles,
    sortRoles,
    updateRole,
    updateRoleStatus
} from "./role-service";
import type { RoleMenuResponse, RoleResponse, RoleSaveRequest } from "./role-service";
import "./role-page.css";

const { Text } = Typography;
const { TextArea } = Input;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    privilege: 120,
    status: 112,
    menuCount: 120,
    remarks: 280,
    actions: 154
};

interface RoleFormValues {
    id?: string | null;
    name: string;
    admin?: boolean | null;
    enable?: boolean | null;
    remarks?: string | null;
}

interface RoleFilters {
    enable: "ALL" | "ENABLED" | "DISABLED";
}

interface RoleMenuTreeNode extends RoleMenuResponse {
    children?: RoleMenuTreeNode[];
}

const DEFAULT_ROLE_FILTERS: RoleFilters = {
    enable: "ALL"
};

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const buildMenuTree = (menus: RoleMenuResponse[]) => {
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

const readFormRequest = (values: RoleFormValues, checkedMenuKeys: Key[]): RoleSaveRequest => {
    return {
        id: values.id,
        name: values.name.trim(),
        admin: Boolean(values.admin),
        enable: values.enable !== false,
        remarks: normalizeText(values.remarks),
        menus: checkedMenuKeys.map(String).map((id) => ({ id }))
    };
};

const sortByMove = (
    roles: RoleResponse[],
    sourceRole: RoleResponse,
    targetRole: RoleResponse,
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
    const [messageApi, contextHolder] = message.useMessage();
    const [editForm] = Form.useForm<RoleFormValues>();
    const queryClient = useQueryClient();
    const canViewRole = hasPermission("sys:role:view") || hasPermission("sys:role:edit");
    const canEditRole = hasPermission("sys:role:edit");
    const [query, setQuery] = useState({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<RoleFilters>(DEFAULT_ROLE_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingRole, setEditingRole] = useState<RoleResponse | null>(null);
    const [deletingRole, setDeletingRole] = useState<RoleResponse | null>(null);
    const [checkedMenuKeys, setCheckedMenuKeys] = useState<Key[]>([]);
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
        mutationFn: (values: RoleSaveRequest) => (values.id ? updateRole(values) : addRole(values)),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingRole(null);
            setCheckedMenuKeys([]);
            editForm.resetFields();
            await queryClient.invalidateQueries({ queryKey: ["role", "list"] });
            messageApi.success("角色已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const statusMutation = useMutation({
        mutationFn: updateRoleStatus,
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
        mutationFn: deleteRoles,
        onSuccess: async () => {
            setDeletingRole(null);
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
        setCheckedMenuKeys([]);
        editForm.resetFields();
        editForm.setFieldsValue({ admin: false, enable: true });
        setEditorOpen(true);
    };

    const openEditEditor = (role: RoleResponse) => {
        setEditingRole(role);
        setCheckedMenuKeys((role.menus || []).map((menu) => menu.id));
        editForm.setFieldsValue({
            id: role.id,
            name: role.name,
            admin: Boolean(role.admin),
            enable: role.enable !== false,
            remarks: role.remarks
        });
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingRole(null);
        setCheckedMenuKeys([]);
        editForm.resetFields();
    };

    const saveRole = async () => {
        const values = await editForm.validateFields();
        saveMutation.mutate(readFormRequest(values, checkedMenuKeys));
    };

    const updateSingleStatus = (role: RoleResponse, enable: boolean) => {
        if (!canEditRole) {
            return;
        }
        statusMutation.mutate([{ id: role.id, enable }]);
    };

    const batchUpdateStatus = (enable: boolean) => {
        statusMutation.mutate(selectedRowKeys.map((id) => ({ id: String(id), enable })));
    };

    const deleteRole = () => {
        if (!deletingRole) {
            return;
        }
        deleteMutation.mutate([deletingRole.id]);
    };

    const batchDeleteRoles = () => {
        deleteMutation.mutate(selectedRowKeys.map(String));
    };

    const sortRole = (
        sourceRole: RoleResponse,
        targetRole: RoleResponse,
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

    const columns: SandwishTableProps<RoleResponse>["columns"] = [
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
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, role) => (
                <div className="sandwish-table-row-actions">
                    <Space.Compact className="sandwish-table-row-actions-inline">
                        <Button
                            aria-label={`编辑 ${role.name}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditRole}
                            icon={<EditOutlined />}
                            type="text"
                            onClick={() => openEditEditor(role)}
                        />
                        <Button
                            aria-label={`删除 ${role.name}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditRole}
                            icon={<DeleteOutlined />}
                            type="text"
                            danger
                            onClick={() => setDeletingRole(role)}
                        />
                        <Button
                            aria-label={`拖动 ${role.name}`}
                            className="sandwish-table-row-action role-drag-action"
                            disabled={!canEditRole || sortMutation.isPending}
                            icon={<HolderOutlined />}
                            type="text"
                        />
                    </Space.Compact>
                    <Dropdown
                        menu={{
                            items: [
                                {
                                    key: "edit",
                                    disabled: !canEditRole,
                                    icon: <EditOutlined />,
                                    label: "编辑"
                                },
                                {
                                    key: "delete",
                                    danger: true,
                                    disabled: !canEditRole,
                                    icon: <DeleteOutlined />,
                                    label: "删除"
                                }
                            ],
                            onClick: ({ key }) => {
                                if (key === "edit") {
                                    openEditEditor(role);
                                }
                                if (key === "delete") {
                                    setDeletingRole(role);
                                }
                            }
                        }}
                        trigger={["click"]}
                    >
                        <Button
                            aria-label={`展开 ${role.name} 操作`}
                            className="sandwish-table-row-action sandwish-table-row-action-more"
                            icon={<MoreOutlined />}
                            type="text"
                        />
                    </Dropdown>
                </div>
            )
        }
    ];

    return (
        <>
            {contextHolder}
            <ListPage<RoleResponse>
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
                            <Button type="primary" icon={<PlusOutlined />} onClick={openCreateEditor}>
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

            <SandwishDrawer
                className="role-edit-drawer"
                title={editingRole ? "编辑角色" : "新增角色"}
                open={editorOpen}
                size="middle"
                onClose={closeEditor}
                footer={
                    <div className="role-edit-footer">
                        <Button onClick={closeEditor}>取消</Button>
                        <Button type="primary" loading={saveMutation.isPending} onClick={saveRole}>
                            保存角色
                        </Button>
                    </div>
                }
            >
                <Form<RoleFormValues> form={editForm} layout="vertical" className="role-editor-form">
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item
                        name="name"
                        label="角色名称"
                        rules={[{ required: true, message: "请输入角色名称" }]}
                    >
                        <Input placeholder="例如：运营管理员" />
                    </Form.Item>
                    <div className="role-editor-switches">
                        <Form.Item name="admin" label="管理权限" valuePropName="checked">
                            <Switch checkedChildren="管理" unCheckedChildren="普通" />
                        </Form.Item>
                        <Form.Item name="enable" label="角色状态" valuePropName="checked">
                            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
                        </Form.Item>
                    </div>
                    <Form.Item name="remarks" label="备注">
                        <TextArea rows={3} maxLength={200} showCount placeholder="角色说明" />
                    </Form.Item>
                    <div className="role-menu-panel">
                        <div className="role-menu-panel-head">
                            <Text strong>菜单权限</Text>
                            <Text type="secondary">{checkedMenuKeys.length} 项已选</Text>
                        </div>
                        <Tree
                            checkable
                            defaultExpandAll
                            checkedKeys={checkedMenuKeys}
                            defaultExpandedKeys={expandedMenuIds}
                            treeData={treeData}
                            selectable={false}
                            onCheck={(keys) => setCheckedMenuKeys(Array.isArray(keys) ? keys : keys.checked)}
                        />
                    </div>
                </Form>
            </SandwishDrawer>

            <SandwishConfirmModal
                title="删除角色"
                open={Boolean(deletingRole)}
                message={`确认删除 ${deletingRole?.name || ""}？`}
                description="删除后需要重新新增。若角色仍有关联用户，接口会按后端校验结果拦截。"
                okText="删除"
                confirmLoading={deleteMutation.isPending}
                cancelText="取消"
                onCancel={() => {
                    if (!deleteMutation.isPending) {
                        setDeletingRole(null);
                    }
                }}
                onOk={deleteRole}
            />
        </>
    );
};
