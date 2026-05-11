import {
    AppstoreOutlined,
    AuditOutlined,
    BookOutlined,
    CloudServerOutlined,
    LogoutOutlined,
    MenuOutlined,
    SafetyCertificateOutlined,
    TeamOutlined,
    UserOutlined
} from "@ant-design/icons";
import { Alert, Avatar, Button, Layout, Menu, Space, Typography, message } from "antd";
import { useMutation, useQuery } from "@tanstack/react-query";
import type { MenuProps } from "antd";
import { useEffect } from "react";
import type { ReactNode } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { logout } from "../api/auth-api";
import { replacePermissions } from "../auth/permission-storage";
import { clearAccessToken, getAccessToken } from "../auth/token-storage";
import {
    getCurrentUserInfo,
    listCurrentUserMenus,
    listCurrentUserPerms
} from "../service/current-user-service";

const { Header, Sider, Content } = Layout;
const { Title, Text } = Typography;

const fallbackMenuItems: MenuProps["items"] = [
    {
        key: "/dashboard",
        icon: <AppstoreOutlined />,
        label: "Dashboard"
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
    "/dashboard": <AppstoreOutlined />,
    "/system/users": <TeamOutlined />,
    "/system/departments": <AppstoreOutlined />,
    "/system/roles": <SafetyCertificateOutlined />,
    "/system/menus": <MenuOutlined />,
    "/system/dictionaries": <BookOutlined />,
    "/system/logs": <AuditOutlined />,
    "/storage/objects": <CloudServerOutlined />,
    system: <SafetyCertificateOutlined />,
    storage: <CloudServerOutlined />
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

const buildAuthorizedMenuItems = (
    menus: Awaited<ReturnType<typeof listCurrentUserMenus>>
): MenuProps["items"] => {
    if (!menus.length) {
        return fallbackMenuItems;
    }

    const childrenByParentId = new Map<number | null, typeof menus>();
    menus.forEach((menu) => {
        const parentId = menu.parentId || null;
        const siblings = childrenByParentId.get(parentId) || [];
        siblings.push(menu);
        childrenByParentId.set(parentId, siblings);
    });

    const toMenuItem = (menu: (typeof menus)[number]): NonNullable<MenuProps["items"]>[number] => {
        const key = normalizeMenuKey(menu);
        const children = childrenByParentId.get(menu.id) || [];

        return {
            key,
            icon: menuIconMap[key] || menuIconMap[String(menu.id)],
            label: menu.name,
            children: children.length ? children.map(toMenuItem) : undefined
        };
    };

    return [
        {
            key: "/dashboard",
            icon: <AppstoreOutlined />,
            label: "Dashboard"
        },
        ...((childrenByParentId.get(null) || []).map(toMenuItem) as NonNullable<MenuProps["items"]>)
    ];
};

export const AdminLayout = () => {
    const location = useLocation();
    const navigate = useNavigate();
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

    return (
        <Layout className="admin-shell">
            <Sider className="sidebar" width={248}>
                <div className="brand">
                    <img className="brand-logo" src="/sandwich-logo.svg" alt="Sandwich" />
                    <div>
                        <strong>Sandwich</strong>
                        <span>Admin Console</span>
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
                    <div>
                        <Text className="eyebrow">admin-api workspace</Text>
                        <Title level={1}>后台管理台</Title>
                    </div>
                    <Space className="topbar-actions">
                        <Space size={10}>
                            <Avatar size={36} src={currentUser?.avatar} icon={<UserOutlined />} />
                            <div>
                                <Text strong>{currentUserName}</Text>
                                <br />
                                <Text type="secondary">{currentUser?.loginName || "未连接"}</Text>
                            </div>
                        </Space>
                        <Button type="default">连接检查</Button>
                        <Button
                            icon={<LogoutOutlined />}
                            loading={logoutMutation.isPending}
                            onClick={() => logoutMutation.mutate()}
                        >
                            退出登录
                        </Button>
                    </Space>
                </Header>

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
            </Layout>
        </Layout>
    );
};
