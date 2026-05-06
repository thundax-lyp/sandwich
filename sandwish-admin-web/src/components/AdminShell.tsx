import {
    AppstoreOutlined,
    AuditOutlined,
    CloudServerOutlined,
    SafetyCertificateOutlined,
    TeamOutlined
} from "@ant-design/icons";
import { Button, Card, Layout, Menu, Space, Typography } from "antd";

const { Header, Sider, Content } = Layout;
const { Title, Text, Paragraph } = Typography;

const menuItems = [
    {
        key: "dashboard",
        icon: <AppstoreOutlined />,
        label: "概览"
    },
    {
        key: "users",
        icon: <TeamOutlined />,
        label: "用户管理"
    },
    {
        key: "roles",
        icon: <SafetyCertificateOutlined />,
        label: "角色权限"
    },
    {
        key: "logs",
        icon: <AuditOutlined />,
        label: "系统日志"
    },
    {
        key: "storage",
        icon: <CloudServerOutlined />,
        label: "存储管理"
    }
];

export function AdminShell() {
    return (
        <Layout className="admin-shell">
            <Sider className="sidebar" width={248}>
                <div className="brand">
                    <span className="brand-mark">S</span>
                    <div>
                        <strong>Sandwich</strong>
                        <span>Admin Console</span>
                    </div>
                </div>

                <Menu
                    className="nav-menu"
                    mode="inline"
                    selectedKeys={["dashboard"]}
                    items={menuItems}
                />
            </Sider>

            <Layout>
                <Header className="topbar">
                    <div>
                        <Text className="eyebrow">admin-api workspace</Text>
                        <Title level={1}>后台管理台</Title>
                    </div>
                    <Button type="default">
                        连接检查
                    </Button>
                </Header>

                <Content className="workspace">
                    <section className="metrics" aria-label="核心指标">
                        <Card className="metric-card">
                            <Text type="secondary">在线会话</Text>
                            <strong>--</strong>
                        </Card>
                        <Card className="metric-card">
                            <Text type="secondary">待处理日志</Text>
                            <strong>--</strong>
                        </Card>
                        <Card className="metric-card">
                            <Text type="secondary">存储对象</Text>
                            <strong>--</strong>
                        </Card>
                    </section>

                    <Card className="panel">
                        <Space direction="vertical" size={8}>
                            <Text className="eyebrow">getting started</Text>
                            <Title level={2}>Ant Design 已接入</Title>
                            <Paragraph>
                                这里是管理端的应用壳，后续可以接入登录、路由、权限菜单和
                                <code>sandwish-admin-api</code> 的业务接口。
                            </Paragraph>
                        </Space>
                    </Card>
                </Content>
            </Layout>
        </Layout>
    );
}
