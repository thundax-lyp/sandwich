import {
    BookOutlined,
    DeleteOutlined,
    EditOutlined,
    MoreOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Dropdown, Form, Input, Modal, Space, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { hasPermission } from "@/auth/permission-storage";
import { ListPage } from "@/components/list-page";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps } from "@/components/sandwish-table";
import {
    addDictionary,
    deleteDictionaries,
    pageDictionaries,
    updateDictionary
} from "./dictionary-service";
import type { DictPageRequest, DictResponse, DictSaveRequest } from "./dictionary-service";
import "./dictionary-page.css";

const { Text } = Typography;
const { TextArea } = Input;

const DEFAULT_PAGE_NO = 1;
const DEFAULT_PAGE_SIZE = 10;

const DEFAULT_COLUMN_WIDTHS = {
    type: 220,
    label: 180,
    value: 180,
    remarks: 320,
    actions: 116
};

interface DictFormValues {
    id?: string | null;
    type: string;
    label: string;
    value: string;
    remarks?: string | null;
}

interface DictionaryFilters {
    remarks: string;
    type: string;
}

const DEFAULT_DICTIONARY_FILTERS: DictionaryFilters = {
    remarks: "",
    type: ""
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
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
    const { message: messageApi } = App.useApp();
    const [editForm] = Form.useForm<DictFormValues>();
    const queryClient = useQueryClient();
    const canEditDictionary = hasPermission("sys:dict:edit");
    const [query, setQuery] = useState<DictPageRequest>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<DictionaryFilters>(DEFAULT_DICTIONARY_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [editingDictionary, setEditingDictionary] = useState<DictResponse | null>(null);
    const [editorOpen, setEditorOpen] = useState(false);
    const hasSelectedDictionaries = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(filters.type.trim()) || Boolean(filters.remarks.trim());

    const dictionaryQuery = useQuery({
        queryKey: ["dictionary", "page", query],
        queryFn: () => pageDictionaries(query),
        retry: false
    });
    const dictionaryPage = dictionaryQuery.data;
    const dictionaries = useMemo(() => dictionaryPage?.records || [], [dictionaryPage?.records]);
    const totalCount = dictionaryPage?.count ?? dictionaryPage?.totalCount ?? 0;
    const currentPageNo = dictionaryPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = dictionaryPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

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

    const updateQuery = (values: Partial<DictPageRequest>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                type: nextQuery.type,
                label: nextQuery.label,
                remarks: nextQuery.remarks,
                pageNo: DEFAULT_PAGE_NO,
                pageSize: currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchDictionaries = (value: string) => {
        setSearchText(value);
        updateQuery({ label: normalizeSearch(value) });
    };

    const applyFilters = () => {
        updateQuery({
            remarks: normalizeSearch(filters.remarks),
            type: normalizeSearch(filters.type)
        });
    };

    const resetFilters = () => {
        setFilters(DEFAULT_DICTIONARY_FILTERS);
        updateQuery({
            remarks: undefined,
            type: undefined
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

    const confirmDelete = (ids: string[]) => {
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

    const columns: SandwishTableProps<DictResponse>["columns"] = [
        {
            title: "字典类型",
            dataIndex: "type",
            key: "type",
            width: DEFAULT_COLUMN_WIDTHS.type,
            render: (type: string) => <Tag className="dictionary-type-tag">{type}</Tag>
        },
        {
            title: "标签",
            dataIndex: "label",
            key: "label",
            width: DEFAULT_COLUMN_WIDTHS.label,
            render: (label: string) => <Text strong>{label}</Text>
        },
        {
            title: "值",
            dataIndex: "value",
            key: "value",
            width: DEFAULT_COLUMN_WIDTHS.value,
            render: (value: string) => <Text code>{value}</Text>
        },
        {
            title: "备注",
            dataIndex: "remarks",
            key: "remarks",
            width: DEFAULT_COLUMN_WIDTHS.remarks,
            ellipsis: true,
            render: (remarks?: string | null) => remarks || <Text type="secondary">未填写</Text>
        },
        {
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, dictionary) => (
                <div className="sandwish-table-row-actions">
                    <Space.Compact className="sandwish-table-row-actions-inline">
                        <Button
                            aria-label={`编辑 ${dictionary.label}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditDictionary}
                            icon={<EditOutlined />}
                            type="text"
                            onClick={() => openEditEditor(dictionary)}
                        />
                        <Button
                            aria-label={`删除 ${dictionary.label}`}
                            className="sandwish-table-row-action"
                            disabled={!canEditDictionary}
                            icon={<DeleteOutlined />}
                            type="text"
                            danger
                            onClick={() => confirmDelete([dictionary.id])}
                        />
                    </Space.Compact>
                    <Dropdown
                        menu={{
                            items: [
                                {
                                    key: "edit",
                                    disabled: !canEditDictionary,
                                    icon: <EditOutlined />,
                                    label: "编辑"
                                },
                                {
                                    key: "delete",
                                    danger: true,
                                    disabled: !canEditDictionary,
                                    icon: <DeleteOutlined />,
                                    label: "删除"
                                }
                            ],
                            onClick: ({ key }) => {
                                if (key === "edit") {
                                    openEditEditor(dictionary);
                                }
                                if (key === "delete") {
                                    confirmDelete([dictionary.id]);
                                }
                            }
                        }}
                        trigger={["click"]}
                    >
                        <Button
                            aria-label={`展开 ${dictionary.label} 操作`}
                            className="sandwish-table-row-action sandwish-table-row-action-more"
                            icon={<MoreOutlined />}
                            type="text"
                        />
                    </Dropdown>
                </div>
            )
        }
    ];

    return (
        <>
            <ListPage<DictResponse>
                pageClassName="dictionary-page"
                title="字典管理"
                description="维护系统字典类型、展示标签、业务值和备注说明。"
                subjectName="字典项"
                enableAdd={canEditDictionary}
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                onSearchChange={searchDictionaries}
                onAdd={openCreateEditor}
                filterActive={hasActiveFilters}
                filter={({ closeFilter }) => (
                    <div className="dictionary-filter-form">
                        <label>
                            <span>字典类型</span>
                            <Input
                                allowClear
                                placeholder="user_status"
                                prefix={<BookOutlined />}
                                value={filters.type}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        type: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>备注</span>
                            <Input
                                allowClear
                                placeholder="备注关键词"
                                value={filters.remarks}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        remarks: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <Button onClick={resetFilters} disabled={!hasActiveFilters}>
                            重置
                        </Button>
                        <Button
                            className="dictionary-filter-search"
                            icon={<SearchOutlined />}
                            onClick={() => {
                                applyFilters();
                                closeFilter();
                            }}
                        >
                            查询
                        </Button>
                    </div>
                )}
                pageActions={
                    <Button icon={<ReloadOutlined />} onClick={() => dictionaryQuery.refetch()}>
                        刷新
                    </Button>
                }
                batchClassName="dictionary-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!canEditDictionary || !hasSelectedDictionaries}
                            loading={deleteMutation.isPending}
                            onClick={() => confirmDelete(selectedRowKeys.map(String))}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="dictionary-table"
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
                locale={{
                    emptyText: dictionaryQuery.isError
                        ? "字典列表加载失败，请确认权限和接口状态。"
                        : "暂无字典项"
                }}
            />

            <SandwishDrawer
                className="dictionary-edit-drawer"
                title={editingDictionary ? "编辑字典项" : "新增字典项"}
                open={editorOpen}
                size="small"
                onClose={closeEditor}
                footer={
                    <div className="dictionary-edit-footer">
                        <Button onClick={closeEditor}>取消</Button>
                        <Button
                            type="primary"
                            loading={saveMutation.isPending}
                            onClick={saveDictionary}
                        >
                            保存字典项
                        </Button>
                    </div>
                }
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
            </SandwishDrawer>
        </>
    );
};
