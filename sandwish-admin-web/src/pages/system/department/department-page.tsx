import {
    ApartmentOutlined,
    ArrowLeftOutlined,
    ArrowRightOutlined,
    BranchesOutlined,
    DeleteOutlined,
    EditOutlined,
    HolderOutlined,
    PlusOutlined,
    ReloadOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Space, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { useSandwishConfirm } from "@/components/sandwish-confirm-modal/hooks/use-sandwish-confirm";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import { DepartmentEdit } from "./components/department-edit";
import {
    addDepartment,
    removeDepartments,
    listDepartments,
    moveDepartment,
    changeDepartmentInfo
} from "./department-service";
import type { DepartmentMoveCommand, DepartmentSaveCommand } from "./department-service";
import type { DepartmentNode, DepartmentTableNode } from "./department-types";
import "./department-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 260,
    namePath: 320,
    remarks: 320,
    actions: 208
};

const buildDepartmentTree = (departments: DepartmentNode[]) => {
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

const toMoveType = (position: SandwishTableSortPosition): DepartmentMoveCommand["type"] => {
    return position === "before" ? "before" : "after";
};

export const DepartmentPage = () => {
    const { message: messageApi } = App.useApp();
    const confirm = useSandwishConfirm();
    const queryClient = useQueryClient();
    const [editingDepartment, setEditingDepartment] = useState<DepartmentTableNode | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const [expandedRowKeys, setExpandedRowKeys] = useState<Key[] | null>(null);
    const canEditDepartment = hasPermission("sys:department:edit");
    const departmentQuery = useQuery({
        queryKey: ["department", "list"],
        queryFn: () => listDepartments(),
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
        mutationFn: (values: DepartmentSaveCommand) =>
            values.id ? changeDepartmentInfo(values) : addDepartment(values),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingDepartment(null);
            await queryClient.invalidateQueries({ queryKey: ["department", "list"] });
            messageApi.success("部门已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: removeDepartments,
        onSuccess: async () => {
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

    const openCreateEditor = () => {
        setEditingDepartment(null);
        setEditorOpen(true);
    };

    const openEditEditor = (department: DepartmentTableNode) => {
        setEditingDepartment(department);
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingDepartment(null);
    };

    const saveDepartment = (request: DepartmentSaveCommand) => {
        saveMutation.mutate(request);
    };

    const openDeleteConfirm = (department: DepartmentTableNode) => {
        confirm.danger({
            title: "删除部门",
            message: `确认删除 ${department.name || ""}？`,
            description:
                "删除后需要重新新增。若该部门下仍有关联用户或子部门，接口会按后端校验结果拦截。",
            okText: "删除",
            onConfirm: () => deleteMutation.mutateAsync([department.id])
        });
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

    const readSiblingDepartments = (department: DepartmentTableNode) => {
        if (!department.parentId) {
            return departmentTree;
        }
        return flatDepartments.find((item) => item.id === department.parentId)?.children || [];
    };

    const readPreviousSiblingDepartment = (department: DepartmentTableNode) => {
        const siblings = readSiblingDepartments(department);
        const index = siblings.findIndex((item) => item.id === department.id);
        return index > 0 ? siblings[index - 1] : null;
    };

    const promoteDepartment = (department: DepartmentTableNode) => {
        if (!canEditDepartment || !department.parentId) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: department.id,
            toNodeId: department.parentId,
            type: "after"
        });
    };

    const demoteDepartment = (department: DepartmentTableNode) => {
        const previousSibling = readPreviousSiblingDepartment(department);
        if (!canEditDepartment || !previousSibling) {
            return;
        }
        moveMutation.mutate({
            fromNodeId: department.id,
            toNodeId: previousSibling.id,
            type: "insideLast"
        });
    };

    const columns: SandwishTableProps<DepartmentTableNode>["columns"] = [
        {
            title: "部门名称",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            ellipsis: true,
            render: (name: string, department) => (
                <Space size={8} className="department-name-cell">
                    <ApartmentOutlined className="department-name-icon" />
                    <span className="department-name-text" title={name}>
                        {name}
                    </span>
                    {department.shortName ? <Tag>{department.shortName}</Tag> : null}
                </Space>
            )
        },
        {
            title: "完整路径",
            dataIndex: "namePath",
            key: "namePath",
            width: DEFAULT_COLUMN_WIDTHS.namePath,
            ellipsis: true,
            render: (namePath?: string | null) =>
                namePath ? (
                    <span className="department-path-text" title={namePath}>
                        {namePath}
                    </span>
                ) : (
                    <Text type="secondary">根部门</Text>
                )
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
                            aria-label={`升级 ${department.name}`}
                            className="sandwish-table-row-action"
                            disabled={
                                !canEditDepartment || !department.parentId || moveMutation.isPending
                            }
                            icon={<ArrowLeftOutlined />}
                            type="text"
                            onClick={() => promoteDepartment(department)}
                        />
                        <Button
                            aria-label={`降级 ${department.name}`}
                            className="sandwish-table-row-action"
                            disabled={
                                !canEditDepartment ||
                                !readPreviousSiblingDepartment(department) ||
                                moveMutation.isPending
                            }
                            icon={<ArrowRightOutlined />}
                            type="text"
                            onClick={() => demoteDepartment(department)}
                        />
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
            <ListPage<DepartmentTableNode>
                pageClassName="department-page"
                title="部门管理"
                description="维护组织树、部门简称、排序和备注信息。"
                subjectName="部门"
                pageActions={
                    <>
                        <Button icon={<ReloadOutlined />} onClick={() => departmentQuery.refetch()}>
                            刷新
                        </Button>
                        {canEditDepartment ? (
                            <Button
                                type="primary"
                                icon={<PlusOutlined />}
                                onClick={openCreateEditor}
                            >
                                新增部门
                            </Button>
                        ) : null}
                    </>
                }
                rowKey="id"
                className="department-table"
                columns={columns}
                dataSource={departmentTree}
                loading={departmentQuery.isFetching || moveMutation.isPending}
                pagination={false}
                scroll={{ x: 1108 }}
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

            <DepartmentEdit
                open={editorOpen}
                department={editingDepartment}
                parentOptions={parentOptions}
                saving={saveMutation.isPending}
                onClose={closeEditor}
                onSave={saveDepartment}
            />
        </>
    );
};
