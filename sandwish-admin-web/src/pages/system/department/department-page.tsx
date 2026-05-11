import {
    ApartmentOutlined,
    BranchesOutlined,
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Alert, Button, Card, Form, Input, Space, Table, Tag, Typography } from "antd";
import type { TableProps } from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { listDepartments } from "./department-service";
import type { DepartmentListRequest, DepartmentResponse } from "./department-service";

const { Text, Title } = Typography;

interface DepartmentTableNode extends DepartmentResponse {
    children?: DepartmentTableNode[];
}

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readDepartmentQuery = (values: DepartmentListRequest): DepartmentListRequest => {
    return {
        name: normalizeSearch(values.name),
        remarks: normalizeSearch(values.remarks)
    };
};

const buildDepartmentTree = (departments: DepartmentResponse[]) => {
    const nodeMap = new Map<number, DepartmentTableNode>();
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

const countLeafDepartments = (departments: DepartmentTableNode[]): number => {
    return departments.reduce((count, department) => {
        if (!department.children?.length) {
            return count + 1;
        }

        return count + countLeafDepartments(department.children);
    }, 0);
};

const collectDepartmentIds = (departments: DepartmentTableNode[]): number[] => {
    return departments.flatMap((department) => [
        department.id,
        ...(department.children ? collectDepartmentIds(department.children) : [])
    ]);
};

const columns: TableProps<DepartmentTableNode>["columns"] = [
    {
        title: "部门名称",
        dataIndex: "name",
        key: "name",
        width: 260,
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
        render: (namePath?: string | null) => namePath || <Text type="secondary">根部门</Text>
    },
    {
        title: "备注",
        dataIndex: "remarks",
        key: "remarks",
        ellipsis: true,
        render: (remarks?: string | null) => remarks || <Text type="secondary">未填写</Text>
    }
];

export const DepartmentPage = () => {
    const [form] = Form.useForm<DepartmentListRequest>();
    const [query, setQuery] = useState<DepartmentListRequest>({});
    const canEditDepartment = hasPermission("sys:department:edit");
    const departmentQuery = useQuery({
        queryKey: ["department", "list", query],
        queryFn: () => listDepartments(query),
        retry: false
    });
    const departments = useMemo(() => departmentQuery.data || [], [departmentQuery.data]);
    const departmentTree = useMemo(() => buildDepartmentTree(departments), [departments]);
    const expandedDepartmentIds = useMemo(
        () => collectDepartmentIds(departmentTree),
        [departmentTree]
    );
    const rootCount = departmentTree.length;
    const leafCount = useMemo(() => countLeafDepartments(departmentTree), [departmentTree]);

    const searchDepartments = (values: DepartmentListRequest) => {
        setQuery(readDepartmentQuery(values));
    };

    const resetSearch = () => {
        form.resetFields();
        setQuery({});
    };

    return (
        <main className="department-page">
            <section className="department-page-header">
                <div>
                    <Text className="eyebrow">system / department</Text>
                    <Title level={2}>部门管理</Title>
                    <Text type="secondary">维护组织树、部门简称、排序和备注信息。</Text>
                </div>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => departmentQuery.refetch()}>
                        刷新
                    </Button>
                    <Button type="primary" icon={<PlusOutlined />} disabled={!canEditDepartment}>
                        新增部门
                    </Button>
                </Space>
            </section>

            <section className="department-summary" aria-label="部门概览">
                <Card className="department-summary-card">
                    <Text type="secondary">部门总数</Text>
                    <strong>{departments.length}</strong>
                </Card>
                <Card className="department-summary-card">
                    <Text type="secondary">根部门</Text>
                    <strong>{rootCount}</strong>
                </Card>
                <Card className="department-summary-card">
                    <Text type="secondary">末级部门</Text>
                    <strong>{leafCount}</strong>
                </Card>
            </section>

            <Card className="department-list-panel">
                <div className="department-list-toolbar">
                    <div>
                        <Text className="eyebrow">department list</Text>
                        <Title level={3}>组织结构</Title>
                    </div>
                    <Form<DepartmentListRequest>
                        form={form}
                        className="department-search-form"
                        layout="inline"
                        onFinish={searchDepartments}
                    >
                        <Form.Item name="name">
                            <Input
                                allowClear
                                placeholder="部门名称 / 简称"
                                prefix={<SearchOutlined />}
                            />
                        </Form.Item>
                        <Form.Item name="remarks">
                            <Input allowClear placeholder="备注关键词" />
                        </Form.Item>
                        <Form.Item>
                            <Space>
                                <Button htmlType="submit" type="primary">
                                    查询
                                </Button>
                                <Button onClick={resetSearch}>重置</Button>
                            </Space>
                        </Form.Item>
                    </Form>
                </div>

                {departmentQuery.isError ? (
                    <Alert
                        type="warning"
                        showIcon
                        message="部门列表加载失败"
                        description="请确认当前账号拥有 sys:department:view 权限，并检查 admin-api 部门列表接口。"
                        action={<Button onClick={() => departmentQuery.refetch()}>重试</Button>}
                        style={{ marginBottom: 16 }}
                    />
                ) : null}

                <Table<DepartmentTableNode>
                    rowKey="id"
                    columns={columns}
                    dataSource={departmentTree}
                    loading={departmentQuery.isFetching}
                    pagination={false}
                    scroll={{ x: 920 }}
                    expandable={{
                        defaultExpandAllRows: true,
                        expandedRowKeys: expandedDepartmentIds,
                        indentSize: 24,
                        expandIconColumnIndex: 0
                    }}
                    locale={{
                        emptyText: (
                            <Space orientation="vertical" size={8}>
                                <BranchesOutlined className="department-empty-icon" />
                                <Text type="secondary">暂无部门数据</Text>
                            </Space>
                        )
                    }}
                />
            </Card>
        </main>
    );
};
