import {
    ClockCircleOutlined,
    EyeOutlined,
    GlobalOutlined,
    IdcardOutlined,
    ReloadOutlined,
    SearchOutlined
} from "@ant-design/icons";
import { useQuery } from "@tanstack/react-query";
import { Avatar, Button, Descriptions, Empty, Input, Select, Space, Tag, Typography } from "antd";
import { useMemo, useState } from "react";
import type { ReactNode } from "react";
import { useCurrentAccessToken } from "@/auth/hooks";
import { toAuthenticatedResourceUrl } from "@/auth/resource-url";
import { ADMIN_API_BASE_URL } from "@/api/http";
import { ListPage } from "@/components/list-page";
import { SandwishDrawer } from "@/components/sandwish-drawer";
import type { SandwishTableProps } from "@/components/sandwish-table";
import { getAuditLogDetail, getAuditOptions, pageAuditLogs } from "./audit-log-service";
import type {
    AuditFieldResponse,
    AuditLogPageRequest,
    AuditLogResponse,
    AuditSnapshotFieldResponse,
    AuditSnapshotResponse
} from "./audit-log-service";
import "./audit-log-page.css";

const { Paragraph, Text } = Typography;

const DEFAULT_PAGE_NO = 1;
const DEFAULT_PAGE_SIZE = 10;

const DEFAULT_COLUMN_WIDTHS = {
    occurredAt: 180,
    object: 260,
    action: 120,
    operator: 180,
    source: 140,
    summary: 260,
    actions: 84
};

const ADMIN_OPERATOR_TYPE = "USER";

interface AuditLogFilters {
    objectType: string;
    objectId: string;
    action: string;
    operatorType: string;
    operatorId: string;
    source: string;
    requestId: string;
    beginDate: string;
    endDate: string;
}

const DEFAULT_AUDIT_LOG_FILTERS: AuditLogFilters = {
    objectType: "ALL",
    objectId: "",
    action: "ALL",
    operatorType: "ALL",
    operatorId: "",
    source: "",
    requestId: "",
    beginDate: "",
    endDate: ""
};

const normalizeSearch = (value?: string | null) => {
    const normalizedValue = value?.trim();
    return normalizedValue || undefined;
};

