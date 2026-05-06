import {
    AppstoreOutlined,
    AuditOutlined,
    BookOutlined,
    CloudServerOutlined,
    MenuOutlined,
    SafetyCertificateOutlined,
    TeamOutlined
} from "@ant-design/icons";
import { Button, Layout, Menu, Typography } from "antd";
import type { MenuProps } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

const { Header, Sider, Content } = Layout;
const { Title, Text } = Typography;

const menuItems: MenuProps["items"] = [
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

function getOpenKeys(pathname: string) {
    const openKeys: string[] = [];

    if (pathname.startsWith("/system/")) {
        openKeys.push("system");
    }

    if (pathname.startsWith("/storage/")) {
        openKeys.push("storage");
    }

    return openKeys;
}

export function AdminLayout() {
    const location = useLocation();
    const navigate = useNavigate();

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
                    <Button type="default">连接检查</Button>
                </Header>

                <Content className="workspace">
                    <Outlet />
                </Content>
            </Layout>
        </Layout>
    );
}
