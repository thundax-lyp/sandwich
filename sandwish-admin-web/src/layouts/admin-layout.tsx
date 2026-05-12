import {
    AppstoreOutlined,
    AuditOutlined,
    BookOutlined,
    CloudServerOutlined,
    DownOutlined,
    IdcardOutlined,
    LogoutOutlined,
    MenuFoldOutlined,
    MenuOutlined,
    MenuUnfoldOutlined,
    MoonOutlined,
    SafetyCertificateOutlined,
    SunOutlined,
    TeamOutlined,
    UserOutlined
} from "@ant-design/icons";
import { Alert, Avatar, Button, Dropdown, Layout, Menu, Space, Typography, message } from "antd";
import { useMutation, useQuery } from "@tanstack/react-query";
import type { MenuProps } from "antd";
import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { logout } from "../api/auth-api";
import { refreshAccessTokenIfNeeded } from "../api/http";
import { replacePermissions } from "../auth/permission-storage";
import { clearAccessToken, getAccessToken } from "../auth/token-storage";
import { SandwichLogo } from "../components/sandwich-logo";
import {
    getCurrentUserInfo,
    listCurrentUserMenus,
    listCurrentUserPerms
} from "../service/current-user-service";
import { getStoredTheme, setAdminTheme, subscribeAdminThemeChange } from "../theme/theme-storage";

const { Header, Sider, Content } = Layout;
const { Text } = Typography;
const TOKEN_KEEP_ALIVE_INTERVAL_MS = 30 * 1000;

const fallbackMenuItems: MenuProps["items"] = [
    {
        key: "/dashboard",
        icon: <AppstoreOutlined />,
        label: "仪表盘"
    },
    {
        key: "system",
        icon: <SafetyCertificateOutlined />,
        label: "系统管理",
        children: [
            {
                key: "/system/users",
                icon: <TeamOutlined />,
                label: "用户管理"
            },
            {
                key: "/system/departments",
                icon: <AppstoreOutlined />,
                label: "部门管理"
            },
            {
                key: "/system/roles",
                icon: <SafetyCertificateOutlined />,
                label: "角色管理"
            },
            {
                key: "/system/menus",
                icon: <MenuOutlined />,
                label: "菜单管理"
            },
            {
                key: "/system/dictionaries",
                icon: <BookOutlined />,
                label: "字典管理"
            },
            {
                key: "/system/logs",
                icon: <AuditOutlined />,
                label: "系统日志"
            }
        ]
    },
    {
        key: "storage",
        icon: <CloudServerOutlined />,
        label: "存储管理",
        children: [
            {
                key: "/storage/objects",
                icon: <CloudServerOutlined />,
                label: "存储对象"
            }
        ]
    }
];

const menuIconMap: Record<string, ReactNode> = {
    dashboard: <AppstoreOutlined />,
    system: <SafetyCertificateOutlined />,
    users: <TeamOutlined />,
    roles: <SafetyCertificateOutlined />,
    menus: <MenuOutlined />,
    departments: <AppstoreOutlined />,
    dictionaries: <BookOutlined />,
    logs: <AuditOutlined />,
    storage: <CloudServerOutlined />,
    "storage-objects": <CloudServerOutlined />,
    permission: <SafetyCertificateOutlined />,
    "/dashboard": <AppstoreOutlined />,
    "/system/users": <TeamOutlined />,
    "/system/departments": <AppstoreOutlined />,
    "/system/roles": <SafetyCertificateOutlined />,
    "/system/menus": <MenuOutlined />,
    "/system/dictionaries": <BookOutlined />,
    "/system/logs": <AuditOutlined />,
    "/storage/objects": <CloudServerOutlined />
};

const getOpenKeys = (pathname: string) => {
    const openKeys: string[] = [];

    if (pathname.startsWith("/system/")) {
        openKeys.push("system");
    }

    if (pathname.startsWith("/storage/")) {
        openKeys.push("storage");
    }

    return openKeys;
};

const normalizeMenuKey = (menu: { id: number; url?: string | null }) => {
    return menu.url || String(menu.id);
};