const readSelectValue = (value: string) => {
    return value === "ALL" ? undefined : value;
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

const readObjectDisplay = (log: AuditLogResponse) => {
    return log.objectDisplayName || log.objectId || "-";
};

const readObjectTypeLabel = (log: AuditLogResponse) => {
    return log.objectTypeLabel || log.objectType || "未知对象";
};

const getInitials = (value?: string | null) => {
    const normalizedValue = value?.trim() || "U";
    return Array.from(normalizedValue.replace(/\s+/g, "")).slice(0, 2).join("");
};

const readOperatorName = (log: AuditLogResponse) => {
    return log.operatorName || log.operatorId || "-";
};

const readOperatorUser = (log: AuditLogResponse) => {
    const name = readOperatorName(log);
    const avatarUrl =
        log.operatorType === ADMIN_OPERATOR_TYPE && log.operatorId
            ? `${ADMIN_API_BASE_URL}/sys/user/avatar?id=${encodeURIComponent(log.operatorId)}`
            : undefined;
    return { avatarUrl, name };
};

const optionItems = (options?: Array<{ value: string; label: string }>) => [
    { value: "ALL", label: "全部" },
    ...(options || []).map((option) => ({
        value: option.value,
        label: option.label || option.value
    }))
];

const renderChangedFields = (fields?: AuditFieldResponse[] | null) => {
    if (!fields?.length) {
        return <Text type="secondary">无字段变更</Text>;
    }

    return (
        <div className="audit-log-field-list">
            {fields.map((field) => (
                <div key={field.fieldName || field.fieldLabel} className="audit-log-field-row">
                    <Text strong>{field.fieldLabel || field.fieldName}</Text>
                    <div className="audit-log-field-values">
                        <Text type="secondary">{field.beforeDisplayValue || "-"}</Text>
                        <span>→</span>
                        <Text>{field.afterDisplayValue || "-"}</Text>
                    </div>
                </div>
            ))}
        </div>
    );
};

const snapshotFieldKey = (field: Pick<AuditSnapshotFieldResponse, "fieldName" | "fieldLabel">) => {
    return field.fieldName || field.fieldLabel || "";
};

const snapshotFieldValue = (field?: AuditSnapshotFieldResponse | null) => {
    return field?.displayValue || "-";
};

const changedFieldKeys = (fields?: AuditFieldResponse[] | null) => {
    const keys = new Set<string>();
    (fields || []).forEach((field) => {
        if (field.fieldName) {
            keys.add(field.fieldName);
        }
        if (field.fieldLabel) {
            keys.add(field.fieldLabel);
        }
    });
    return keys;
};

const snapshotFieldMap = (snapshot?: AuditSnapshotResponse | null) => {
    const fields = new Map<string, AuditSnapshotFieldResponse>();
    (snapshot?.fields || []).forEach((field) => {
        const key = snapshotFieldKey(field);
        if (key) {
            fields.set(key, field);
        }
    });
    return fields;
};

const snapshotFieldKeys = (
    beforeSnapshot?: AuditSnapshotResponse | null,
    afterSnapshot?: AuditSnapshotResponse | null
) => {
    const keys: string[] = [];
    [...(beforeSnapshot?.fields || []), ...(afterSnapshot?.fields || [])].forEach((field) => {
        const key = snapshotFieldKey(field);
        if (key && !keys.includes(key)) {
            keys.push(key);
        }
    });
    return keys;
};

const renderSnapshotCompare = (
    beforeSnapshot?: AuditSnapshotResponse | null,
    afterSnapshot?: AuditSnapshotResponse | null,
    fields?: AuditFieldResponse[] | null
) => {
    const keys = snapshotFieldKeys(beforeSnapshot, afterSnapshot);
    if (!keys.length) {
        return <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无快照" />;
    }
    const beforeFields = snapshotFieldMap(beforeSnapshot);
    const afterFields = snapshotFieldMap(afterSnapshot);
    const changedKeys = changedFieldKeys(fields);

    return (
        <div className="audit-log-snapshot-compare">
            <div className="audit-log-snapshot-head">
                <Text type="secondary">字段</Text>
                <Text type="secondary">变更前</Text>
                <Text type="secondary">变更后</Text>
            </div>
            {keys.map((key) => {
                const beforeField = beforeFields.get(key);
                const afterField = afterFields.get(key);
                const beforeValue = snapshotFieldValue(beforeField);
                const afterValue = snapshotFieldValue(afterField);
                const changed = changedKeys.has(key) || beforeValue !== afterValue;
                return (
                    <div
                        key={key}
                        className={
                            changed
                                ? "audit-log-snapshot-row audit-log-snapshot-row-changed"
                                : "audit-log-snapshot-row"
                        }
                    >
                        <div className="audit-log-snapshot-field">
                            <Text strong={changed}>
                                {beforeField?.fieldLabel || afterField?.fieldLabel || key}
                            </Text>
                            {changed ? <Tag className="audit-log-changed-tag">已变更</Tag> : null}
                        </div>
                        <Text type="secondary">{beforeValue}</Text>
                        <Text strong={changed}>{afterValue}</Text>
                    </div>
                );
            })}
        </div>
    );
};

const renderOperator = (
    log: AuditLogResponse,
    accessToken: string | null,
    nameNode?: ReactNode
) => {
    const user = readOperatorUser(log);
    const avatarUrl = toAuthenticatedResourceUrl(user.avatarUrl, accessToken);
    return (
        <Space size={8} className="audit-log-operator-cell">
            <Avatar size={28} src={avatarUrl}>
                {getInitials(user.name)}
            </Avatar>
            {nameNode || <Text ellipsis>{user.name}</Text>}
        </Space>
    );
};

export const AuditLogPage = () => {
    const accessToken = useCurrentAccessToken();
    const [query, setQuery] = useState<AuditLogPageRequest>({
        pageNo: DEFAULT_PAGE_NO,
        pageSize: DEFAULT_PAGE_SIZE
    });
    const [searchText, setSearchText] = useState("");
    const [filters, setFilters] = useState<AuditLogFilters>(DEFAULT_AUDIT_LOG_FILTERS);
    const [detailLogId, setDetailLogId] = useState<string | null>(null);
    const hasActiveFilters = Boolean(
        filters.objectType !== "ALL" ||
        filters.objectId.trim() ||
        filters.action !== "ALL" ||
        filters.operatorType !== "ALL" ||
        filters.operatorId.trim() ||
        filters.source.trim() ||
        filters.requestId.trim() ||
        filters.beginDate.trim() ||
        filters.endDate.trim()
    );

    const auditOptionsQuery = useQuery({
        queryKey: ["audit-log", "options"],
        queryFn: getAuditOptions,
        retry: false
    });
    const auditLogQuery = useQuery({
        queryKey: ["audit-log", "page", query],
        queryFn: () => pageAuditLogs(query),
        retry: false
    });
    const detailQuery = useQuery({
        queryKey: ["audit-log", "detail", detailLogId],
        queryFn: () => getAuditLogDetail(detailLogId || ""),
        enabled: Boolean(detailLogId),
        retry: false
    });

    const auditLogPage = auditLogQuery.data;
    const auditLogs = useMemo(() => auditLogPage?.records || [], [auditLogPage?.records]);
    const totalCount = auditLogPage?.count ?? auditLogPage?.totalCount ?? 0;
    const currentPageNo = auditLogPage?.pageNo || query.pageNo || DEFAULT_PAGE_NO;
    const currentPageSize = auditLogPage?.pageSize || query.pageSize || DEFAULT_PAGE_SIZE;
    const auditOptions = auditOptionsQuery.data;

    const updateQuery = (values: Partial<AuditLogPageRequest>) => {
        setQuery((currentQuery) => {
            const nextQuery = { ...currentQuery, ...values };
            return {
                objectType: nextQuery.objectType,
                objectId: nextQuery.objectId,
                action: nextQuery.action,
                operatorType: nextQuery.operatorType,
                operatorId: nextQuery.operatorId,
                source: nextQuery.source,
                requestId: nextQuery.requestId,
                beginDate: nextQuery.beginDate,
                endDate: nextQuery.endDate,
                pageNo: values.pageNo || DEFAULT_PAGE_NO,
                pageSize: values.pageSize || currentQuery.pageSize || DEFAULT_PAGE_SIZE
            };
        });
    };

    const searchObjectId = (value: string) => {
        setSearchText(value);
        updateQuery({ objectId: normalizeSearch(value) });
    };

    const applyFilters = (closeFilter: () => void) => {
        updateQuery({
            objectType: readSelectValue(filters.objectType),
            objectId: normalizeSearch(filters.objectId || searchText),
            action: readSelectValue(filters.action),
            operatorType: readSelectValue(filters.operatorType),
            operatorId: normalizeSearch(filters.operatorId),
            source: normalizeSearch(filters.source),
            requestId: normalizeSearch(filters.requestId),
            beginDate: normalizeSearch(filters.beginDate),
            endDate: normalizeSearch(filters.endDate)
        });
        closeFilter();
    };

    const resetFilters = () => {
        setFilters(DEFAULT_AUDIT_LOG_FILTERS);
        setSearchText("");
        updateQuery({
            objectType: undefined,
            objectId: undefined,
            action: undefined,
            operatorType: undefined,
            operatorId: undefined,
            source: undefined,
            requestId: undefined,
            beginDate: undefined,
            endDate: undefined
        });
    };

    const columns: SandwishTableProps<AuditLogResponse>["columns"] = [
        {
            title: "时间",
            dataIndex: "occurredAt",
            key: "occurredAt",
            width: DEFAULT_COLUMN_WIDTHS.occurredAt,
            render: (occurredAt?: string | null) => (
                <Space size={7}>
                    <ClockCircleOutlined className="audit-log-time-icon" />
                    <span>{formatDateTime(occurredAt)}</span>
                </Space>
            )
        },
        {
            title: "对象",
            key: "object",
            width: DEFAULT_COLUMN_WIDTHS.object,
            ellipsis: true,
            render: (_, log) => (
                <div className="audit-log-object-cell">
                    <Text strong>{readObjectDisplay(log)}</Text>
                    <Space size={6} wrap>
                        <Tag className="audit-log-object-tag">{readObjectTypeLabel(log)}</Tag>
                        {log.version ? <Tag>v{log.version}</Tag> : null}
                    </Space>
                </div>
            )
        },
        {
            title: "动作",
            dataIndex: "actionLabel",
            key: "action",
            width: DEFAULT_COLUMN_WIDTHS.action,
            render: (_, log) => (
                <Tag className="audit-log-action-tag">{log.actionLabel || log.action || "-"}</Tag>
            )
        },
        {
            title: "操作者",
            key: "operator",
            width: DEFAULT_COLUMN_WIDTHS.operator,
            ellipsis: true,
            render: (_, log) => renderOperator(log, accessToken)
        },
        {
            title: "来源",
            dataIndex: "source",
            key: "source",
            width: DEFAULT_COLUMN_WIDTHS.source,
            render: (source?: string | null) => source || "-"
        },
        {
            title: "摘要",
            dataIndex: "summary",
            key: "summary",
            width: DEFAULT_COLUMN_WIDTHS.summary,
            ellipsis: true,
            render: (summary?: string | null) => summary || "-"
        },
        {
            title: "操作",
            key: "actions",
            width: DEFAULT_COLUMN_WIDTHS.actions,
            render: (_, log) => (
                <Button
                    aria-label={`查看审计日志 ${log.id}`}
                    className="sandwish-table-row-action"
                    icon={<EyeOutlined />}
                    type="text"
                    onClick={() => setDetailLogId(log.id)}
                />
            )
        }
    ];

    const detailLog = detailQuery.data;

    return (
        <>
            <ListPage<AuditLogResponse>
                pageClassName="audit-log-page"
                title="审计日志"
                description="查看关键业务对象的变更记录、操作者和字段差异。"
                subjectName="审计日志"
                enableFilter
                enableSearch
                searchShortcut="⌘K"
                searchValue={searchText}
                searchPlaceholder="搜索对象 ID..."
                onSearchChange={searchObjectId}
                filterActive={hasActiveFilters}
                filterClassName="audit-log-filter-panel"
                filter={({ closeFilter }) => (
                    <div className="audit-log-filter-form">
                        <label>
                            <span>对象类型</span>
                            <Select
                                value={filters.objectType}
                                options={optionItems(auditOptions?.objectTypes)}
                                loading={auditOptionsQuery.isFetching}
                                onChange={(objectType) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        objectType
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>动作</span>
                            <Select
                                value={filters.action}
                                options={optionItems(auditOptions?.actions)}
                                loading={auditOptionsQuery.isFetching}
                                onChange={(action) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        action
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>操作者类型</span>
                            <Select
                                value={filters.operatorType}
                                options={optionItems(auditOptions?.operatorTypes)}
                                loading={auditOptionsQuery.isFetching}
                                onChange={(operatorType) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        operatorType
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>对象 ID</span>
                            <Input
                                allowClear
                                prefix={<IdcardOutlined />}
                                value={filters.objectId}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        objectId: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>操作者 ID</span>
                            <Input
                                allowClear
                                value={filters.operatorId}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        operatorId: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>来源</span>
                            <Input
                                allowClear
                                prefix={<GlobalOutlined />}
                                value={filters.source}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        source: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>请求 ID</span>
                            <Input
                                allowClear
                                value={filters.requestId}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        requestId: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>开始时间</span>
                            <Input
                                allowClear
                                placeholder="2026-05-19 00:00:00"
                                value={filters.beginDate}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        beginDate: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <label>
                            <span>结束时间</span>
                            <Input
                                allowClear
                                placeholder="2026-05-19 23:59:59"
                                value={filters.endDate}
                                onChange={(event) =>
                                    setFilters((currentFilters) => ({
                                        ...currentFilters,
                                        endDate: event.target.value
                                    }))
                                }
                            />
                        </label>
                        <Button onClick={resetFilters} disabled={!hasActiveFilters}>
                            重置
                        </Button>
                        <Button
                            className="audit-log-filter-search"
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
                        loading={auditLogQuery.isFetching}
                        onClick={() => auditLogQuery.refetch()}
                    >
                        刷新
                    </Button>
                }
                rowKey="id"
                className="audit-log-table"
                columns={columns}
                dataSource={auditLogs}
                loading={auditLogQuery.isFetching}
                scroll={{ x: 1320 }}
                pagination={{
                    current: currentPageNo,
                    pageSize: currentPageSize,
                    total: totalCount,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    onChange: (pageNo, pageSize) => updateQuery({ pageNo, pageSize })
                }}
                locale={{
                    emptyText: auditLogQuery.isError
                        ? "审计日志加载失败，请确认权限和接口状态。"
                        : "暂无审计日志"
                }}
            />

            <SandwishDrawer
                title="审计详情"
                open={Boolean(detailLogId)}
                size="large"
                loading={detailQuery.isFetching}
                onClose={() => setDetailLogId(null)}
            >
                {detailLog ? (
                    <div className="audit-log-detail">
                        <Descriptions column={2} size="small" bordered>
                            <Descriptions.Item label="对象">
                                {detailLog.objectDisplayName || detailLog.objectId || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="对象类型">
                                {detailLog.objectTypeLabel || detailLog.objectType || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="动作">
                                {detailLog.actionLabel || detailLog.action || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="版本">
                                {detailLog.version ?? "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="操作者">
                                {renderOperator(
                                    detailLog,
                                    accessToken,
                                    <span>{readOperatorName(detailLog)}</span>
                                )}
                            </Descriptions.Item>
                            <Descriptions.Item label="操作者类型">
                                {detailLog.operatorTypeLabel || detailLog.operatorType || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="来源">
                                {detailLog.source || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="远端地址">
                                {detailLog.remoteAddr || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="请求 ID">
                                {detailLog.requestId || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="链路 ID">
                                {detailLog.traceId || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="发生时间">
                                {formatDateTime(detailLog.occurredAt)}
                            </Descriptions.Item>
                            <Descriptions.Item label="幂等键">
                                {detailLog.idempotencyKey || "-"}
                            </Descriptions.Item>
                        </Descriptions>

                        <section>
                            <Text type="secondary">摘要</Text>
                            <Paragraph>{detailLog.summary || "-"}</Paragraph>
                        </section>

                        <section>
                            <Text type="secondary">字段变更</Text>
                            {renderChangedFields(detailLog.changedFields)}
                        </section>

                        <section>
                            <Text type="secondary">快照对比</Text>
                            {renderSnapshotCompare(
                                detailLog.beforeSnapshot,
                                detailLog.afterSnapshot,
                                detailLog.changedFields
                            )}
                        </section>
                    </div>
                ) : detailQuery.isError ? (
                    <Empty description="审计详情加载失败" />
                ) : null}
            </SandwishDrawer>
        </>
    );
};
