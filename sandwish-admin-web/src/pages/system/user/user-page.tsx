import {
    CameraOutlined,
    DeleteOutlined,
    EditOutlined,
    ExclamationCircleOutlined,
    FilterOutlined,
    HolderOutlined,
    MoreOutlined,
    PlusOutlined,
    PoweroffOutlined,
    SearchOutlined
} from "@ant-design/icons";
import {
    Avatar,
    Button,
    Drawer,
    Dropdown,
    Input,
    Modal,
    Select,
    Space,
    Table,
    Tag,
    Typography
} from "antd";
import type { TableProps } from "antd";
import { useEffect, useMemo, useState } from "react";
import type { Key } from "react";
import type { DragEvent as ReactDragEvent, MouseEvent as ReactMouseEvent } from "react";

const { Text, Title } = Typography;

const MIN_COLUMN_WIDTH = 96;
const MOBILE_MEDIA_QUERY = "(max-width: 760px)";
const DESKTOP_ACTION_COLUMN_WIDTH = 116;
const MOBILE_ACTION_COLUMN_WIDTH = 54;
const DEFAULT_COLUMN_WIDTHS = {
    name: 220,
    email: 220,
    role: 120,
    status: 130,
    lastLogin: 160,
    actions: DESKTOP_ACTION_COLUMN_WIDTH
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
type DropPosition = "before" | "after";

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
        avatarColor: "#0f766e"
    },
    {
        id: "2",
        name: "Sophia Carter",
        email: "sophia@acme.com",
        role: "Editor",
        status: "Active",
        lastLogin: "1 小时前",
        avatarColor: "#c2410c"
    },
    {
        id: "3",
        name: "Liam Johnson",
        email: "liam@acme.com",
        role: "Viewer",
        status: "Active",
        lastLogin: "3 小时前",
        avatarColor: "#1d4ed8"
    },
    {
        id: "4",
        name: "Olivia Martinez",
        email: "olivia@acme.com",
        role: "Editor",
        status: "Inactive",
        lastLogin: "2 天前",
        avatarColor: "#be185d"
    },
    {
        id: "5",
        name: "Noah Williams",
        email: "noah@acme.com",
        role: "Viewer",
        status: "Invited",
        lastLogin: "从未登录",
        avatarColor: "#0369a1"
    },
    {
        id: "6",
        name: "Ava Brown",
        email: "ava@acme.com",
        role: "Admin",
        status: "Active",
        lastLogin: "5 分钟前",
        avatarColor: "#7c3aed"
    },
    {
        id: "7",
        name: "James Davis",
        email: "james@acme.com",
        role: "Editor",
        status: "Active",
        lastLogin: "1 天前",
        avatarColor: "#b45309"
    }
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

