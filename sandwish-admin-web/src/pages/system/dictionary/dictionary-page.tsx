import {
    BookOutlined,
    DeleteOutlined,
    EditOutlined,
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
    Alert,
    Button,
    Card,
    Form,
    Input,
    Modal,
    Space,
    Table,
    Tag,
    Typography,
    message
} from "antd";
import type { TableProps } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import {
    addDictionary,
    deleteDictionaries,
    pageDictionaries,
    updateDictionary
} from "./dictionary-service";
import type { DictPageRequest, DictResponse, DictSaveRequest } from "./dictionary-service";

const { Text, Title } = Typography;
const { TextArea } = Input;

const DEFAULT_PAGE_NO = 1;
const DEFAULT_PAGE_SIZE = 10;

interface DictFormValues {
    id?: number | null;
    type: string;
    label: string;
    value: string;
    remarks?: string | null;
}

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readDictionaryQuery = (values: DictPageRequest): DictPageRequest => {
    return {
        type: normalizeSearch(values.type),
        label: normalizeSearch(values.label),
        remarks: normalizeSearch(values.remarks)
    };
};

const readFormRequest = (values: DictFormValues): DictSaveRequest => {
    return {
        id: values.id,
        type: values.type.trim(),
        label: values.label.trim(),
        value: values.value.trim(),
        remarks: normalizeSearch(values.remarks)
    };
};

