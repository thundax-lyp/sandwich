import {
    CopyOutlined,
    EditOutlined,
    KeyOutlined,
    MoreOutlined,
    PoweroffOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    App,
    Button,
    Dropdown,
    Form,
    Input,
    Modal,
    Select,
    Space,
    Tag,
    Tooltip,
    Typography
} from "antd";
import { useMemo, useState } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { SandwishConfirmModal } from "@/components/sandwish-confirm-modal";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps } from "@/components/sandwish-table";
import {
    changeOpenClientStatus,
    createOpenClient,
    getOpenClient,
    pageOpenClients,
    resetOpenClientSecret,
    updateOpenClient
} from "./open-client-service";
import type {
    OpenClientPageRequest,
    OpenClientResponse,
    OpenClientSaveRequest,
    OpenClientSecretResponse,
    OpenClientStatus
} from "./open-client-service";
import "./open-client-page.css";

const { Text } = Typography;
const { TextArea } = Input;

const DEFAULT_PAGE_NO = 1;
const DEFAULT_PAGE_SIZE = 10;

const DEFAULT_COLUMN_WIDTHS = {
    name: 260,
    status: 120,
    expiredAt: 180,
    permissions: 280,
    actions: 104
};

interface OpenClientFilters {
    status: OpenClientStatus | "ALL";
}

interface OpenClientFormValues {
    id?: string | null;
    name: string;
    ipWhitelist?: string | null;
    expiredAt?: string | null;
    remarks?: string | null;
    permissions?: string[];
}

const DEFAULT_OPEN_CLIENT_FILTERS: OpenClientFilters = {
    status: "ALL"
};

const statusLabels: Record<OpenClientStatus, string> = {
    ENABLED: "启用",
    DISABLED: "停用"
};

const statusOptions: Array<{ label: string; value: OpenClientStatus }> = [
    { value: "ENABLED", label: statusLabels.ENABLED },
    { value: "DISABLED", label: statusLabels.DISABLED }
];

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readStatusFilterValue = (value: OpenClientStatus | "ALL") => {
    return value === "ALL" ? undefined : value;
};

const readStatusLabel = (status?: string | null) => {
    return status && status in statusLabels ? statusLabels[status as OpenClientStatus] : "未知";
};

const statusClassName = (status?: string | null) => {
    return status ? `open-client-status open-client-status-${status.toLowerCase()}` : "";
};

const formatDateTime = (value?: string | null) => {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
    });
};

const toDateTimeLocalValue = (value?: string | null) => {
    if (!value) {
        return undefined;
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value.slice(0, 16);
    }

    const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60 * 1000);
    return offsetDate.toISOString().slice(0, 16);
};

const toApiDateValue = (value?: string | null) => {
    const normalizedValue = normalizeText(value);
    if (!normalizedValue) {
        return undefined;
    }

    const date = new Date(normalizedValue);
    return Number.isNaN(date.getTime()) ? normalizedValue : date.toISOString();
};

const formatIpWhitelistForForm = (value?: string | null) => {
    const normalizedValue = normalizeText(value);
    if (!normalizedValue) {
        return "";
    }

    try {
        const parsedValue = JSON.parse(normalizedValue);
        if (Array.isArray(parsedValue)) {
            return parsedValue.map(String).join("\n");
        }
    } catch {
        return normalizedValue;
    }

    return normalizedValue;
};

const toIpWhitelistJson = (value?: string | null) => {
    const items = (value || "")
        .split(/\r?\n|,/)
        .map((item) => item.trim())
        .filter(Boolean);
    return items.length > 0 ? JSON.stringify(items) : undefined;
};

const readFormRequest = (values: OpenClientFormValues): OpenClientSaveRequest => {
    return {
        id: values.id,
        name: values.name.trim(),
        ipWhitelist: toIpWhitelistJson(values.ipWhitelist),
        expiredAt: toApiDateValue(values.expiredAt),
        remarks: normalizeText(values.remarks),
        permissions: (values.permissions || [])
            .map((permission) => permission.trim())
            .filter(Boolean)
    };
};

const toFormValues = (client: OpenClientResponse): OpenClientFormValues => {
    return {
        id: client.id,
        name: client.name,
        ipWhitelist: formatIpWhitelistForForm(client.ipWhitelist),
        expiredAt: toDateTimeLocalValue(client.expiredAt),
        remarks: client.remarks,
        permissions: client.permissions || []
    };
};

