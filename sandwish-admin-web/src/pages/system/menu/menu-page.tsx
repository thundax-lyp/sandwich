import {
    ArrowLeftOutlined,
    ArrowRightOutlined,
    DeleteOutlined,
    EditOutlined,
    HolderOutlined,
    MenuOutlined,
    PlusOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button, Form, Input, InputNumber, Select, Space, Tag, Typography, message } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { SandwishConfirmModal } from "@/components/sandwish-confirm-modal";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import { addMenu, deleteMenus, listMenus, moveMenu, updateMenu } from "./menu-service";
import type { MenuMoveRequest, MenuResponse, MenuSaveRequest } from "./menu-service";
import "./menu-page.css";

const { Text } = Typography;
const { TextArea } = Input;

const DEFAULT_COLUMN_WIDTHS = {
    name: 260,
    url: 260,
    perms: 240,
    display: 96,
    actions: 208
};

interface MenuTableNode extends MenuResponse {
    children?: MenuTableNode[];
}

interface MenuFormValues {
    id?: string | null;
    parentId?: string | null;
    name: string;
    perms?: string | null;
    ranks?: number | null;
    display?: boolean | null;
    displayParams?: string | null;
    url?: string | null;
    remarks?: string | null;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const buildMenuTree = (menus: MenuResponse[]) => {
    const nodeMap = new Map<string, MenuTableNode>();
    const roots: MenuTableNode[] = [];

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

const collectMenuIds = (menus: MenuTableNode[]): string[] => {
    return menus.flatMap((menu) => [
        menu.id,
        ...(menu.children ? collectMenuIds(menu.children) : [])
    ]);
};

const flattenMenus = (menus: MenuTableNode[]): MenuTableNode[] => {
    return menus.flatMap((menu) => [menu, ...(menu.children ? flattenMenus(menu.children) : [])]);
};

const collectDescendantIds = (menu?: MenuTableNode | null): Set<string> => {
    if (!menu?.children?.length) {
        return new Set();
    }
    return new Set(collectMenuIds(menu.children));
};

const readFormRequest = (values: MenuFormValues): MenuSaveRequest => {
    return {
        id: values.id,
        parentId: values.parentId || null,
        name: values.name.trim(),
        perms: normalizeText(values.perms),
        ranks: values.ranks,
        display: values.display !== false,
        displayParams: normalizeText(values.displayParams),
        url: normalizeText(values.url),
        remarks: normalizeText(values.remarks)
    };
};

const toMoveType = (position: SandwishTableSortPosition): MenuMoveRequest["type"] => {
    return position === "before" ? "before" : "after";
};

export const MenuPage = () => {
    const [messageApi, contextHolder] = message.useMessage();
    const [editForm] = Form.useForm<MenuFormValues>();
    const queryClient = useQueryClient();
    const [editingMenu, setEditingMenu] = useState<MenuTableNode | null>(null);
    const [deletingMenu, setDeletingMenu] = useState<MenuTableNode | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const [expandedRowKeys, setExpandedRowKeys] = useState<Key[] | null>(null);
    const canEditMenu = hasPermission("super");
    const menuQuery = useQuery({
        queryKey: ["menu", "list"],
        queryFn: () => listMenus(),
        retry: false
    });
    const menus = useMemo(() => menuQuery.data || [], [menuQuery.data]);
    const menuTree = useMemo(() => buildMenuTree(menus), [menus]);
    const flatMenus = useMemo(() => flattenMenus(menuTree), [menuTree]);
    const expandedMenuIds = useMemo(() => collectMenuIds(menuTree), [menuTree]);
    const actualExpandedRowKeys = expandedRowKeys || expandedMenuIds;
    const unavailableParentIds = useMemo(() => {
        const descendantIds = collectDescendantIds(editingMenu);
        if (editingMenu?.id) {
            descendantIds.add(editingMenu.id);
        }
        return descendantIds;
    }, [editingMenu]);
    const parentOptions = useMemo(
        () =>
            flatMenus
                .filter((menu) => !unavailableParentIds.has(menu.id))
                .map((menu) => ({
                    label: menu.name,
                    value: menu.id
                })),
        [flatMenus, unavailableParentIds]
    );

    const saveMutation = useMutation({
        mutationFn: (values: MenuSaveRequest) => (values.id ? updateMenu(values) : addMenu(values)),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingMenu(null);
            editForm.resetFields();
            await queryClient.invalidateQueries({ queryKey: ["menu", "list"] });
            messageApi.success("菜单已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: deleteMenus,
        onSuccess: async () => {
            setDeletingMenu(null);
            await queryClient.invalidateQueries({ queryKey: ["menu", "list"] });
            messageApi.success("菜单已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const moveMutation = useMutation({
        mutationFn: moveMenu,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["menu", "list"] });
            messageApi.success("菜单层级已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "移动失败");
        }
    });

    const openCreateEditor = () => {
        setEditingMenu(null);
        editForm.resetFields();
        editForm.setFieldsValue({ display: true, ranks: 0 });
        setEditorOpen(true);
    };

    const openEditEditor = (menu: MenuTableNode) => {
        setEditingMenu(menu);
        editForm.setFieldsValue({
            id: menu.id,
            parentId: menu.parentId || null,
            name: menu.name,
            perms: menu.perms,
            ranks: menu.ranks,
            display: menu.display !== false,
            displayParams: menu.displayParams,
            url: menu.url,
            remarks: menu.remarks
        });
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingMenu(null);
        editForm.resetFields();
    };

    const saveMenu = async () => {
        const values = await editForm.validateFields();
        saveMutation.mutate(readFormRequest(values));
    };

    const openDeleteConfirm = (menu: MenuTableNode) => {
        setDeletingMenu(menu);
    };

    const closeDeleteConfirm = () => {
        if (deleteMutation.isPending) {
            return;
        }
        setDeletingMenu(null);
    };

    const deleteMenu = () => {
        if (!deletingMenu) {
            return;
        }
        deleteMutation.mutate([deletingMenu.id]);
    };

    const sortMenu = (
        sourceMenu: MenuTableNode,
        targetMenu: MenuTableNode,
        position: SandwishTableSortPosition
    ) => {
        if (!canEditMenu || sourceMenu.id === targetMenu.id) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: sourceMenu.id,
            toNodeId: targetMenu.id,
            type: toMoveType(position)
        });
    };

    const readSiblingMenus = (menu: MenuTableNode) => {
        if (!menu.parentId) {
            return menuTree;
        }
        return flatMenus.find((item) => item.id === menu.parentId)?.children || [];
    };

    const readPreviousSiblingMenu = (menu: MenuTableNode) => {
        const siblings = readSiblingMenus(menu);
        const index = siblings.findIndex((item) => item.id === menu.id);
        return index > 0 ? siblings[index - 1] : null;
    };

    const promoteMenu = (menu: MenuTableNode) => {
        if (!canEditMenu || !menu.parentId) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: menu.id,
            toNodeId: menu.parentId,
            type: "after"
        });
    };

    const demoteMenu = (menu: MenuTableNode) => {
        const previousSibling = readPreviousSiblingMenu(menu);
        if (!canEditMenu || !previousSibling) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: menu.id,
            toNodeId: previousSibling.id,
            type: "insideLast"
        });
    };

    const columns: SandwishTableProps<MenuTableNode>["columns"] = [
        {
            title: "菜单名称",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (name: string) => (
                <Space size={8}>
                    <MenuOutlined className="menu-name-icon" />
                    <span>{name}</span>
                </Space>
            )
        },
        {
            title: "URL",
            dataIndex: "url",
            key: "url",
            width: DEFAULT_COLUMN_WIDTHS.url,
            ellipsis: true,
            render: (url?: string | null) => url || null
        },
        {
            title: "权限标识",
            dataIndex: "perms",
            key: "perms",
            width: DEFAULT_COLUMN_WIDTHS.perms,
            ellipsis: true,
            render: (perms?: string | null) => perms || null
        },
        {
            title: "显示",
            dataIndex: "display",
            key: "display",
            width: DEFAULT_COLUMN_WIDTHS.display,
            render: (display?: boolean | null) =>
                display === false ? <Tag>隐藏</Tag> : <Tag color="success">显示</Tag>
        },
        {
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, menu) => (
                <div className="sandwish-table-row-actions">
                    <Space.Compact className="sandwish-table-row-actions-inline">
                        <Button
                            aria-label={`升级 ${menu.name}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditMenu || !menu.parentId || moveMutation.isPending}
                            icon={<ArrowLeftOutlined />}
                            type="text"
                            onClick={() => promoteMenu(menu)}
                        />
                        <Button
                            aria-label={`降级 ${menu.name}`}
                            className="sandwish-table-row-action"
                            disabled={
                                !canEditMenu || !readPreviousSiblingMenu(menu) || moveMutation.isPending
                            }
                            icon={<ArrowRightOutlined />}
                            type="text"
                            onClick={() => demoteMenu(menu)}
                        />
                        <Button
                            aria-label={`编辑 ${menu.name}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditMenu}
                            icon={<EditOutlined />}
                            type="text"
                            onClick={() => openEditEditor(menu)}
                        />
                        <Button
                            aria-label={`删除 ${menu.name}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditMenu}
                            icon={<DeleteOutlined />}
                            type="text"
                            danger
                            onClick={() => openDeleteConfirm(menu)}
                        />
                        <Button
                            aria-label={`拖动 ${menu.name}`}
                            className="sandwish-table-row-action menu-drag-action"
                            disabled={!canEditMenu || moveMutation.isPending}
                            icon={<HolderOutlined />}
                            type="text"
                        />
                    </Space.Compact>
                </div>
            )
        }
    ];

    return (
        <>
            {contextHolder}
            <ListPage<MenuTableNode>
                pageClassName="menu-page"
                title="菜单管理"
                description="维护后台菜单树、页面入口和权限标识。"
                subjectName="菜单"
                pageActions={
                    <>
                        <Button icon={<ReloadOutlined />} onClick={() => menuQuery.refetch()}>
                            刷新
                        </Button>
                        {canEditMenu ? (
                            <Button type="primary" icon={<PlusOutlined />} onClick={openCreateEditor}>
                                新增菜单
                            </Button>
                        ) : null}
                    </>
                }
                rowKey="id"
                className="menu-table"
                columns={columns}
                dataSource={menuTree}
                loading={menuQuery.isFetching || moveMutation.isPending}
                pagination={false}
                scroll={{ x: 1064 }}
                expandable={{
                    defaultExpandAllRows: true,
                    expandedRowKeys: actualExpandedRowKeys,
                    indentSize: 24,
                    expandIconColumnIndex: 0,
                    onExpandedRowsChange: (keys) => setExpandedRowKeys([...keys])
                }}
                locale={{
                    emptyText: menuQuery.isError ? (
                        "菜单列表加载失败，请确认权限和接口状态。"
                    ) : (
                        <Space orientation="vertical" size={8}>
                            <MenuOutlined className="menu-empty-icon" />
                            <Text type="secondary">暂无菜单数据</Text>
                        </Space>
                    )
                }}
                onSort={sortMenu}
                sortable={canEditMenu}
            />

            <SandwishDrawer
                className="menu-edit-drawer"
                title={editingMenu ? "编辑菜单" : "新增菜单"}
                open={editorOpen}
                size="small"
                onClose={closeEditor}
                footer={
                    <div className="menu-edit-footer">
                        <Button onClick={closeEditor}>取消</Button>
                        <Button type="primary" loading={saveMutation.isPending} onClick={saveMenu}>
                            保存菜单
                        </Button>
                    </div>
                }
            >
                <Form<MenuFormValues> form={editForm} layout="vertical" className="menu-editor-form">
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item name="parentId" label="上级菜单">
                        <Select allowClear placeholder="不选择则作为根菜单" options={parentOptions} />
                    </Form.Item>
                    <Form.Item
                        name="name"
                        label="菜单名称"
                        rules={[{ required: true, message: "请输入菜单名称" }]}
                    >
                        <Input placeholder="例如：菜单管理" />
                    </Form.Item>
                    <Form.Item name="url" label="URL">
                        <Input placeholder="例如：/system/menus" />
                    </Form.Item>
                    <Form.Item name="perms" label="权限标识">
                        <Input placeholder="例如：sys:menu:view" />
                    </Form.Item>
                    <Form.Item name="ranks" label="等级">
                        <InputNumber min={0} max={9} precision={0} className="menu-rank-input" />
                    </Form.Item>
                    <Form.Item name="display" label="显示状态">
                        <Select
                            options={[
                                { label: "显示", value: true },
                                { label: "隐藏", value: false }
                            ]}
                        />
                    </Form.Item>
                    <Form.Item name="displayParams" label="显示参数">
                        <TextArea rows={3} maxLength={1000} showCount placeholder='例如：{"icon":"menu"}' />
                    </Form.Item>
                    <Form.Item name="remarks" label="备注">
                        <TextArea rows={3} maxLength={200} showCount placeholder="菜单说明" />
                    </Form.Item>
                </Form>
            </SandwishDrawer>

            <SandwishConfirmModal
                title="删除菜单"
                open={Boolean(deletingMenu)}
                message={`确认删除 ${deletingMenu?.name || ""}？`}
                description="删除后需要重新新增。若该菜单下仍有关联子菜单，接口会按后端校验结果拦截。"
                okText="删除"
                confirmLoading={deleteMutation.isPending}
                cancelText="取消"
                onCancel={closeDeleteConfirm}
                onOk={deleteMenu}
            />
        </>
    );
};