const getDisplayIcon = (displayParams?: string | null) => {
    if (!displayParams) {
        return undefined;
    }

    try {
        const parsedDisplayParams = JSON.parse(displayParams) as { icon?: unknown };
        return typeof parsedDisplayParams.icon === "string" ? parsedDisplayParams.icon : undefined;
    } catch {
        return undefined;
    }
};

const buildAuthorizedMenuItems = (
    menus: Awaited<ReturnType<typeof listCurrentUserMenus>>
): MenuProps["items"] => {
    if (!menus.length) {
        return fallbackMenuItems;
    }

    const menuIds = new Set(menus.map((menu) => menu.id));
    const childrenByParentId = new Map<number | null, typeof menus>();
    menus.forEach((menu) => {
        const parentId = menu.parentId && menu.parentId !== menu.id && menuIds.has(menu.parentId) ? menu.parentId : null;
        const siblings = childrenByParentId.get(parentId) || [];
        siblings.push(menu);
        childrenByParentId.set(parentId, siblings);
    });

    const visited = new Set<number>();
    const toMenuItem = (
        menu: (typeof menus)[number],
        ancestors: Set<number> = new Set()
    ): NonNullable<MenuProps["items"]>[number] => {
        const key = normalizeMenuKey(menu);
        const nextAncestors = new Set(ancestors);
        nextAncestors.add(menu.id);
        visited.add(menu.id);
        const children = (childrenByParentId.get(menu.id) || []).filter((child) => !nextAncestors.has(child.id));

        return {
            key,
            icon: menuIconMap[getDisplayIcon(menu.displayParams) || ""] || menuIconMap[key] || menuIconMap[String(menu.id)],
            label: menu.name,
            children: children.length ? children.map((child) => toMenuItem(child, nextAncestors)) : undefined
        };
    };
    const rootMenus = childrenByParentId.get(null) || [];
    const rootMenuItems = rootMenus.map((menu) => toMenuItem(menu));
    const orphanMenuItems = menus
        .filter((menu) => !visited.has(menu.id))
        .map((menu) => toMenuItem(menu));

    return [
        {
            key: "/dashboard",
            icon: <AppstoreOutlined />,
            label: "仪表盘"
        },
        ...(rootMenuItems as NonNullable<MenuProps["items"]>),
        ...(orphanMenuItems as NonNullable<MenuProps["items"]>)
    ];
};

