import { Card, Space, Typography } from "antd";

const { Title, Text, Paragraph } = Typography;

export function DashboardPage() {
    return (
        <>
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
                    <Text className="eyebrow">dashboard</Text>
                    <Title level={2}>Dashboard 已就绪</Title>
                    <Paragraph>
                        这里是后台管理台首页，后续可以接入登录态概览、系统日志和业务域运行状态。
                    </Paragraph>
                </Space>
            </Card>
        </>
    );
}