export const UserPage = () => {
    const [users, setUsers] = useState<UserRecord[]>(USER_RECORDS);
    const [searchText, setSearchText] = useState("");
    const [filtersOpen, setFiltersOpen] = useState(false);
    const [filters, setFilters] = useState<UserFilters>(DEFAULT_USER_FILTERS);
    const [editingUser, setEditingUser] = useState<UserRecord | null>(null);
    const [deletingUser, setDeletingUser] = useState<UserRecord | null>(null);
    const [deleteConfirmText, setDeleteConfirmText] = useState("delete");
    const [columnWidths, setColumnWidths] = useState(DEFAULT_COLUMN_WIDTHS);
    const [isMobileTable, setIsMobileTable] = useState(false);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [draggingUserId, setDraggingUserId] = useState<string | null>(null);
    const [dropTarget, setDropTarget] = useState<{ userId: string; position: DropPosition } | null>(null);
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

    useEffect(() => {
        if (typeof window.matchMedia !== "function") {
            return undefined;
        }

        const mediaQueryList = window.matchMedia(MOBILE_MEDIA_QUERY);
        const updateMobileTable = () => setIsMobileTable(mediaQueryList.matches);

        updateMobileTable();
        mediaQueryList.addEventListener("change", updateMobileTable);
        return () => mediaQueryList.removeEventListener("change", updateMobileTable);
    }, []);

    const actionColumnWidth = isMobileTable ? MOBILE_ACTION_COLUMN_WIDTH : DESKTOP_ACTION_COLUMN_WIDTH;
    const tableScrollX =
        columnWidths.name + columnWidths.email + columnWidths.role + columnWidths.status + columnWidths.lastLogin
        + actionColumnWidth;

    const filteredUsers = useMemo(() => {
        const keyword = searchText.trim().toLowerCase();
        const emailKeyword = filters.email.trim().toLowerCase();
        return users.filter((user) => {
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
    }, [filters, searchText, users]);

    const startUserDrag = (userId: string) => (event: ReactDragEvent<HTMLButtonElement>) => {
        setDraggingUserId(userId);
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("text/plain", userId);
    };

    const readDropPosition = (event: ReactDragEvent<HTMLElement>): DropPosition => {
        const rowRect = event.currentTarget.getBoundingClientRect();
        return event.clientY < rowRect.top + rowRect.height / 2 ? "before" : "after";
    };

    const moveUser = (sourceUserId: string, targetUserId: string, position: DropPosition) => {
        if (sourceUserId === targetUserId) {
            return;
        }

        setUsers((currentUsers) => {
            const sourceIndex = currentUsers.findIndex((user) => user.id === sourceUserId);
            const targetIndex = currentUsers.findIndex((user) => user.id === targetUserId);
            if (sourceIndex < 0 || targetIndex < 0) {
                return currentUsers;
            }

            const nextUsers = [...currentUsers];
            const [sourceUser] = nextUsers.splice(sourceIndex, 1);
            const nextTargetIndex = nextUsers.findIndex((user) => user.id === targetUserId);
            nextUsers.splice(position === "before" ? nextTargetIndex : nextTargetIndex + 1, 0, sourceUser);
            return nextUsers;
        });
    };

    const clearUserDrag = () => {
        setDraggingUserId(null);
        setDropTarget(null);
    };

    const resetFilters = () => {
        setFilters(DEFAULT_USER_FILTERS);
    };

    const columns: TableProps<UserRecord>["columns"] = [
        {
            title: renderResizableTitle("name", "用户"),
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
            title: renderResizableTitle("email", "邮箱"),
            dataIndex: "email",
            key: "email",
            width: columnWidths.email
        },
        {
            title: renderResizableTitle("role", "角色"),
            dataIndex: "role",
            key: "role",
            width: columnWidths.role,
            render: (role: UserRecord["role"]) => <Tag className={roleClassName[role]}>{roleLabel[role]}</Tag>
        },
        {
            title: renderResizableTitle("status", "状态"),
            dataIndex: "status",
            key: "status",
            width: columnWidths.status,
            render: (status: UserRecord["status"]) => (
                <Tag className={statusClassName[status]}>{statusLabel[status]}</Tag>
            )
        },
        {
            title: renderResizableTitle("lastLogin", "最近登录"),
            dataIndex: "lastLogin",
            key: "lastLogin",
            width: columnWidths.lastLogin
        },
        {
            title: renderResizableTitle("actions", "操作"),
            key: "actions",
            width: actionColumnWidth,
            fixed: "right",
            render: (_, user) => (
                <div className="user-row-actions">
                    <Space.Compact className="user-row-actions-inline">
                        <Button
                            aria-label={`编辑 ${user.name}`}
                            className="user-row-action"
                            icon={<EditOutlined />}
                            type="text"
                            onClick={() => setEditingUser(user)}
                        />
                        <Button
                            aria-label={`删除 ${user.name}`}
                            className="user-row-action"
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
                        className="user-row-action user-row-drag-handle"
                        draggable
                        type="button"
                        onDragEnd={clearUserDrag}
                        onDragStart={startUserDrag(user.id)}
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
                            className="user-row-action user-row-action-more"
                            icon={<MoreOutlined />}
                            type="text"
                        />
                    </Dropdown>
                </div>
            )
        }
    ];

    return (
        <main className="user-page">
            <section className="user-command-panel">
                <div className="user-page-header">
                    <div>
                        <Title level={2}>用户管理</Title>
                        <Text type="secondary">管理后台用户、角色与权限状态。</Text>
                    </div>
                    <Space className="user-page-actions">
                        <Input
                            allowClear
                            className={`user-search${filtersOpen ? " user-search-hidden" : ""}`}
                            placeholder="搜索用户..."
                            prefix={<SearchOutlined />}
                            suffix={<span className="user-search-shortcut">⌘K</span>}
                            value={searchText}
                            onChange={(event) => setSearchText(event.target.value)}
                        />
                        <Button
                            className={filtersOpen || hasActiveFilters ? "user-filter-toggle-active" : undefined}
                            icon={<FilterOutlined />}
                            aria-expanded={filtersOpen}
                            onClick={() => setFiltersOpen((open) => !open)}
                        >
                            筛选
                        </Button>
                        <Button type="primary" icon={<PlusOutlined />}>
                            新增用户
                        </Button>
                    </Space>
                </div>

                <div className={`user-filter-panel${filtersOpen ? " user-filter-panel-open" : ""}`}>
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
                            onClick={() => setFiltersOpen(false)}
                        >
                            查询
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
                            className="user-batch-enable"
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
                    onRow={(user) => ({
                        className:
                            dropTarget?.userId === user.id
                                ? `user-row-drop-${dropTarget.position}`
                                : undefined,
                        onDragEnter: () => {
                            if (draggingUserId && draggingUserId !== user.id) {
                                setDropTarget({ userId: user.id, position: "before" });
                            }
                        },
                        onDragOver: (event) => {
                            if (draggingUserId && draggingUserId !== user.id) {
                                event.preventDefault();
                                event.dataTransfer.dropEffect = "move";
                                setDropTarget({ userId: user.id, position: readDropPosition(event) });
                            }
                        },
                        onDragLeave: () => {
                            if (dropTarget?.userId === user.id) {
                                setDropTarget(null);
                            }
                        },
                        onDrop: (event) => {
                            event.preventDefault();
                            const sourceUserId = event.dataTransfer.getData("text/plain") || draggingUserId;
                            if (sourceUserId) {
                                moveUser(sourceUserId, user.id, readDropPosition(event));
                            }
                            clearUserDrag();
                        }
                    })}
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
                    scroll={{ x: tableScrollX }}
                />
            </section>

            <Drawer
                className="user-edit-drawer"
                title="编辑用户"
                open={Boolean(editingUser)}
                size="default"
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
            </Drawer>

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
        </main>
    );
};
