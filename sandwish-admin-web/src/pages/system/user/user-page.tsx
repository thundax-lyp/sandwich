import {
    CameraOutlined,
    DeleteOutlined,
    EditOutlined,
    ExclamationCircleOutlined,
    FilterOutlined,
    MoreOutlined,
    PlusOutlined,
    PoweroffOutlined,
    SearchOutlined
} from "@ant-design/icons";
import {
    Avatar,
    Button,
    Drawer,
    Input,
    Modal,
    Select,
    Space,
    Table,
    Tag,
    Typography
} from "antd";
import type { TableProps } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import type { MouseEvent as ReactMouseEvent } from "react";

const { Text, Title } = Typography;

const MIN_COLUMN_WIDTH = 96;
const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    email: 220,
    role: 120,
    status: 130,
    lastLogin: 160,
    actions: 140
};

type UserColumnKey = keyof typeof DEFAULT_COLUMN_WIDTHS;

interface UserRecord {
    id: string;
    name: string;
    email: string;
    role: "Admin" | "Editor" | "Viewer";
    status: "Active" | "Inactive" | "Invited";
    lastLogin: string;
    avatarColor: string;
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
        lastLogin: "2 minutes ago",
        avatarColor: "#0f766e"
    },
    {
        id: "2",
        name: "Sophia Carter",
        email: "sophia@acme.com",
        role: "Editor",
        status: "Active",
        lastLogin: "1 hour ago",
        avatarColor: "#c2410c"
    },
    {
        id: "3",
        name: "Liam Johnson",
        email: "liam@acme.com",
        role: "Viewer",
        status: "Active",
        lastLogin: "3 hours ago",
        avatarColor: "#1d4ed8"
    },
    {
        id: "4",
        name: "Olivia Martinez",
        email: "olivia@acme.com",
        role: "Editor",
        status: "Inactive",
        lastLogin: "2 days ago",
        avatarColor: "#be185d"
    },
    {
        id: "5",
        name: "Noah Williams",
        email: "noah@acme.com",
        role: "Viewer",
        status: "Invited",
        lastLogin: "Never",
        avatarColor: "#0369a1"
    },
    {
        id: "6",
        name: "Ava Brown",
        email: "ava@acme.com",
        role: "Admin",
        status: "Active",
        lastLogin: "5 minutes ago",
        avatarColor: "#7c3aed"
    },
    {
        id: "7",
        name: "James Davis",
        email: "james@acme.com",
        role: "Editor",
        status: "Active",
        lastLogin: "1 day ago",
        avatarColor: "#b45309"
    }
];

const roleClassName: Record<UserRecord["role"], string> = {
    Admin: "user-role-admin",
    Editor: "user-role-editor",
    Viewer: "user-role-viewer"
};

const statusClassName: Record<UserRecord["status"], string> = {
    Active: "user-status-active",
    Inactive: "user-status-inactive",
    Invited: "user-status-invited"
};

const getInitials = (name: string) => {
    return name
        .split(" ")
        .map((part) => part[0])
        .join("")
        .slice(0, 2);
};

