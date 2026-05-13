import {
    ApartmentOutlined,
    CameraOutlined,
    DeleteOutlined,
    EditOutlined,
    ExclamationCircleOutlined,
    HolderOutlined,
    MoreOutlined,
    PoweroffOutlined,
    SearchOutlined
} from "@ant-design/icons";
import {
    Avatar,
    Button,
    Dropdown,
    Input,
    Modal,
    Select,
    Space,
    Tag,
    Tree,
    Typography
} from "antd";
import type { DataNode } from "antd/es/tree";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { ListPage } from "@/components/list-page";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import "./user-page.css";

const { Text } = Typography;

const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    email: 220,
    role: 120,
    status: 130,
    lastLogin: 160,
    actions: 116
};

interface UserRecord {
    id: string;
    name: string;
    email: string;
    role: "Admin" | "Editor" | "Viewer";
    status: "Active" | "Inactive" | "Invited";
    lastLogin: string;
    avatarColor: string;
    departmentId: string;
}

interface DepartmentRecord {
    id: string;
    parentId?: string | null;
    name: string;
    shortName: string;
}

type UserFilterRole = "All" | UserRecord["role"];
type UserFilterStatus = "All" | UserRecord["status"];

interface UserFilters {
    email: string;
    role: UserFilterRole;
    status: UserFilterStatus;
}

const DEFAULT_USER_FILTERS: UserFilters = {
    email: "",
    role: "All",
    status: "All"
};

const USER_RECORDS: UserRecord[] = [
    {
        id: "1",
        name: "Ethan Chen",
        email: "ethan@acme.com",
        role: "Admin",
        status: "Active",
        lastLogin: "2 分钟前",
        avatarColor: "#0f766e",
        departmentId: "platform"
    },
    {
        id: "2",
        name: "Sophia Carter",
        email: "sophia@acme.com",
        role: "Editor",
        status: "Active",
        lastLogin: "1 小时前",
        avatarColor: "#c2410c",
        departmentId: "product-planning"
    },
    {
        id: "3",
        name: "Liam Johnson",
        email: "liam@acme.com",
        role: "Viewer",
        status: "Active",
        lastLogin: "3 小时前",
        avatarColor: "#1d4ed8",
        departmentId: "backend"
    },
    {
        id: "4",
        name: "Olivia Martinez",
        email: "olivia@acme.com",
        role: "Editor",
        status: "Inactive",
        lastLogin: "2 天前",
        avatarColor: "#be185d",
        departmentId: "marketing"
    },
    {
        id: "5",
        name: "Noah Williams",
        email: "noah@acme.com",
        role: "Viewer",
        status: "Invited",
        lastLogin: "从未登录",
        avatarColor: "#0369a1",
        departmentId: "customer-success"
    },
    {
        id: "6",
        name: "Ava Brown",
        email: "ava@acme.com",
        role: "Admin",
        status: "Active",
        lastLogin: "5 分钟前",
        avatarColor: "#7c3aed",
        departmentId: "frontend"
    },
    {
        id: "7",
        name: "James Davis",
        email: "james@acme.com",
        role: "Editor",
        status: "Active",
        lastLogin: "1 天前",
        avatarColor: "#b45309",
        departmentId: "qa"
    }
];

const DEPARTMENT_RECORDS: DepartmentRecord[] = [
    { id: "all", parentId: null, name: "全部部门", shortName: "全部" },
    { id: "rd", parentId: "all", name: "研发中心", shortName: "研发" },
    { id: "platform", parentId: "rd", name: "平台架构部", shortName: "平台" },
    { id: "backend", parentId: "rd", name: "后端研发部", shortName: "后端" },
    { id: "frontend", parentId: "rd", name: "前端体验部", shortName: "前端" },
    { id: "qa", parentId: "rd", name: "测试质量部", shortName: "测试" },
    { id: "product", parentId: "all", name: "产品中心", shortName: "产品" },
    { id: "product-planning", parentId: "product", name: "产品规划部", shortName: "规划" },
    { id: "design", parentId: "product", name: "交互设计部", shortName: "设计" },
    { id: "business", parentId: "all", name: "商业化中心", shortName: "商业" },
    { id: "marketing", parentId: "business", name: "市场运营部", shortName: "市场" },
    { id: "customer-success", parentId: "business", name: "客户成功部", shortName: "客户" }
];

