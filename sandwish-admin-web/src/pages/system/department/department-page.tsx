import {
    ApartmentOutlined,
    BranchesOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Button, Input, Space, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import { ListPage } from "@/components/list-page";
import type { SandwishTableProps } from "@/components/sandwish-table";
import { listDepartments } from "./department-service";
import type { DepartmentListRequest, DepartmentResponse } from "./department-service";
import "./department-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 260,
    namePath: 320,
    remarks: 320
};

interface DepartmentTableNode extends DepartmentResponse {
    children?: DepartmentTableNode[];
}

interface DepartmentFilters {
    remarks: string;
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
    }
];

export const DepartmentPage = () => {
    const [query, setQuery] = useState<DepartmentListRequest>({});
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<DepartmentFilters>(DEFAULT_DEPARTMENT_FILTERS);
    const hasActiveFilters = Boolean(filters.remarks.trim());
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

    return (
        <ListPage<DepartmentTableNode>
            pageClassName="department-page"
            title="部门管理"
            description="维护组织树、部门简称、排序和备注信息。"
            subjectName="部门"
            enableFilter
            enableSearch
            searchShortcut="⌘K"
            searchValue={searchText}
            onSearchChange={searchDepartments}
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
            loading={departmentQuery.isFetching}
            pagination={false}
            scroll={{ x: 900 }}
            expandable={{
                defaultExpandAllRows: true,
                expandedRowKeys: expandedDepartmentIds,
                indentSize: 24,
                expandIconColumnIndex: 0
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
        />
    );
};