export const AdminLayout = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [themeName, setThemeName] = useState<"light" | "dark">(getStoredTheme);
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
    const currentUserInfoQuery = useQuery({
        queryKey: ["current-user", "info"],
        queryFn: getCurrentUserInfo,
        retry: false
    });
    const currentUserMenusQuery = useQuery({
        queryKey: ["current-user", "menus"],
        queryFn: listCurrentUserMenus,
        enabled: currentUserInfoQuery.isSuccess,
        retry: false
    });
    const currentUserPermsQuery = useQuery({
        queryKey: ["current-user", "perms"],
        queryFn: listCurrentUserPerms,
        enabled: currentUserInfoQuery.isSuccess,
        retry: false
    });
    const logoutMutation = useMutation({
        mutationFn: async () => {
            const token = getAccessToken();
            if (!token) {
                return false;
            }

            return logout({ token });
        },
        onSettled: () => {
            clearAccessToken();
            message.success("已退出登录");
            navigate("/login", { replace: true });
        }
    });
    const menuItems = currentUserMenusQuery.data
        ? buildAuthorizedMenuItems(currentUserMenusQuery.data)
        : fallbackMenuItems;
    const currentUser = currentUserInfoQuery.data;
    const currentUserName = currentUser?.name || currentUser?.loginName || "当前用户";

    useEffect(() => {
        if (currentUserPermsQuery.data) {
            replacePermissions(currentUserPermsQuery.data.perms || []);
        }
    }, [currentUserPermsQuery.data]);

    useEffect(() => {
        const syncTheme = () => setThemeName(getStoredTheme());
        return subscribeAdminThemeChange(syncTheme);
    }, []);

    useEffect(() => {
        void refreshAccessTokenIfNeeded();
        const timer = window.setInterval(() => {
            void refreshAccessTokenIfNeeded();
        }, TOKEN_KEEP_ALIVE_INTERVAL_MS);

        return () => window.clearInterval(timer);
    }, []);

    const toggleTheme = () => {
        const nextTheme = themeName === "dark" ? "light" : "dark";
        setThemeName(nextTheme);
        setAdminTheme(nextTheme);
    };
    const userMenuItems: MenuProps["items"] = [
        {
            key: "profile",
            icon: <IdcardOutlined />,
            label: "个人资料"
        },
        {
            type: "divider"
        },
        {
            key: "logout",
            icon: <LogoutOutlined />,
            label: "退出登录"
        }
    ];

    return (
        <Layout className="admin-shell">
            <Sider
                className="sidebar"
                width={248}
                collapsedWidth={88}
                collapsed={sidebarCollapsed}
                trigger={null}
            >
                <div className="brand">
                    <SandwichLogo className="brand-logo" />
                    <div className="brand-copy">
                        <strong>Sandwich</strong>
                        <span>管理台</span>
                    </div>
                </div>

                <Menu
                    className="nav-menu"
                    mode="inline"
                    defaultOpenKeys={getOpenKeys(location.pathname)}
                    selectedKeys={[location.pathname]}
                    items={menuItems}
                    onClick={({ key }) => navigate(key)}
                />
            </Sider>

            <Layout>
                <Header className="topbar">
                    <div className="topbar-heading">
                        <Button
                            className="sidebar-toggle"
                            type="text"
                            shape="circle"
                            aria-label={sidebarCollapsed ? "展开菜单" : "收起菜单"}
                            icon={sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                            onClick={() => setSidebarCollapsed((collapsed) => !collapsed)}
                        />
                        <div>
                            <Text className="topbar-path">Sandwich / 管理台</Text>
                        </div>
                    </div>
                    <Space className="topbar-actions">
                        <Button
                            shape="circle"
                            icon={themeName === "dark" ? <SunOutlined /> : <MoonOutlined />}
                            onClick={toggleTheme}
                        />
                        <Dropdown
                            menu={{
                                items: userMenuItems,
                                onClick: ({ key }) => {
                                    if (key === "logout") {
                                        logoutMutation.mutate();
                                    }
                                    if (key === "profile") {
                                        message.info("个人资料功能待接入");
                                    }
                                }
                            }}
                            trigger={["click"]}
                        >
                            <Button className="user-menu-trigger" loading={logoutMutation.isPending}>
                                <Avatar size={32} src={currentUser?.avatar} icon={<UserOutlined />} />
                                <span className="user-menu-copy">
                                    <Text strong>{currentUserName}</Text>
                                    <Text type="secondary">{currentUser?.loginName || "未连接"}</Text>
                                </span>
                                <DownOutlined />
                            </Button>
                        </Dropdown>
                    </Space>
                </Header>

                <div className="admin-content-grid">
                    <Content className="workspace">
                        {currentUserInfoQuery.isError ? (
                            <Alert
                                type="warning"
                                showIcon
                                message="当前用户信息加载失败"
                                description="请确认当前登录态有效，并检查 admin-api 当前用户接口。"
                                style={{ marginBottom: 16 }}
                            />
                        ) : null}
                        {currentUserMenusQuery.isError ? (
                            <Alert
                                type="warning"
                                showIcon
                                message="权限菜单加载失败"
                                description="请确认当前登录态有效，并检查 admin-api 权限菜单接口。"
                                style={{ marginBottom: 16 }}
                            />
                        ) : null}
                        {currentUserPermsQuery.isError ? (
                            <Alert
                                type="warning"
                                showIcon
                                message="权限字符串加载失败"
                                description="请确认当前登录态有效，并检查 admin-api 当前用户权限接口。"
                                style={{ marginBottom: 16 }}
                            />
                        ) : null}
                        <Outlet />
                    </Content>
                </div>
            </Layout>
        </Layout>
    );
};