const roleClassName: Record<UserRecord["role"], string> = {
    Admin: "user-role-admin",
    Editor: "user-role-editor",
    Viewer: "user-role-viewer"
};

const roleLabel: Record<UserRecord["role"], string> = {
    Admin: "管理员",
    Editor: "编辑者",
    Viewer: "只读用户"
};

const statusClassName: Record<UserRecord["status"], string> = {
    Active: "user-status-active",
    Inactive: "user-status-inactive",
    Invited: "user-status-invited"
};

const statusLabel: Record<UserRecord["status"], string> = {
    Active: "启用",
    Inactive: "禁用",
    Invited: "已邀请"
};

const getInitials = (name: string) => {
    return name
        .split(" ")
        .map((part) => part[0])
        .join("")
        .slice(0, 2);
};

const collectDepartmentIds = (departmentId: string): string[] => {
    const children = DEPARTMENT_RECORDS.filter((department) => department.parentId === departmentId);
    return [
        departmentId,
        ...children.flatMap((department) => collectDepartmentIds(department.id))
    ];
};

const countDepartmentUsers = (departmentId: string, users: UserRecord[]) => {
    const departmentIds = new Set(collectDepartmentIds(departmentId));
    return users.filter((user) => departmentIds.has(user.departmentId)).length;
};

const buildDepartmentTree = (users: UserRecord[]): DataNode[] => {
    const childrenByParentId = new Map<string | null | undefined, DepartmentRecord[]>();
    DEPARTMENT_RECORDS.forEach((department) => {
        const children = childrenByParentId.get(department.parentId) || [];
        children.push(department);
        childrenByParentId.set(department.parentId, children);
    });

    const toNode = (department: DepartmentRecord): DataNode => ({
        key: department.id,
        title: (
            <span className="user-department-node">
                <span>{department.name}</span>
                <Text type="secondary">{countDepartmentUsers(department.id, users)}</Text>
            </span>
        ),
        children: childrenByParentId.get(department.id)?.map(toNode)
    });

    return (childrenByParentId.get(null) || []).map(toNode);
};

