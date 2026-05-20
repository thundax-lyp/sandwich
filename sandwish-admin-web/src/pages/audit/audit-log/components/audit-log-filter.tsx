import { GlobalOutlined, IdcardOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input, Select } from "antd";
import type { OptionsRecord } from "@/types/options";
import type { AuditOptionKeys } from "../audit-log-service";

export interface AuditLogFilters {
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

interface AuditLogFilterProps {
    auditOptions?: Partial<OptionsRecord<AuditOptionKeys>>;
    filters: AuditLogFilters;
    hasActiveFilters: boolean;
    loading?: boolean;
    onApply: () => void;
    onChange: (filters: AuditLogFilters) => void;
    onReset: () => void;
}

const optionItems = (options?: OptionsRecord[string]) => [
    { value: "ALL", label: "全部" },
    ...(options || []).map((option) => ({
        value: option.value,
        label: option.label || option.value
    }))
];

export const AuditLogFilter = ({
    auditOptions,
    filters,
    hasActiveFilters,
    loading = false,
    onApply,
    onChange,
    onReset
}: AuditLogFilterProps) => {
    const updateFilters = (values: Partial<AuditLogFilters>) => {
        onChange({ ...filters, ...values });
    };

    return (
        <div className="audit-log-filter-form">
            <label>
                <span>对象类型</span>
                <Select
                    value={filters.objectType}
                    options={optionItems(auditOptions?.objectTypes)}
                    loading={loading}
                    onChange={(objectType) => updateFilters({ objectType })}
                />
            </label>
            <label>
                <span>动作</span>
                <Select
                    value={filters.action}
                    options={optionItems(auditOptions?.actions)}
                    loading={loading}
                    onChange={(action) => updateFilters({ action })}
                />
            </label>
            <label>
                <span>操作者类型</span>
                <Select
                    value={filters.operatorType}
                    options={optionItems(auditOptions?.operatorTypes)}
                    loading={loading}
                    onChange={(operatorType) => updateFilters({ operatorType })}
                />
            </label>
            <label>
                <span>对象 ID</span>
                <Input
                    allowClear
                    prefix={<IdcardOutlined />}
                    value={filters.objectId}
                    onChange={(event) => updateFilters({ objectId: event.target.value })}
                />
            </label>
            <label>
                <span>操作者 ID</span>
                <Input
                    allowClear
                    value={filters.operatorId}
                    onChange={(event) => updateFilters({ operatorId: event.target.value })}
                />
            </label>
            <label>
                <span>来源</span>
                <Input
                    allowClear
                    prefix={<GlobalOutlined />}
                    value={filters.source}
                    onChange={(event) => updateFilters({ source: event.target.value })}
                />
            </label>
            <label>
                <span>请求 ID</span>
                <Input
                    allowClear
                    value={filters.requestId}
                    onChange={(event) => updateFilters({ requestId: event.target.value })}
                />
            </label>
            <label>
                <span>开始时间</span>
                <Input
                    allowClear
                    placeholder="2026-05-19 00:00:00"
                    value={filters.beginDate}
                    onChange={(event) => updateFilters({ beginDate: event.target.value })}
                />
            </label>
            <label>
                <span>结束时间</span>
                <Input
                    allowClear
                    placeholder="2026-05-19 23:59:59"
                    value={filters.endDate}
                    onChange={(event) => updateFilters({ endDate: event.target.value })}
                />
            </label>
            <Button onClick={onReset} disabled={!hasActiveFilters}>
                重置
            </Button>
            <Button className="audit-log-filter-search" icon={<SearchOutlined />} onClick={onApply}>
                查询
            </Button>
        </div>
    );
};
