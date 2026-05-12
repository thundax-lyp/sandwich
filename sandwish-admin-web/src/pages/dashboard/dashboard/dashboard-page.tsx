import { AppstoreOutlined, AuditOutlined, CloudServerOutlined, TeamOutlined } from "@ant-design/icons";
import { Card, Typography } from "antd";

const { Title, Text, Paragraph } = Typography;

const metricItems = [
    {
        label: "在线会话",
        value: "22",
        delta: "15%",
        tone: "green",
        icon: <TeamOutlined />,
        line: "M8 54 L34 30 L58 36 L84 16 L112 20"
    },
    {
        label: "待处理日志",
        value: "320",
        delta: "4%",
        tone: "orange",
        icon: <AuditOutlined />,
        line: "M8 58 L30 24 L56 18 L84 36 L112 10"
    },
    {
        label: "存储对象",
        value: "1,080",
        delta: "8%",
        tone: "violet",
        icon: <CloudServerOutlined />,
        line: "M8 62 L36 48 L62 42 L88 28 L112 12"
    }
];

const workColumns = [
    {
        title: "草稿",
        count: 2,
        items: [
            ["登录态治理", "预认证会话、验证码与访问令牌链路"],
            ["菜单数据巡检", "清理孤立节点与循环父子关系"]
        ]
    },
    {
        title: "进行中",
        count: 2,
        items: [
            ["MQ 兼容层", "RabbitMQ / RocketMQ 发送消费闭环"],
            ["对象存储控制台", "本地与 S3 策略统一展示"]
        ]
    },
    {
        title: "已归档",
        count: 1,
        items: [["权限字典", "用户、角色、菜单权限基础模型"]]
    }
];

export const DashboardPage = () => {
    return (
        <div className="dashboard-page">
            <section className="dashboard-hero">
                <Text className="eyebrow">仪表盘</Text>
                <Title level={2}>仪表盘已就绪</Title>
                <strong>Sandwich 管理台</strong>
                <Paragraph>系统运行态、权限治理和基础资源管理都在这里聚合。</Paragraph>
            </section>

            <section className="metrics" aria-label="核心指标">
                {metricItems.map((metric) => (
                    <Card className={`metric-card metric-card-${metric.tone}`} key={metric.label}>
                        <div className="metric-card-heading">
                            <Text>{metric.label}</Text>
                            <span>{metric.icon}</span>
                        </div>
                        <div className="metric-card-body">
                            <div>
                                <strong>{metric.value}</strong>
                                <span className="metric-delta">{metric.delta}</span>
                                <p>较上周</p>
                            </div>
                            <svg viewBox="0 0 120 72" role="img" aria-label={`${metric.label}趋势`}>
                                <path className="metric-chart-fill" d={`${metric.line} L112 72 L8 72 Z`} />
                                <path className="metric-chart-line" d={metric.line} />
                            </svg>
                        </div>
                    </Card>
                ))}
            </section>

            <section className="campaign-board">
                <div className="section-title-row">
                    <Title level={3}>最近操作</Title>
                    <button type="button">查看全部</button>
                </div>
                <div className="operation-columns">
                    {workColumns.map((column) => (
                        <div className="operation-column" key={column.title}>
                            <Text>
                                {column.title} <span>{column.count}</span>
                            </Text>
                            {column.items.map(([title, description]) => (
                                <article className="operation-card" key={title}>
                                    <div className="operation-card-icon">
                                        <AppstoreOutlined />
                                    </div>
                                    <strong>{title}</strong>
                                    <p>{description}</p>
                                    <div className="operation-progress">
                                        <span />
                                    </div>
                                </article>
                            ))}
                            <button className="add-operation" type="button">
                                + 新增操作
                            </button>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
};