export const OpenClientPage = () => {
    const { message: messageApi } = App.useApp();
    const [editForm] = Form.useForm<OpenClientFormValues>();
    const queryClient = useQueryClient();
    const canEditOpenClient = hasPermission("open:client:edit");
    const [query, setQuery] = useState<OpenClientPageRequest>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<OpenClientFilters>(DEFAULT_OPEN_CLIENT_FILTERS);
    const [editingClient, setEditingClient] = useState<OpenClientResponse | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const [secretResponse, setSecretResponse] = useState<OpenClientSecretResponse | null>(null);
    const [resettingClient, setResettingClient] = useState<OpenClientResponse | null>(null);
    const hasActiveFilters = filters.status !== "ALL";

    const openClientQuery = useQuery({
        queryKey: ["open-client", "page", query],
        queryFn: () => pageOpenClients(query),
        retry: false
    });
    const openClientPage = openClientQuery.data;
    const openClients = useMemo(() => openClientPage?.records || [], [openClientPage?.records]);
    const totalCount = openClientPage?.count ?? openClientPage?.totalCount ?? 0;
    const currentPageNo = openClientPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = openClientPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const invalidateOpenClientPage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["open-client", "page"] });
    };

    const saveMutation = useMutation<
        OpenClientResponse | OpenClientSecretResponse,
        Error,
        OpenClientSaveRequest
    >({
        mutationFn: (request: OpenClientSaveRequest) =>
            request.id ? updateOpenClient(request) : createOpenClient(request),
        onSuccess: async (response, variables) => {
            setEditorOpen(false);
            setEditingClient(null);
            editForm.resetFields();
            await invalidateOpenClientPage();
            if (!variables.id) {
                setSecretResponse(response as OpenClientSecretResponse);
            }
            messageApi.success(variables.id ? "开放客户端已更新" : "开放客户端已创建");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const statusMutation = useMutation({
        mutationFn: changeOpenClientStatus,
        onSuccess: async () => {
            await invalidateOpenClientPage();
            messageApi.success("开放客户端状态已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "状态更新失败");
        }
    });

    const detailMutation = useMutation({
        mutationFn: getOpenClient,
        onSuccess: (client) => {
            setEditingClient(client);
            editForm.setFieldsValue(toFormValues(client));
            setEditorOpen(true);
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "获取开放客户端失败");
        }
    });

    const resetSecretMutation = useMutation({
        mutationFn: resetOpenClientSecret,
        onSuccess: async (response) => {
            setResettingClient(null);
            await invalidateOpenClientPage();
            setEditingClient((current) =>
                current && current.id === response.id
                    ? { ...current, apiKey: response.apiKey }
                    : current
            );
            setSecretResponse(response);
            messageApi.success("API SECRET 已重置");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "重置失败");
        }
    });

    const updateQuery = (values: Partial<OpenClientPageRequest>) => {
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                name: nextQuery.name,
                status: nextQuery.status,
                pageNo: values.pageNo || DEFAULT_PAGE_NO,
                pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchOpenClients = (value: string) => {
        setSearchText(value);
        updateQuery({ name: normalizeText(value) });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_OPEN_CLIENT_FILTERS);
        updateQuery({
            status: undefined
        });
    };

    const openCreateEditor = () => {
        setEditingClient(null);
        editForm.resetFields();
        setEditorOpen(true);
    };

    const openUpdateEditor = (client: OpenClientResponse) => {
        detailMutation.mutate({ id: client.id });
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingClient(null);
        editForm.resetFields();
    };

    const saveOpenClient = async () => {
        const values = await editForm.validateFields();
        saveMutation.mutate(readFormRequest(values));
    };

    const toggleStatus = (client: OpenClientResponse) => {
        statusMutation.mutate({
            id: client.id,
            status: client.status === "ENABLED" ? "DISABLED" : "ENABLED"
        });
    };

    const resetSecret = () => {
        if (!resettingClient) {
            return;
        }
        resetSecretMutation.mutate({ id: resettingClient.id });
    };

    const copySecretValue = async (label: string, value?: string | null) => {
        if (!value) {
            return;
        }
        try {
            await navigator.clipboard.writeText(value);
            messageApi.success(`${label} 已复制`);
        } catch {
            messageApi.error("复制失败");
        }
    };

    const renderSecretField = (label: string, value?: string | null) => (
        <div className="open-client-secret-field">
            <div className="open-client-secret-field-header">
                <Text strong>{label}</Text>
            </div>
            <div className="open-client-secret-control">
                <div className="open-client-secret-value" title={value || undefined}>
                    {value || "-"}
                </div>
                <Tooltip title={`复制 ${label}`}>
                    <Button
                        className="open-client-secret-copy"
                        type="text"
                        icon={<CopyOutlined />}
                        aria-label={`复制 ${label}`}
                        disabled={!value}
                        onClick={() => copySecretValue(label, value)}
                    >
                        复制
                    </Button>
                </Tooltip>
            </div>
        </div>
    );

    const renderEditorApiKey = () => {
        if (!editingClient) {
            return null;
        }

        if (editingClient.apiKey) {
            return (
                <div className="open-client-editor-api-key">
                    {renderSecretField("API KEY", editingClient.apiKey)}
                </div>
            );
        }

        return (
            <div className="open-client-editor-api-key open-client-editor-api-key-empty">
                <Text type="secondary">API KEY 未生成</Text>
                {canEditOpenClient ? (
                    <Button
                        icon={<KeyOutlined />}
                        loading={resetSecretMutation.isPending}
                        onClick={() => setResettingClient(editingClient)}
                    >
                        生成凭据
                    </Button>
                ) : null}
            </div>
        );
    };

    const columns: SandwishTableProps<OpenClientResponse>["columns"] = [
        {
            key: "name",
            title: "客户端",
            width: DEFAULT_COLUMN_WIDTHS.name,
            render: (_, client) => (
                <div className="open-client-name-cell">
                    <Text strong>{client.name}</Text>
                    {client.remarks ? <Text type="secondary">{client.remarks}</Text> : null}
                </div>
            )
        },
        {
            dataIndex: "status",
            key: "status",
            title: "状态",
            width: DEFAULT_COLUMN_WIDTHS.status,
            render: (status?: string | null) => (
                <Tag className={statusClassName(status)}>{readStatusLabel(status)}</Tag>
            )
        },
        {
            dataIndex: "expiredAt",
            key: "expiredAt",
            title: "过期时间",
            width: DEFAULT_COLUMN_WIDTHS.expiredAt,
            render: (expiredAt?: string | null) => formatDateTime(expiredAt)
        },
        {
            dataIndex: "permissions",
            key: "permissions",
            title: "权限",
            width: DEFAULT_COLUMN_WIDTHS.permissions,
            render: (permissions?: string[] | null) => (
                <Space wrap size={[4, 4]} className="open-client-permission-list">
                    {(permissions || []).map((permission) => (
                        <Tag key={permission}>{permission}</Tag>
                    ))}
                    {!permissions?.length ? <Text type="secondary">-</Text> : null}
                </Space>
            )
        },
        {
            key: "actions",
            title: "操作",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, client) => {
                const enabled = client.status === "ENABLED";
                return (
                    <Space size={4}>
                        <Button
                            aria-label={`编辑 ${client.name}`}
                            icon={<EditOutlined />}
                            size="small"
                            loading={
                                detailMutation.isPending &&
                                detailMutation.variables?.id === client.id
                            }
                            disabled={!canEditOpenClient}
                            onClick={() => openUpdateEditor(client)}
                        />
                        <Dropdown
                            trigger={["click"]}
                            menu={{
                                items: [
                                    {
                                        key: "status",
                                        icon: <PoweroffOutlined />,
                                        label: enabled ? "停用" : "启用",
                                        disabled: !canEditOpenClient,
                                        onClick: () => toggleStatus(client)
                                    },
                                    {
                                        key: "secret",
                                        icon: <KeyOutlined />,
                                        label: "重置 API SECRET",
                                        disabled: !canEditOpenClient,
                                        onClick: () => setResettingClient(client)
                                    }
                                ]
                            }}
                        >
                            <Button
                                aria-label={`更多 ${client.name}`}
                                icon={<MoreOutlined />}
                                size="small"
                            />
                        </Dropdown>
                    </Space>
                );
            }
        }
    ];

    return (
        <>
            <ListPage<OpenClientResponse>
                pageClassName="open-client-page"
                title="开放客户端"
                description="管理第三方系统访问 Open API 使用的 API KEY、IP 白名单、有效期和最小权限集合。"
                subjectName="开放客户端"
                enableSearch
                searchValue={searchText}
                searchPlaceholder="搜索客户端..."
                onSearchChange={searchOpenClients}
                enableFilter
                filterActive={hasActiveFilters}
                filter={({ closeFilter }) => (
                    <div className="open-client-filter-form">
                        <label>
                            客户端名称
                            <Input
                                allowClear
                                placeholder="第三方主体"
                                value={searchText}
                                onChange={(event) => setSearchText(event.target.value)}
                            />
                        </label>
                        <label>
                            状态
                            <Select
                                value={filters.status}
                                options={[{ value: "ALL", label: "全部" }, ...statusOptions]}
                                onChange={(status) => setFilters({ status })}
                            />
                        </label>
                        <Button
                            className="open-client-filter-search"
                            icon={<SearchOutlined />}
                            type="primary"
                            onClick={() => {
                                updateQuery({
                                    name: normalizeText(searchText),
                                    status: readStatusFilterValue(filters.status)
                                });
                                closeFilter();
                            }}
                        >
                            查询
                        </Button>
                        <Button onClick={resetFilters}>重置</Button>
                    </div>
                )}
                enableAdd={canEditOpenClient}
                addText="新增客户端"
                onAdd={openCreateEditor}
                pageActions={
                    <Button
                        icon={<ReloadOutlined />}
                        loading={openClientQuery.isFetching}
                        onClick={() => openClientQuery.refetch()}
                    >
                        刷新
                    </Button>
                }
                tableLayout="fixed"
                columns={columns}
                dataSource={openClients}
                rowKey="id"
                loading={openClientQuery.isLoading || openClientQuery.isFetching}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    showSizeChanger: true,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
            />

            <SandwishDrawer
                title={editingClient ? "编辑开放客户端" : "新增开放客户端"}
                open={editorOpen}
                size="middle"
                onClose={closeEditor}
                extra={
                    <Space>
                        <Button onClick={closeEditor}>取消</Button>
                        <Button
                            type="primary"
                            loading={saveMutation.isPending}
                            onClick={saveOpenClient}
                        >
                            保存
                        </Button>
                    </Space>
                }
            >
                <Form
                    form={editForm}
                    className="open-client-editor-form"
                    layout="vertical"
                    initialValues={{ permissions: ["submission:submission:create"] }}
                >
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    {renderEditorApiKey()}
                    <Form.Item
                        name="name"
                        label="第三方主体名称"
                        rules={[{ required: true, message: "请输入第三方主体名称" }]}
                    >
                        <Input maxLength={128} placeholder="第三方应用或客户名称" />
                    </Form.Item>
                    <Form.Item name="ipWhitelist" label="IP 白名单">
                        <TextArea rows={4} placeholder="每行一个 IP 或 CIDR" />
                    </Form.Item>
                    <Form.Item name="expiredAt" label="过期时间">
                        <Input type="datetime-local" />
                    </Form.Item>
                    <Form.Item name="permissions" label="权限">
                        <Select mode="tags" tokenSeparators={[",", "\n"]} placeholder="权限码" />
                    </Form.Item>
                    <Form.Item name="remarks" label="备注">
                        <TextArea rows={3} maxLength={255} />
                    </Form.Item>
                </Form>
            </SandwishDrawer>

            <SandwishConfirmModal
                open={Boolean(resettingClient)}
                title="重置 API SECRET"
                message={`确认重置 ${resettingClient?.name || ""} 的 API SECRET？`}
                description="原 API SECRET 会立即失效，新明文只会在本次结果中显示。"
                okText="重置"
                confirmLoading={resetSecretMutation.isPending}
                onOk={resetSecret}
                onCancel={() => setResettingClient(null)}
            />

            <Modal
                className="open-client-secret-modal"
                open={Boolean(secretResponse)}
                width={680}
                title="API SECRET 已重置"
                okText="我已保存"
                cancelButtonProps={{ style: { display: "none" } }}
                onOk={() => setSecretResponse(null)}
                onCancel={() => setSecretResponse(null)}
            >
                <Text className="open-client-secret-note" type="secondary">
                    API KEY 保持不变，新的 API SECRET 只在本次结果中显示。
                </Text>
                <div className="open-client-secret-panel">
                    {renderSecretField("API KEY", secretResponse?.apiKey)}
                    {renderSecretField("API SECRET", secretResponse?.apiSecret)}
                </div>
            </Modal>
        </>
    );
};
