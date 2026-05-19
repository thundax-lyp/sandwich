import {
    DeleteOutlined,
    EyeOutlined,
    FileOutlined,
    HolderOutlined,
    MoreOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { App, Button, Dropdown, Input, Select, Space, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import type { Key } from "react";
import { useCurrentAccessToken } from "@/auth/hooks";
import { hasPermission } from "@/auth/permission-storage";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { ListPage } from "@/components/list-page";
import { SandwishConfirmModal } from "@/components/sandwish-confirm-modal";
import type { SandwishTableProps, SandwishTableSortPosition } from "@/components/sandwish-table";
import {
    removeStorageObjects,
    pageStorageObjects,
    sortStorageObjects
} from "./storage-object-service";
import type { StoragePageRequest, StorageResponse } from "./storage-object-service";
import "./storage-object-page.css";

const { Text } = Typography;

const DEFAULT_PAGE_NO = 1;
const DEFAULT_PAGE_SIZE = 10;

const DEFAULT_COLUMN_WIDTHS = {
    name: 280,
    contentType: 180,
    owner: 170,
    objectStatus: 120,
    referenceStatus: 130,
    remarks: 240,
    actions: 116
};

type StorageObjectStatusFilter = "ALL" | "ACTIVE" | "DELETING" | "DELETED";
type StorageReferenceStatusFilter = "ALL" | "REFERENCED" | "UNREFERENCED";

interface StorageObjectFilters {
    contentType: string;
    objectStatus: StorageObjectStatusFilter;
    referenceStatus: StorageReferenceStatusFilter;
    remarks: string;
}

interface DeleteTarget {
    ids: string[];
    title: string;
}

const DEFAULT_STORAGE_OBJECT_FILTERS: StorageObjectFilters = {
    contentType: "",
    objectStatus: "ALL",
    referenceStatus: "ALL",
    remarks: ""
};

const objectStatusLabels: Record<Exclude<StorageObjectStatusFilter, "ALL">, string> = {
    ACTIVE: "可用",
    DELETING: "删除中",
    DELETED: "已删除"
};

const referenceStatusLabels: Record<Exclude<StorageReferenceStatusFilter, "ALL">, string> = {
    REFERENCED: "已引用",
    UNREFERENCED: "未引用"
};

const ownerTypeLabels: Record<string, string> = {
    USER: "后台用户",
    MEMBER: "前台会员"
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readFilename = (storage: StorageResponse) => {
    return normalizeSearch(storage.originalFilename) || `对象 ${storage.id}`;
};

const readStatusFilterValue = <T extends string>(value: T | "ALL") => {
    return value === "ALL" ? undefined : value;
};

const objectStatusClassName = (status?: string | null) => {
    return status ? `storage-object-status storage-object-status-${status.toLowerCase()}` : "";
};

const referenceStatusClassName = (status?: string | null) => {
    return status
        ? `storage-object-reference storage-object-reference-${status.toLowerCase()}`
        : "";
};

const sortByMove = (
    storages: StorageResponse[],
    sourceStorage: StorageResponse,
    targetStorage: StorageResponse,
    position: SandwishTableSortPosition
) => {
    const sourceIndex = storages.findIndex((storage) => storage.id === sourceStorage.id);
    const targetIndex = storages.findIndex((storage) => storage.id === targetStorage.id);
    if (sourceIndex < 0 || targetIndex < 0) {
        return storages;
    }

    const nextStorages = [...storages];
    const [movedStorage] = nextStorages.splice(sourceIndex, 1);
    const nextTargetIndex = nextStorages.findIndex((storage) => storage.id === targetStorage.id);
    nextStorages.splice(
        position === "before" ? nextTargetIndex : nextTargetIndex + 1,
        0,
        movedStorage
    );
    return nextStorages;
};

export const StorageObjectPage = () => {
    const { message: messageApi } = App.useApp();
    const queryClient = useQueryClient();
    const canEditStorage = hasPermission("storage:storage:edit");
    const accessToken = useCurrentAccessToken();
    const [query, setQuery] = useState<StoragePageRequest>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<StorageObjectFilters>(DEFAULT_STORAGE_OBJECT_FILTERS);
    const [selectedRowKeys, setSelectedRowKeys] = useState<Key[]>([]);
    const [deleteTarget, setDeleteTarget] = useState<DeleteTarget | null>(null);
    const hasSelectedStorages = selectedRowKeys.length > 0;
    const hasActiveFilters = Boolean(
        filters.contentType.trim() ||
        filters.remarks.trim() ||
        filters.objectStatus !== "ALL" ||
        filters.referenceStatus !== "ALL"
    );

    const storageQuery = useQuery({
        queryKey: ["storage-object", "page", query],
        queryFn: () => pageStorageObjects(query),
        retry: false
    });
    const storagePage = storageQuery.data;
    const storages = useMemo(() => storagePage?.records || [], [storagePage?.records]);
    const totalCount = storagePage?.count ?? storagePage?.totalCount ?? 0;
    const currentPageNo = storagePage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = storagePage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;

    const invalidateStoragePage = async () => {
        await queryClient.invalidateQueries({ queryKey: ["storage-object", "page"] });
    };

    const deleteMutation = useMutation({
        mutationFn: removeStorageObjects,
        onSuccess: async () => {
            setDeleteTarget(null);
            setSelectedRowKeys([]);
            await invalidateStoragePage();
            messageApi.success("存储对象已删除");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "删除失败");
        }
    });

    const sortMutation = useMutation({
        mutationFn: sortStorageObjects,
        onSuccess: async () => {
            await invalidateStoragePage();
            messageApi.success("存储对象顺序已更新");
        },
        onError: (error) => {
            messageApi.error(error instanceof Error ? error.message : "排序失败");
        }
    });

    const updateQuery = (values: Partial<StoragePageRequest>) => {
        setSelectedRowKeys([]);
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                contentType: nextQuery.contentType,
                objectStatus: nextQuery.objectStatus,
                referenceStatus: nextQuery.referenceStatus,
                originalFilename: nextQuery.originalFilename,
                remarks: nextQuery.remarks,
                pageNo: values.pageNo || DEFAULT_PAGE_NO,
                pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchStorages = (value: string) => {
        setSearchText(value);
        updateQuery({ originalFilename: normalizeSearch(value) });
    };

    const applyFilters = (closeFilter: () => void) => {
        updateQuery({
            contentType: normalizeSearch(filters.contentType),
            objectStatus: readStatusFilterValue(filters.objectStatus),
            referenceStatus: readStatusFilterValue(filters.referenceStatus),
            remarks: normalizeSearch(filters.remarks)
        });
        closeFilter();
    };

    const resetFilters = () => {
        setFilters(DEFAULT_STORAGE_OBJECT_FILTERS);
        updateQuery({
            contentType: undefined,
            objectStatus: undefined,
            referenceStatus: undefined,
            remarks: undefined
        });
    };

    const openDeleteConfirm = (storage: StorageResponse) => {
        setDeleteTarget({
            ids: [storage.id],
            title: readFilename(storage)
        });
    };

    const openBatchDeleteConfirm = () => {
        if (!hasSelectedStorages) {
            return;
        }
        setDeleteTarget({
            ids: selectedRowKeys.map(String),
            title: `${selectedRowKeys.length} 个存储对象`
        });
    };

    const deleteStorages = () => {
        if (!deleteTarget) {
            return;
        }
        deleteMutation.mutate(deleteTarget.ids);
    };

    const moveStorage = (
        sourceStorage: StorageResponse,
        targetStorage: StorageResponse,
        position: SandwishTableSortPosition
    ) => {
        if (!canEditStorage || sourceStorage.id === targetStorage.id) {
            return;
        }
        const nextStorages = sortByMove(storages, sourceStorage, targetStorage, position);
        sortMutation.mutate({
            orderedIds: nextStorages.map((storage) => storage.id)
        });
    };

    const columns: SandwishTableProps<StorageResponse>["columns"] = [
        {
            title: "文件",
            dataIndex: "originalFilename",
            key: "originalFilename",
            width: DEFAULT_COLUMN_WIDTHS.name,
            ellipsis: true,
            render: (_, storage) => (
                <Space size={10}>
                    <FileOutlined className="storage-object-file-icon" />
                    <div className="storage-object-name-cell">
                        <Text strong>{readFilename(storage)}</Text>
                        {storage.extendName ? (
                            <Text type="secondary">{storage.extendName}</Text>
                        ) : null}
                    </div>
                </Space>
            )
        },
        {
            title: "MIME",
            dataIndex: "contentType",
            key: "contentType",
            width: DEFAULT_COLUMN_WIDTHS.contentType,
            ellipsis: true,
            render: (contentType?: string | null) =>
                contentType ? <Text code>{contentType}</Text> : null
        },
        {
            title: "归属",
            key: "owner",
            width: DEFAULT_COLUMN_WIDTHS.owner,
            render: (_, storage) => {
                const ownerType = storage.ownerType
                    ? ownerTypeLabels[storage.ownerType] || storage.ownerType
                    : "";
                if (!ownerType && !storage.ownerId) {
                    return null;
                }
                return (
                    <div className="storage-object-owner-cell">
                        {ownerType ? <Text>{ownerType}</Text> : null}
                        {storage.ownerId ? <Text type="secondary">{storage.ownerId}</Text> : null}
                    </div>
                );
            }
        },
        {
            title: "状态",
            dataIndex: "objectStatus",
            key: "objectStatus",
            width: DEFAULT_COLUMN_WIDTHS.objectStatus,
            render: (status?: string | null) =>
                status ? (
                    <Tag className={objectStatusClassName(status)}>
                        {objectStatusLabels[status as Exclude<StorageObjectStatusFilter, "ALL">] ||
                            status}
                    </Tag>
                ) : null
        },
        {
            title: "引用",
            dataIndex: "referenceStatus",
            key: "referenceStatus",
            width: DEFAULT_COLUMN_WIDTHS.referenceStatus,
            render: (status?: string | null) =>
                status ? (
                    <Tag className={referenceStatusClassName(status)}>
                        {referenceStatusLabels[
                            status as Exclude<StorageReferenceStatusFilter, "ALL">
                        ] || status}
                    </Tag>
                ) : null
        },
        {
            title: "备注",
            dataIndex: "remarks",
            key: "remarks",
            width: DEFAULT_COLUMN_WIDTHS.remarks,
            ellipsis: true,
            render: (remarks?: string | null) => remarks || null
        },
        {
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, storage) => {
                const filename = readFilename(storage);
                const previewUrl = toAuthenticatedResourceUrl(storage.contentUrl, accessToken);
                return (
                    <div className="sandwish-table-row-actions">
                        <Space.Compact className="sandwish-table-row-actions-inline">
                            <Button
                                aria-label={`预览 ${filename}`}
                                className="sandwish-table-row-action"
                                disabled={!previewUrl}
                                href={previewUrl}
                                icon={<EyeOutlined />}
                                target="_blank"
                                type="text"
                            />
                            <Button
                                aria-label={`删除 ${filename}`}
                                className="sandwish-table-row-action"
                                disabled={!canEditStorage}
                                icon={<DeleteOutlined />}
                                type="text"
                                danger
                                onClick={() => openDeleteConfirm(storage)}
                            />
                        </Space.Compact>
                        <button
                            aria-label={`拖动排序 ${filename}`}
                            className="sandwish-table-row-action sandwish-table-row-drag-handle"
                            disabled={!canEditStorage}
                            type="button"
                        >
                            <HolderOutlined />
                        </button>
                        <Dropdown
                            menu={{
                                items: [
                                    {
                                        key: "preview",
                                        disabled: !previewUrl,
                                        icon: <EyeOutlined />,
                                        label: previewUrl ? (
                                            <a href={previewUrl} target="_blank" rel="noreferrer">
                                                预览
                                            </a>
                                        ) : (
                                            "预览"
                                        )
                                    },
                                    {
                                        key: "delete",
                                        danger: true,
                                        disabled: !canEditStorage,
                                        icon: <DeleteOutlined />,
                                        label: "删除"
                                    }
                                ],
                                onClick: ({ key }) => {
                                    if (key === "delete") {
                                        openDeleteConfirm(storage);
                                    }
                                }
                            }}
                            trigger={["click"]}
                        >
                            <Button
                                aria-label={`展开 ${filename} 操作`}
                                className="sandwish-table-row-action sandwish-table-row-action-more"
                                icon={<MoreOutlined />}
                                type="text"
                            />
                        </Dropdown>
                    </div>
                );
            }
        }
    ];

    return (
        <>
            <ListPage<StorageResponse>
                pageClassName="storage-object-page"
                title="存储对象"
                description="管理上传后的对象文件、存储状态和业务引用入口。"
                subjectName="存储对象"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                searchPlaceholder="搜索文件名..."
                onSearchChange={searchStorages}
                filterActive={hasActiveFilters}
                filterClassName="storage-object-filter-panel"
                filter={({ closeFilter }) => (
                    <div className="storage-object-filter-form">
                        <label>
                            <span>MIME</span>
                            <Input
                                allowClear
                                placeholder="image/png"
                                value={filters.contentType}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        contentType: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>对象状态</span>
                            <Select<StorageObjectStatusFilter>
                                value={filters.objectStatus}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    { value: "ACTIVE", label: "可用" },
                                    { value: "DELETING", label: "删除中" },
                                    { value: "DELETED", label: "已删除" }
                                ]}
                                onChange={(objectStatus) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        objectStatus
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>引用状态</span>
                            <Select<StorageReferenceStatusFilter>
                                value={filters.referenceStatus}
                                options={[
                                    { value: "ALL", label: "全部" },
                                    { value: "REFERENCED", label: "已引用" },
                                    { value: "UNREFERENCED", label: "未引用" }
                                ]}
                                onChange={(referenceStatus) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        referenceStatus
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>备注</span>
                            <Input
                                allowClear
                                placeholder="业务说明"
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
                            className="storage-object-filter-search"
                            icon={<SearchOutlined />}
                            onClick={() => applyFilters(closeFilter)}
                        >
                            查询
                        </Button>
                    </div>
                )}
                pageActions={
                    <Button
                        icon={<ReloadOutlined />}
                        loading={storageQuery.isFetching}
                        onClick={() => storageQuery.refetch()}
                    >
                        刷新
                    </Button>
                }
                batchClassName="storage-object-table-toolbar"
                selectedCount={selectedRowKeys.length}
                batchActions={
                    <Space wrap>
                        <Button
                            danger
                            icon={<DeleteOutlined />}
                            disabled={!hasSelectedStorages || !canEditStorage}
                            loading={deleteMutation.isPending}
                            onClick={openBatchDeleteConfirm}
                        >
                            批量删除
                        </Button>
                    </Space>
                }
                rowKey="id"
                className="storage-object-table"
                columns={columns}
                dataSource={storages}
                loading={storageQuery.isFetching || sortMutation.isPending}
                onSort={moveStorage}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    showSizeChanger: true,
                    showTotal: (total) => `${total} 个存储对象`,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys
                }}
                sortable={canEditStorage}
            />

            <SandwishConfirmModal
                title="删除存储对象"
                open={Boolean(deleteTarget)}
                message={`确认删除 ${deleteTarget?.title || ""}？`}
                description="删除后需要重新上传。若对象仍被业务引用，接口会按后端校验结果拦截。"
                okText="删除"
                confirmLoading={deleteMutation.isPending}
                cancelText="取消"
                onCancel={() => {
                    if (!deleteMutation.isPending) {
                        setDeleteTarget(null);
                    }
                }}
                onOk={deleteStorages}
            />
        </>
    );
};