export const UserPage = () => {
    const [searchText, setSearchText] = useState("");
    const [filtersOpen, setFiltersOpen] = useState(false);
    const [filters, setFilters] = useState<UserFilters>(DEFAULT_USER_FILTERS);
    const [editingUser, setEditingUser] = useState<UserRecord | null>(null);
    const [deletingUser, setDeletingUser] = useState<UserRecord | null>(null);
    const [deleteConfirmText, setDeleteConfirmText] = useState("delete");
    const [columnWidths, setColumnWidths] = useState(DEFAULT_COLUMN_WIDTHS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const hasSelectedUsers = selectedRowKeys.length > 0;
    const hasActiveFilters =
        Boolean(filters.email.trim()) || filters.role !== "All" || filters.status !== "All";

    const startResizeColumn = (columnKey: UserColumnKey) => (event: ReactMouseEvent) => {
        event.preventDefault();
        event.stopPropagation();

        const startX = event.clientX;
        const startWidth = columnWidths[columnKey];

        const resizeColumn = (moveEvent: MouseEvent) => {
            const nextWidth = Math.max(MIN_COLUMN_WIDTH, startWidth + moveEvent.clientX - startX);
            setColumnWidths((currentWidths) => ({
                ...currentWidths,
                [columnKey]: nextWidth
            }));
        };

        const stopResizeColumn = () => {
            document.removeEventListener("mousemove", resizeColumn);
            document.removeEventListener("mouseup", stopResizeColumn);
        };

        document.addEventListener("mousemove", resizeColumn);
        document.addEventListener("mouseup", stopResizeColumn);
    };

    const renderResizableTitle = (columnKey: UserColumnKey, title: string) => (
        <span className="user-column-title">
            {title}
            <span
                aria-hidden="true"
                className="user-column-resize-handle"
                onMouseDown={startResizeColumn(columnKey)}
            />
        </span>
    );

    const filteredUsers = useMemo(() => {
        const keyword = searchText.trim().toLowerCase();
        const emailKeyword = filters.email.trim().toLowerCase();
        return USER_RECORDS.filter((user) => {
            const isEmailMatched = !emailKeyword || user.email.toLowerCase().includes(emailKeyword);
            const isRoleMatched = filters.role === "All" || user.role === filters.role;
            const isStatusMatched = filters.status === "All" || user.status === filters.status;
            const isKeywordMatched =
                !keyword ||
                user.name.toLowerCase().includes(keyword) ||
                user.email.toLowerCase().includes(keyword) ||
                user.role.toLowerCase().includes(keyword);

            return isEmailMatched && isRoleMatched && isStatusMatched && isKeywordMatched;
        });
    }, [filters, searchText]);

    const resetFilters = () => {
        setFilters(DEFAULT_USER_FILTERS);
    };

    const columns: TableProps<UserRecord>["columns"] = [
        {
            title: renderResizableTitle("name", "User"),
            dataIndex: "name",
            key: "name",
            width: columnWidths.name,
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
            title: renderResizableTitle("email", "Email"),
            dataIndex: "email",
            key: "email",
            width: columnWidths.email
        },
        {
            title: renderResizableTitle("role", "Role"),
            dataIndex: "role",
            key: "role",
            width: columnWidths.role,
            render: (role: UserRecord["role"]) => <Tag className={roleClassName[role]}>{role}</Tag>
        },
        {
            title: renderResizableTitle("status", "Status"),
            dataIndex: "status",
            key: "status",
            width: columnWidths.status,
            render: (status: UserRecord["status"]) => (
                <Tag className={statusClassName[status]}>{status}</Tag>
            )
        },
        {
            title: renderResizableTitle("lastLogin", "Last Login"),
            dataIndex: "lastLogin",
            key: "lastLogin",
            width: columnWidths.lastLogin
        },
        {
            title: renderResizableTitle("actions", "Actions"),
            key: "actions",
            width: columnWidths.actions,
            fixed: "right",
            render: (_, user) => (
                <Space size={6}>
                    <Button
                        aria-label={`编辑 ${user.name}`}
                        icon={<EditOutlined />}
                        type="text"
                        onClick={() => setEditingUser(user)}
                    />
                    <Button
                        aria-label={`删除 ${user.name}`}
                        icon={<DeleteOutlined />}
                        type="text"
                        danger
                        onClick={() => {
                            setDeletingUser(user);
                            setDeleteConfirmText("delete");
                        }}
                    />
                    <Button aria-label={`更多 ${user.name}`} icon={<MoreOutlined />} type="text" />
                </Space>
            )
        }
    ];

    return (
        <main className="user-page">
            <section className="user-command-panel">
                <div className="user-page-header">
                    <div>
                        <Title level={2}>Users</Title>
                        <Text type="secondary">Manage your users and their permissions.</Text>
                    </div>
                    <Space className="user-page-actions">
                        <Input
                            allowClear
                            className="user-search"
                            placeholder="Search users..."
                            prefix={<SearchOutlined />}
                            suffix={<span className="user-search-shortcut">⌘K</span>}
                            value={searchText}
                            onChange={(event) => setSearchText(event.target.value)}
                        />
                        <Button
                            icon={<FilterOutlined />}
                            type={filtersOpen || hasActiveFilters ? "primary" : "default"}
                            aria-expanded={filtersOpen}
                            onClick={() => setFiltersOpen((open) => !open)}
                        >
                            Filters
                        </Button>
                        <Button type="primary" icon={<PlusOutlined />}>
                            Create User
                        </Button>
                    </Space>
                </div>

                <div className={`user-filter-panel${filtersOpen ? " user-filter-panel-open" : ""}`}>
                    <div className="user-filter-form">
                        <label>
                            <span>Email</span>
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
                            <span>Role</span>
                            <Select<UserFilterRole>
                                value={filters.role}
                                options={["All", "Admin", "Editor", "Viewer"].map((value) => ({
                                    value: value as UserFilterRole,
                                    label: value
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
                            <span>Status</span>
                            <Select<UserFilterStatus>
                                value={filters.status}
                                options={["All", "Active", "Inactive", "Invited"].map((value) => ({
                                    value: value as UserFilterStatus,
                                    label: value
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
                            Reset
                        </Button>
                    </div>
                </div>

                <div className="user-table-toolbar">
                    <Text type={hasSelectedUsers ? undefined : "secondary"}>
                        已选择 {selectedRowKeys.length} 项
                    </Text>
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
                            type="primary"
                            icon={<PoweroffOutlined />}
                            disabled={!hasSelectedUsers}
                        >
                            启用
                        </Button>
                    </Space>
                </div>

                <Table<UserRecord>
                    rowKey="id"
                    className="user-table"
                    columns={columns}
                    dataSource={filteredUsers}
                    pagination={{
                        current: 1,
                        pageSize: 50,
                        total: 1248,
                        showSizeChanger: false,
                        showTotal: () => "1,248 users"
                    }}
                    rowSelection={{
                        selectedRowKeys,
                        onChange: setSelectedRowKeys
                    }}
                    scroll={{ x: Object.values(columnWidths).reduce((sum, width) => sum + width, 0) }}
                />
            </section>

            <Drawer
                className="user-edit-drawer"
                title="Edit User"
                open={Boolean(editingUser)}
                size="default"
                onClose={() => setEditingUser(null)}
                extra={<Button size="small">−</Button>}
                footer={
                    <div className="user-edit-footer">
                        <Button onClick={() => setEditingUser(null)}>Cancel</Button>
                        <Button type="primary" onClick={() => setEditingUser(null)}>
                            Update User
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
                            <span>Full Name</span>
                            <Input value={editingUser.name} readOnly />
                        </label>
                        <label>
                            <span>Email</span>
                            <Input value={editingUser.email} readOnly />
                        </label>
                        <label>
                            <span>Role</span>
                            <Select
                                value={editingUser.role}
                                options={["Admin", "Editor", "Viewer"].map((value) => ({ value }))}
                            />
                        </label>
                        <label>
                            <span>Status</span>
                            <Select
                                value={editingUser.status}
                                options={["Active", "Inactive", "Invited"].map((value) => ({
                                    value
                                }))}
                            />
                        </label>
                        <label>
                            <span>Organization</span>
                            <Select
                                value="Acme Corporation"
                                options={[{ value: "Acme Corporation" }]}
                            />
                        </label>
                        <label>
                            <span>Projects</span>
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
            </Drawer>

            <Modal
                className="user-delete-modal"
                title="Delete User"
                open={Boolean(deletingUser)}
                centered
                width={360}
                okText="Delete User"
                cancelText="Cancel"
                okButtonProps={{ danger: true, disabled: deleteConfirmText !== "delete" }}
                onCancel={() => setDeletingUser(null)}
                onOk={() => setDeletingUser(null)}
            >
                {deletingUser ? (
                    <div className="user-delete-content">
                        <ExclamationCircleOutlined className="user-delete-warning" />
                        <strong>Are you sure you want to delete this user?</strong>
                        <Text type="secondary">
                            This action cannot be undone. All user data will be permanently removed.
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
                            <span>Type "delete" to confirm</span>
                            <Input
                                value={deleteConfirmText}
                                onChange={(event) => setDeleteConfirmText(event.target.value)}
                            />
                        </label>
                    </div>
                ) : null}
            </Modal>
        </main>
    );
};