export const DictionaryPage = () => {
    const [messageApi, contextHolder] = message.useMessage();
    const [form] = Form.useForm<DictPageRequest>();
    const [editForm] = Form.useForm<DictFormValues>();
    const queryClient = useQueryClient();
    const canEditDictionary = hasPermission("sys:dict:edit");
    const [query, setQuery] = useState<DictPageRequest>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingDictionary, setEditingDictionary] = useState<DictResponse | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);

    const dictionaryQuery = useQuery({
        queryKey: ["dictionary", "page", query],
        queryFn: () => pageDictionaries(query),
        retry: false
    });
    const dictionaryPage = dictionaryQuery.data;
    const dictionaries = useMemo(() => dictionaryPage?.records || [], [dictionaryPage?.records]);
    const totalCount = dictionaryPage?.totalCount || 0;
    const currentPageNo = query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = query.pageSize || DEFAULT_PAGE_SIZE;
    const typeCount = useMemo(
        () => new Set(dictionaries.map((item) => item.type)).size,
        [dictionaries]
    );
    const topTypes = useMemo(() => {
        const typeMap = new Map<string, number>();
        dictionaries.forEach((item) => {
            typeMap.set(item.type, (typeMap.get(item.type) || 0) + 1);
        });
        return Array.from(typeMap.entries())
            .sort((first, second) => second[1] - first[1])
            .slice(0, 5);
    }, [dictionaries]);

    const saveMutation = useMutation({
        mutationFn: (values: DictSaveRequest) =>
            values.id ? updateDictionary(values) : addDictionary(values),
        onSuccess: async () => {
            setEditorOpen(false);
            setEditingDictionary(null);
            editForm.resetFields();
            await queryClient.invalidateQueries({ queryKey: ["dictionary", "page"] });
            messageApi.success("字典项已保存");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "保存失败");
        }
    });

    const deleteMutation = useMutation({
        mutationFn: deleteDictionaries,
        onSuccess: async () => {
            setSelectedRowKeys([]);
            await queryClient.invalidateQueries({ queryKey: ["dictionary", "page"] });
            messageApi.success("字典项已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const searchDictionaries = (values: DictPageRequest) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => ({
            ...readDictionaryQuery(values),
            pageNo: DEFAULT_PAGE_NO,
            pageSize: currentQuery.pageSize || DEFAULT_PAGE_SIZE
        }));
    };

    const resetSearch = () => {
        form.resetFields();
        setSelectedRowKeys([]);
        setQuery({
            pageNo: DEFAULT_PAGE_NO,
            pageSize: query.pageSize || DEFAULT_PAGE_SIZE
        });
    };

    const openCreateEditor = () => {
        setEditingDictionary(null);
        editForm.resetFields();
        setEditorOpen(true);
    };

    const openEditEditor = (dictionary: DictResponse) => {
        setEditingDictionary(dictionary);
        editForm.setFieldsValue({
            id: dictionary.id,
            type: dictionary.type,
            label: dictionary.label,
            value: dictionary.value,
            remarks: dictionary.remarks
        });
        setEditorOpen(true);
    };

    const closeEditor = () => {
        if (saveMutation.isPending) {
            return;
        }
        setEditorOpen(false);
        setEditingDictionary(null);
        editForm.resetFields();
    };

    const saveDictionary = async () => {
        const values = await editForm.validateFields();
        saveMutation.mutate(readFormRequest(values));
    };

    const confirmDelete = (ids: number[]) => {
        Modal.confirm({
            title: "删除字典项",
            content: `确认删除 ${ids.length} 个字典项？删除后需要重新新增。`,
            okText: "删除",
            okButtonProps: {
                danger: true,
                loading: deleteMutation.isPending
            },
            cancelText: "取消",
            onOk: () => deleteMutation.mutateAsync(ids)
        });
    };

    const columns: TableProps<DictResponse>["columns"] = [
        {
            title: "字典类型",
            dataIndex: "type",
            key: "type",
            width: 220,
            render: (type: string) => <Tag color="green">{type}</Tag>
        },
        {
            title: "标签",
            dataIndex: "label",
            key: "label",
            width: 180,
            render: (label: string) => <strong>{label}</strong>
        },
        {
            title: "值",
            dataIndex: "value",
            key: "value",
            width: 180,
            render: (value: string) => <Text code>{value}</Text>
        },
        {
            title: "备注",
            dataIndex: "remarks",
            key: "remarks",
            ellipsis: true,
            render: (remarks?: string | null) => remarks || <Text type="secondary">未填写</Text>
        },
        {
            title: "操作",
            key: "action",
            width: 150,
            fixed: "right",
            render: (_, dictionary) => (
                <Space size={4}>
                    <Button
                        type="text"
                        icon={<EditOutlined />}
                        disabled={!canEditDictionary}
                        onClick={() => openEditEditor(dictionary)}
                    >
                        编辑
                    </Button>
                    <Button
                        type="text"
                        danger
                        icon={<DeleteOutlined />}
                        disabled={!canEditDictionary}
                        onClick={() => confirmDelete([dictionary.id])}
                    />
                </Space>
            )
        }
    ];

    return (
        <main className="dictionary-page">
            {contextHolder}
            <section className="dictionary-page-header">
                <div>
                    <Text className="eyebrow">system / dictionary</Text>
                    <Title level={2}>字典管理</Title>
                    <Text type="secondary">维护系统字典类型、展示标签、业务值和排序。</Text>
                </div>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => dictionaryQuery.refetch()}>
                        刷新
                    </Button>
                    <Button
                        type="primary"
                        icon={<PlusOutlined />}
                        disabled={!canEditDictionary}
                        onClick={openCreateEditor}
                    >
                        新增字典项
                    </Button>
                </Space>
            </section>

            <section className="dictionary-summary" aria-label="字典概览">
                <Card className="dictionary-summary-card">
                    <Text type="secondary">字典项</Text>
                    <strong>{totalCount}</strong>
                </Card>
                <Card className="dictionary-summary-card">
                    <Text type="secondary">当前页类型</Text>
                    <strong>{typeCount}</strong>
                </Card>
                <Card className="dictionary-summary-card">
                    <Text type="secondary">当前页</Text>
                    <strong>{dictionaries.length}</strong>
                </Card>
            </section>

            <Card className="dictionary-list-panel">
                <div className="dictionary-list-toolbar">
                    <div>
                        <Text className="eyebrow">dictionary list</Text>
                        <Title level={3}>字典项列表</Title>
                    </div>
                    <Form<DictPageRequest>
                        form={form}
                        className="dictionary-search-form"
                        layout="inline"
                        onFinish={searchDictionaries}
                    >
                        <Form.Item name="type">
                            <Input allowClear placeholder="字典类型" prefix={<BookOutlined />} />
                        </Form.Item>
                        <Form.Item name="label">
                            <Input allowClear placeholder="标签" prefix={<SearchOutlined />} />
                        </Form.Item>
                        <Form.Item name="remarks">
                            <Input allowClear placeholder="备注关键词" />
                        </Form.Item>
                        <Form.Item>
                            <Space>
                                <Button htmlType="submit" type="primary">
                                    查询
                                </Button>
                                <Button onClick={resetSearch}>重置</Button>
                            </Space>
                        </Form.Item>
                    </Form>
                </div>

                <div className="dictionary-type-strip" aria-label="当前页类型分布">
                    {topTypes.length ? (
                        topTypes.map(([type, count]) => (
                            <Tag key={type} color="default">
                                {type} · {count}
                            </Tag>
                        ))
                    ) : (
                        <Text type="secondary">当前筛选暂无类型分布</Text>
                    )}
                </div>

                {dictionaryQuery.isError ? (
                    <Alert
                        type="warning"
                        showIcon
                        message="字典列表加载失败"
                        description="请确认当前账号拥有 sys:dict:view 权限，并检查 admin-api 字典分页接口。"
                        action={<Button onClick={() => dictionaryQuery.refetch()}>重试</Button>}
                        style={{ marginBottom: 16 }}
                    />
                ) : null}

                <div className="dictionary-bulk-bar">
                    <Text type="secondary">已选择 {selectedRowKeys.length} 项</Text>
                    <Button
                        danger
                        icon={<DeleteOutlined />}
                        disabled={!canEditDictionary || selectedRowKeys.length === 0}
                        loading={deleteMutation.isPending}
                        onClick={() => confirmDelete(selectedRowKeys.map(Number))}
                    >
                        批量删除
                    </Button>
                </div>

                <Table<DictResponse>
                    rowKey="id"
                    columns={columns}
                    dataSource={dictionaries}
                    loading={dictionaryQuery.isFetching}
                    rowSelection={{
                        selectedRowKeys,
                        onChange: setSelectedRowKeys,
                        getCheckboxProps: () => ({
                            disabled: !canEditDictionary
                        })
                    }}
                    pagination={{
                        current: currentPageNo,
                        pageSize: currentPageSize,
                        total: totalCount,
                        showSizeChanger: true,
                        showTotal: (total) => `共 ${total} 项`,
                        onChange: (pageNo, pageSize) => {
                            setQuery((currentQuery) => ({
                                ...currentQuery,
                                pageNo,
                                pageSize
                            }));
                        }
                    }}
                    scroll={{ x: 1120 }}
                    locale={{
                        emptyText: (
                            <Space orientation="vertical" size={8}>
                                <BookOutlined className="dictionary-empty-icon" />
                                <Text type="secondary">暂无字典项</Text>
                            </Space>
                        )
                    }}
                />
            </Card>

            <Modal
                title={editingDictionary ? "编辑字典项" : "新增字典项"}
                open={editorOpen}
                okText="保存"
                cancelText="取消"
                confirmLoading={saveMutation.isPending}
                onCancel={closeEditor}
                onOk={saveDictionary}
                destroyOnHidden
            >
                <Form<DictFormValues>
                    form={editForm}
                    layout="vertical"
                    className="dictionary-editor-form"
                >
                    <Form.Item name="id" hidden>
                        <Input />
                    </Form.Item>
                    <Form.Item
                        name="type"
                        label="字典类型"
                        rules={[{ required: true, message: "请输入字典类型" }]}
                    >
                        <Input placeholder="例如：user_status" />
                    </Form.Item>
                    <Form.Item
                        name="label"
                        label="标签"
                        rules={[{ required: true, message: "请输入标签" }]}
                    >
                        <Input placeholder="例如：启用" />
                    </Form.Item>
                    <Form.Item
                        name="value"
                        label="值"
                        rules={[{ required: true, message: "请输入值" }]}
                    >
                        <Input placeholder="例如：ENABLED" />
                    </Form.Item>
                    <Form.Item name="remarks" label="备注">
                        <TextArea rows={3} maxLength={200} showCount placeholder="补充使用说明" />
                    </Form.Item>
                </Form>
            </Modal>
        </main>
    );
};
