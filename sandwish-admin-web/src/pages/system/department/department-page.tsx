import {
    ApartmentOutlined,
    BranchesOutlined,
    DeleteOutlined,
    EditOutlined,
    HolderOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Button, Form, Input, Modal, Select, Space, Tag, Typography, message } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import {
    addDepartment,
    deleteDepartments,
    listDepartments,
    moveDepartment,
    updateDepartment
} from "./department-service";
import type {
    DepartmentListRequest,
    DepartmentMoveRequest,
    DepartmentResponse,
    DepartmentSaveRequest
} from "./department-service";
import "./department-page.css";

const { Text } = Typography;
const { TextArea } = Input;

const DEFAULT_COLUMN_WIDTHS = {
    name: 260,
    namePath: 320,
    remarks: 320,
    actions: 128
};

interface DepartmentTableNode extends DepartmentResponse {
    children?: DepartmentTableNode[];
}

interface DepartmentFilters {
    remarks: string;
}

interface DepartmentFormValues {
    id?: string | null;
    parentId?: string | null;
    name: string;
    shortName?: string | null;
    remarks?: string | null;
}

const DEFAULT_DEPARTMENT_FILTERS: DepartmentFilters = {
    remarks: ""
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const buildDepartmentTree = (departments: DepartmentResponse[]) => {
    const nodeMap = new Map<string, DepartmentTableNode>();
    const roots: DepartmentTableNode[] = [];

    departments.forEach((department) => {
        nodeMap.set(department.id, { ...department });
    });

    nodeMap.forEach((department) => {
        if (department.parentId) {
            const parent = nodeMap.get(department.parentId);
            if (parent) {
                parent.children = parent.children || [];
                parent.children.push(department);
                return;
            }
        }

        roots.push(department);
    });

    return roots;
};

const collectDepartmentIds = (departments: DepartmentTableNode[]): string[] => {
    return departments.flatMap((department) => [
        department.id,
        ...(department.children ? collectDepartmentIds(department.children) : [])
    ]);
};

const flattenDepartments = (departments: DepartmentTableNode[]): DepartmentTableNode[] => {
    return departments.flatMap((department) => [
        department,
        ...(department.children ? flattenDepartments(department.children) : [])
    ]);
};

const collectDescendantIds = (department?: DepartmentTableNode | null): Set<string> => {
    if (!department?.children?.length) {
        return new Set();
    }

    return new Set(collectDepartmentIds(department.children));
};

const readFormRequest = (values: DepartmentFormValues): DepartmentSaveRequest => {
    return {
        id: values.id,
        parentId: values.parentId || null,
        name: values.name.trim(),
        shortName: normalizeSearch(values.shortName),
        remarks: normalizeSearch(values.remarks)
    };
};

const toMoveType = (position: SandwishTableSortPosition): DepartmentMoveRequest["type"] => {
    return position === "before" ? "before" : "after";
};

export const DepartmentPage = () => {
    const [messageApi, contextHolder] = message.useMessage();
    const [editForm] = Form.useForm<DepartmentFormValues>();
    const queryClient = useQueryClient();
    const [query, setQuery] = useState<DepartmentListRequest>({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<DepartmentFilters>(DEFAULT_DEPARTMENT_FILTERS);
    const [editingDepartment, setEditingDepartment] = useState<DepartmentTableNode | null>(null);
    const [deletingDepartment, setDeletingDepartment] = useState<DepartmentTableNode | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const [expandedRowKeys, setExpandedRowKeys] = useState<Key[] | null>(null);
    const canEditDepartment = hasPermission("sys:department:edit");
    const hasActiveFilters = Boolean(filters.remarks.trim());
    const departmentQuery = useQuery({
        queryKey: ["department", "list", query],
        queryFn: () => listDepartments(query),
        retry: false
    });
    const departments = useMemo(() => departmentQuery.data || [], [departmentQuery.data]);
    const departmentTree = useMemo(() => buildDepartmentTree(departments), [departments]);
    const flatDepartments = useMemo(() => flattenDepartments(departmentTree), [departmentTree]);
    const expandedDepartmentIds = useMemo(
        () => collectDepartmentIds(departmentTree),
        [departmentTree]
    );
    const actualExpandedRowKeys = expandedRowKeys || expandedDepartmentIds;
    const unavailableParentIds = useMemo(() => {
        const descendantIds = collectDescendantIds(editingDepartment);
        if (editingDepartment?.id) {
            descendantIds.add(editingDepartment.id);
        }
        return descendantIds;
    }, [editingDepartment]);
    const parentOptions = useMemo(
        () =>
            flatDepartments
                .filter((department) => !unavailableParentIds.has(department.id))
                .map((department) => ({
                    label: department.namePath || department.name,
                    value: department.id
                })),
        [flatDepartments, unavailableParentIds]
    );

    const saveMutation = useMutation({
        mutationFn: (values: DepartmentSaveRequest) =>
            values.id ? updateDepartment(values) : addDepartment(values),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingDepartment(null);
            editForm.resetFields();
            await queryClient.invalidateQueries({ queryKey: ["department", "list"] });
            messageApi.success("部门已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: deleteDepartments,
        onSuccess: async () => {
            setDeletingDepartment(null);
            await queryClient.invalidateQueries({ queryKey: ["department", "list"] });
            messageApi.success("部门已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const moveMutation = useMutation({
        mutationFn: moveDepartment,
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["department", "list"] });
            messageApi.success("部门顺序已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "移动失败");
        }
    });

    const updateQuery = (values: Partial<DepartmentListRequest>) => {
        setQuery((currentQuery) => ({
            name: currentQuery.name,
            remarks: currentQuery.remarks,
            ...values
        }));
    };

    const searchDepartments = (value: string) => {
        setSearchText(value);
        updateQuery({ name: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            remarks: normalizeSearch(filters.remarks)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_DEPARTMENT_FILTERS);
        updateQuery({
            remarks: undefined
        });
    };

    const openCreateEditor = () => {
        setEditingDepartment(null);
        editForm.resetFields();
        setEditorOpen(true);
    };

    const openEditEditor = (department: DepartmentTableNode) => {
        setEditingDepartment(department);
        editForm.setFieldsValue({
            id: department.id,
            parentId: department.parentId || null,
            name: department.name,
            shortName: department.shortName,
            remarks: department.remarks
        });
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingDepartment(null);
        editForm.resetFields();
    };

    const saveDepartment = async () => {
        const values = await editForm.validateFields();
        saveMutation.mutate(readFormRequest(values));
    };

    const openDeleteConfirm = (department: DepartmentTableNode) => {
        setDeletingDepartment(department);
    };

    const closeDeleteConfirm = () => {
        if (deleteMutation.isPending) {
            return;
        }
        setDeletingDepartment(null);
    };

    const deleteDepartment = () => {
        if (!deletingDepartment) {
            return;
        }
        deleteMutation.mutate([deletingDepartment.id]);
    };

    const sortDepartment = (
        sourceDepartment: DepartmentTableNode,
        targetDepartment: DepartmentTableNode,
        position: SandwishTableSortPosition
    ) => {
        if (!canEditDepartment || sourceDepartment.id === targetDepartment.id) {
            return;
        }

        moveMutation.mutate({
            fromNodeId: sourceDepartment.id,
            toNodeId: targetDepartment.id,
            type: toMoveType(position)
        });
    };

    const columns: SandwishTableProps<DepartmentTableNode>["columns"] = [
        {
            title: "部门名称",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (name: string, department) => (
                <Space size={8}>
                    <ApartmentOutlined className="department-name-icon" />
                    <span>{name}</span>
                    {department.shortName ? <Tag>{department.shortName}</Tag> : null}
                </Space>
            )
        },
        {
            title: "完整路径",
            dataIndex: "namePath",
            key: "namePath",
            width: DEFAULT_COLUMN_WIDTHS.namePath,
            render: (namePath?: string | null) => namePath || <Text type="secondary">根部门</Text>
        },
        {
            title: "备注",
            dataIndex: "remarks",
            key: "remarks",
            width: DEFAULT_COLUMN_WIDTHS.remarks,
            ellipsis: true,
            render: (remarks?: string | null) => remarks || <Text type="secondary">未填写</Text>
        },
        {
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, department) => (
                <div className="sandwish-table-row-actions">
                    <Space.Compact className="sandwish-table-row-actions-inline">
                        <Button
                            aria-label={`编辑 ${department.name}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditDepartment}
                            icon={<EditOutlined />}
                            type="text"
                            onClick={() => openEditEditor(department)}
                        />
                        <Button
                            aria-label={`删除 ${department.name}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditDepartment}
                            icon={<DeleteOutlined />}
                            type="text"
                            danger
                            onClick={() => openDeleteConfirm(department)}
                        />
                        <Button
                            aria-label={`拖动 ${department.name}`}
                            className="sandwish-table-row-action department-drag-action"
                            disabled={!canEditDepartment || moveMutation.isPending}
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
            <ListPage<DepartmentTableNode>
                pageClassName="department-page"
                title="部门管理"
                description="维护组织树、部门简称、排序和备注信息。"
                subjectName="部门"
                enableAdd={canEditDepartment}
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={searchDepartments}
                onAdd={openCreateEditor}
                filterActive={hasActiveFilters}
                filter={({ closeFilter }) => (
                    <div className="department-filter-form">
                        <label>
                            <span>备注</span>
                            <Input
                                allowClear
                                placeholder="备注关键词"
                                value={filters.remarks}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        remarks: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <Button onClick={resetFilters} disabled={!hasActiveFilters}>
                            重置
                        </Button>
                        <Button
                            className="department-filter-search"
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
                    <Button icon={<ReloadOutlined />} onClick={() => departmentQuery.refetch()}>
                        刷新
                    </Button>
                }
                rowKey="id"
                className="department-table"
                columns={columns}
                dataSource={departmentTree}
                loading={departmentQuery.isFetching || moveMutation.isPending}
                pagination={false}
                scroll={{ x: 1028 }}
                expandable={{
                    defaultExpandAllRows: true,
                    expandedRowKeys: actualExpandedRowKeys,
                    indentSize: 24,
                    expandIconColumnIndex: 0,
                    onExpandedRowsChange: (keys) => setExpandedRowKeys([...keys])
                }}
                locale={{
                    emptyText: departmentQuery.isError ? (
                        "部门列表加载失败，请确认权限和接口状态。"
                    ) : (
                        <Space orientation="vertical" size={8}>
                            <BranchesOutlined className="department-empty-icon" />
                            <Text type="secondary">暂无部门数据</Text>
                        </Space>
                    )
                }}
                onSort={sortDepartment}
                sortable={canEditDepartment}
            />

            <SandwishDrawer
                className="department-edit-drawer"
                title={editingDepartment ? "编辑部门" : "新增部门"}
                open={editorOpen}
                size="small"
                onClose={closeEditor}
                footer={
                    <div className="department-edit-footer">
                        <Button onClick={closeEditor}>取消</Button>
                        <Button
                            type="primary"
                            loading={saveMutation.isPending}
                            onClick={saveDepartment}
                        >
                            保存部门
                        </Button>
                    </div>
                }
            >
                <Form<DepartmentFormValues>
                    form={editForm}
                    layout="vertical"
                    className="department-editor-form"
                >
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item name="parentId" label="上级部门">
                        <Select
                            allowClear
                            placeholder="不选择则作为根部门"
                            options={parentOptions}
                        />
                    </Form.Item>
                    <Form.Item
                        name="name"
                        label="部门名称"
                        rules={[{ required: true, message: "请输入部门名称" }]}
                    >
                        <Input placeholder="例如：研发中心" />
                    </Form.Item>
                    <Form.Item name="shortName" label="简称">
                        <Input placeholder="例如：R&D" />
                    </Form.Item>
                    <Form.Item name="remarks" label="备注">
                        <TextArea rows={4} maxLength={200} showCount placeholder="部门职责说明" />
                    </Form.Item>
                </Form>
            </SandwishDrawer>

            <Modal
                className="department-delete-modal"
                rootClassName="department-delete-modal-root"
                title="删除部门"
                open={Boolean(deletingDepartment)}
                okText="删除"
                okButtonProps={{ danger: true }}
                confirmLoading={deleteMutation.isPending}
                cancelText="取消"
                onCancel={closeDeleteConfirm}
                onOk={deleteDepartment}
            >
                <div className="department-delete-content">
                    <DeleteOutlined className="department-delete-icon" />
                    <div>
                        <Text strong>确认删除 {deletingDepartment?.name}？</Text>
                        <Text type="secondary">
                            删除后需要重新新增。若该部门下仍有关联用户或子部门，接口会按后端校验结果拦截。
                        </Text>
                    </div>
                </div>
            </Modal>
        </>
    );
};