export const UserPage = () => {
    const [users, setUsers] = useState<UserRecord[]>(USER_RECORDS);
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<UserFilters>(DEFAULT_USER_FILTERS);
    const [selectedDepartmentId, setSelectedDepartmentId] = useState("all");
    const [editingUser, setEditingUser] = useState<UserRecord | null>(null);
    const [deletingUser, setDeletingUser] = useState<UserRecord | null>(null);
    const [deleteConfirmText, setDeleteConfirmText] = useState("delete");
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const hasSelectedUsers = selectedRowKeys.length > 0;
    const hasActiveFilters =
        Boolean(filters.email.trim()) || filters.role !== "All" || filters.status !== "All";

    const filteredUsers = useMemo(() => {
        const keyword = searchText.trim().toLowerCase();
        const emailKeyword = filters.email.trim().toLowerCase();
        const selectedDepartmentIds = new Set(collectDepartmentIds(selectedDepartmentId));
        return users.filter((user) => {
            const isDepartmentMatched =
                selectedDepartmentId === "all" || selectedDepartmentIds.has(user.departmentId);
            const isEmailMatched = !emailKeyword || user.email.toLowerCase().includes(emailKeyword);
            const isRoleMatched = filters.role === "All" || user.role === filters.role;
            const isStatusMatched = filters.status === "All" || user.status === filters.status;
            const isKeywordMatched =
                !keyword ||
                user.name.toLowerCase().includes(keyword) ||
                user.email.toLowerCase().includes(keyword) ||
                user.role.toLowerCase().includes(keyword);

            return isDepartmentMatched && isEmailMatched && isRoleMatched && isStatusMatched && isKeywordMatched;
        });
    }, [filters, searchText, selectedDepartmentId, users]);
    const departmentTreeData = useMemo(() => buildDepartmentTree(users), [users]);

    const moveUser = (
        sourceUser: UserRecord,
        targetUser: UserRecord,
        position: SandwishTableSortPosition
    ) => {
        if (sourceUser.id === targetUser.id) {
            return;
        }

        setUsers((currentUsers) => {
            const sourceIndex = currentUsers.findIndex((user) => user.id === sourceUser.id);
            const targetIndex = currentUsers.findIndex((user) => user.id === targetUser.id);
            if (sourceIndex < 0 || targetIndex < 0) {
                return currentUsers;
            }

            const nextUsers = [...currentUsers];
            const [movedUser] = nextUsers.splice(sourceIndex, 1);
            const nextTargetIndex = nextUsers.findIndex((user) => user.id === targetUser.id);
            nextUsers.splice(position === "before" ? nextTargetIndex : nextTargetIndex + 1, 0, movedUser);
            return nextUsers;
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_USER_FILTERS);
    };

    const columns: SandwishTableProps<UserRecord>["columns"] = [
        {
            title: "用户",
            dataIndex: "name",
            key: "name",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (name: string, user) => (
                <Space size={10}>
                    <Avatar style={{ backgroundColor: user.avatarColor }}>
                        {getInitials(name)}
                    </Avatar>
                    <Text strong>{name}</Text>
                </Space>
            )
        },
        {
            title: "邮箱",
            dataIndex: "email",
            key: "email",
            width: DEFAULT_COLUMN_WIDTHS.email
        },
        {
            title: "角色",
            dataIndex: "role",
            key: "role",
            width: DEFAULT_COLUMN_WIDTHS.role,
            render: (role: UserRecord["role"]) => <Tag className={roleClassName[role]}>{roleLabel[role]}</Tag>
        },
        {
            title: "状态",
            dataIndex: "status",
            key: "status",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (status: UserRecord["status"]) => (
                <Tag className={statusClassName[status]}>{statusLabel[status]}</Tag>
            )
        },
        {
            title: "最近登录",
            dataIndex: "lastLogin",
            key: "lastLogin",
            width: DEFAULT_COLUMN_WIDTHS.lastLogin
        },
        {
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, user) => (
                <div className="sandwish-table-row-actions">
                    <Space.Compact className="sandwish-table-row-actions-inline">
                        <Button
                            aria-label={`编辑 ${user.name}`}
                            className="sandwish-table-row-action"
                            icon={<EditOutlined />}
                            type="text"
                            onClick={() => setEditingUser(user)}
                        />
                        <Button
                            aria-label={`删除 ${user.name}`}
                            className="sandwish-table-row-action"
                            icon={<DeleteOutlined />}
                            type="text"
                            danger
                            onClick={() => {
                                setDeletingUser(user);
                                setDeleteConfirmText("delete");
                            }}
                        />
                    </Space.Compact>
                    <button
                        aria-label={`拖动排序 ${user.name}`}
                        className="sandwish-table-row-action sandwish-table-row-drag-handle"
                        type="button"
                    >
                        <HolderOutlined />
                    </button>
                    <Dropdown
                        menu={{
                            items: [
                                {
                                    key: "edit",
                                    icon: <EditOutlined />,
                                    label: "编辑"
                                },
                                {
                                    key: "delete",
                                    danger: true,
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
                                    setDeleteConfirmText("delete");
                                }
                            }
                        }}
                        trigger={["click"]}
                    >
                        <Button
                            aria-label={`展开 ${user.name} 操作`}
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
            <ListPage<UserRecord>
                pageClassName="user-page"
                title="用户管理"
                description="管理后台用户、角色与权限状态。"
                subjectName="用户"
                enableAdd
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={setSearchText}
                filterActive={hasActiveFilters}
                filterClassName="user-filter-panel"
                filter={({ closeFilter }) => (
                    <div className="user-filter-form">
                        <label>
                            <span>邮箱</span>
                            <Input
                                allowClear
                                placeholder="name@company.com"
                                value={filters.email}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        email: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>角色</span>
                            <Select<UserFilterRole>
                                value={filters.role}
                                options={["All", "Admin", "Editor", "Viewer"].map((value) => ({
                                    value: value as UserFilterRole,
                                    label: value === "All" ? "全部" : roleLabel[value as UserRecord["role"]]
                                }))}
                                onChange={(role) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        role
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>状态</span>
                            <Select<UserFilterStatus>
                                value={filters.status}
                                options={["All", "Active", "Inactive", "Invited"].map((value) => ({
                                    value: value as UserFilterStatus,
                                    label: value === "All" ? "全部" : statusLabel[value as UserRecord["status"]]
                                }))}
                                onChange={(status) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        status
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
                            onClick={closeFilter}
                        >
                            查询
                        </Button>
                    </div>
                )}
                batchClassName="user-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!hasSelectedUsers}
                        >
                            批量删除
                        </Button>
                        <Button
                            className="user-batch-neutral"
                            icon={<PoweroffOutlined />}
                            disabled={!hasSelectedUsers}
                        >
                            禁用
                        </Button>
                        <Button
                            className="user-batch-enable"
                            icon={<PoweroffOutlined />}
                            disabled={!hasSelectedUsers}
                        >
                            启用
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="user-table"
                columns={columns}
                dataSource={filteredUsers}
                onSort={moveUser}
                pagination={{
                    current: 1,
                    pageSize: 50,
                    total: 1248,
                    showSizeChanger: false,
                    showTotal: () => "1,248 个用户"
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys
                }}
                tableAsidePlacement="left"
                tableAside={
                    <div className="user-department-panel">
                        <div className="user-department-panel-head">
                            <Space size={8}>
                                <ApartmentOutlined />
                                <Text strong>部门</Text>
                            </Space>
                            <Text type="secondary">{filteredUsers.length} 人</Text>
                        </div>
                        <Tree
                            blockNode
                            defaultExpandAll
                            selectedKeys={[selectedDepartmentId]}
                            treeData={departmentTreeData}
                            onSelect={(keys) => {
                                setSelectedDepartmentId(String(keys[0] || "all"));
                                setSelectedRowKeys([]);
                            }}
                        />
                    </div>
                }
                sortable
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
                            <Avatar size={64} style={{ backgroundColor: editingUser.avatarColor }}>
                                {getInitials(editingUser.name)}
                            </Avatar>
                            <Button size="small" shape="circle" icon={<CameraOutlined />} />
                        </div>
                        <label>
                            <span>姓名</span>
                            <Input value={editingUser.name} readOnly />
                        </label>
                        <label>
                            <span>邮箱</span>
                            <Input value={editingUser.email} readOnly />
                        </label>
                        <label>
                            <span>角色</span>
                            <Select
                                value={editingUser.role}
                                options={["Admin", "Editor", "Viewer"].map((value) => ({
                                    value,
                                    label: roleLabel[value as UserRecord["role"]]
                                }))}
                            />
                        </label>
                        <label>
                            <span>状态</span>
                            <Select
                                value={editingUser.status}
                                options={["Active", "Inactive", "Invited"].map((value) => ({
                                    value,
                                    label: statusLabel[value as UserRecord["status"]]
                                }))}
                            />
                        </label>
                        <label>
                            <span>组织</span>
                            <Select
                                value="Acme Corporation"
                                options={[{ value: "Acme Corporation" }]}
                            />
                        </label>
                        <label>
                            <span>项目</span>
                            <Select
                                mode="multiple"
                                value={["AI Platform", "Data Infrastructure"]}
                                options={["AI Platform", "Data Infrastructure", "Security"].map(
                                    (value) => ({ value })
                                )}
                            />
                        </label>
                    </div>
                ) : null}
            </SandwishDrawer>

            <Modal
                className="user-delete-modal"
                title="删除用户"
                open={Boolean(deletingUser)}
                centered
                width={360}
                okText="删除用户"
                cancelText="取消"
                okButtonProps={{ danger: true, disabled: deleteConfirmText !== "delete" }}
                onCancel={() => setDeletingUser(null)}
                onOk={() => setDeletingUser(null)}
            >
                {deletingUser ? (
                    <div className="user-delete-content">
                        <ExclamationCircleOutlined className="user-delete-warning" />
                        <strong>确认删除这个用户？</strong>
                        <Text type="secondary">
                            此操作不可撤销，相关用户数据将被永久移除。
                        </Text>
                        <div className="user-delete-person">
                            <Avatar style={{ backgroundColor: deletingUser.avatarColor }}>
                                {getInitials(deletingUser.name)}
                            </Avatar>
                            <div>
                                <Text strong>{deletingUser.name}</Text>
                                <Text type="secondary">{deletingUser.email}</Text>
                            </div>
                        </div>
                        <label>
                            <span>输入 delete 确认删除</span>
                            <Input
                                value={deleteConfirmText}
                                onChange={(event) => setDeleteConfirmText(event.target.value)}
                            />
                        </label>
                    </div>
                ) : null}
            </Modal>
        </>
    );
};
