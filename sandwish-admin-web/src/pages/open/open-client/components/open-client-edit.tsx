import { CopyOutlined, KeyOutlined } from "@ant-design/icons";
import { Button, Form, Input, Modal, Select, Space, Tooltip, Typography } from "antd";
import { useEffect } from "react";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type {
    OpenClientResponse,
    OpenClientSaveRequest,
    OpenClientSecretResponse
} from "../open-client-service";

const { Text } = Typography;
const { TextArea } = Input;

interface OpenClientEditProps {
    open?: boolean;
    client?: OpenClientResponse | null;
    saving?: boolean;
    canEdit?: boolean;
    resetSecretLoading?: boolean;
    onClose: () => void;
    onSave: (request: OpenClientSaveRequest) => void;
    onGenerateSecret: (client: OpenClientResponse) => void;
    onCopySecret: (label: string, value?: string | null) => void;
}

interface OpenClientFormValues {
    id?: string | null;
    name: string;
    ipWhitelist?: string | null;
    expiredAt?: string | null;
    remarks?: string | null;
    permissions?: string[];
}

interface OpenClientSecretModalProps {
    secret?: OpenClientSecretResponse | null;
    onClose: () => void;
    onCopySecret: (label: string, value?: string | null) => void;
}

const normalizeText = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
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

export const OpenClientSecretField = ({
    label,
    value,
    onCopy
}: {
    label: string;
    value?: string | null;
    onCopy: (label: string, value?: string | null) => void;
}) => (
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
                    onClick={() => onCopy(label, value)}
                >
                    复制
                </Button>
            </Tooltip>
        </div>
    </div>
);

export const OpenClientEdit = ({
    open,
    client,
    saving,
    canEdit,
    resetSecretLoading,
    onClose,
    onSave,
    onGenerateSecret,
    onCopySecret
}: OpenClientEditProps) => {
    const [form] = Form.useForm<OpenClientFormValues>();
    const editing = Boolean(client);

    useEffect(() => {
        if (!open) {
            form.resetFields();
            return;
        }
        if (client) {
            form.setFieldsValue(toFormValues(client));
            return;
        }
        form.resetFields();
        form.setFieldsValue({ permissions: ["submission:submission:create"] });
    }, [client, form, open]);

    const saveOpenClient = async () => {
        const values = await form.validateFields();
        onSave(readFormRequest(values));
    };

    const renderApiKey = () => {
        if (!client) {
            return null;
        }

        if (client.apiKey) {
            return (
                <div className="open-client-editor-api-key">
                    <OpenClientSecretField
                        label="API KEY"
                        value={client.apiKey}
                        onCopy={onCopySecret}
                    />
                </div>
            );
        }

        return (
            <div className="open-client-editor-api-key open-client-editor-api-key-empty">
                <Text type="secondary">API KEY 未生成</Text>
                {canEdit ? (
                    <Button
                        icon={<KeyOutlined />}
                        loading={resetSecretLoading}
                        onClick={() => onGenerateSecret(client)}
                    >
                        生成凭据
                    </Button>
                ) : null}
            </div>
        );
    };

    return (
        <SandwishDrawer
            title={editing ? "编辑开放客户端" : "新增开放客户端"}
            open={Boolean(open)}
            size="middle"
            onClose={onClose}
            extra={
                <Space>
                    <Button onClick={onClose}>取消</Button>
                    <Button type="primary" loading={saving} onClick={saveOpenClient}>
                        保存
                    </Button>
                </Space>
            }
        >
            <Form form={form} className="open-client-editor-form" layout="vertical">
                <Form.Item name="id" hidden>
                    <Input />
                </Form.Item>
                {renderApiKey()}
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
    );
};

export const OpenClientSecretModal = ({
    secret,
    onClose,
    onCopySecret
}: OpenClientSecretModalProps) => (
    <Modal
        className="open-client-secret-modal"
        open={Boolean(secret)}
        width={680}
        title="API SECRET 已重置"
        okText="我已保存"
        cancelButtonProps={{ style: { display: "none" } }}
        onOk={onClose}
        onCancel={onClose}
    >
        <Text className="open-client-secret-note" type="secondary">
            API KEY 保持不变，新的 API SECRET 只在本次结果中显示。
        </Text>
        <div className="open-client-secret-panel">
            <OpenClientSecretField
                label="API KEY"
                value={secret?.apiKey}
                onCopy={onCopySecret}
            />
            <OpenClientSecretField
                label="API SECRET"
                value={secret?.apiSecret}
                onCopy={onCopySecret}
            />
        </div>
    </Modal>
);
