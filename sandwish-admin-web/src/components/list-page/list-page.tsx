import type { ReactNode } from "react";
import { useState } from "react";
import { FilterOutlined, PlusOutlined, SearchOutlined } from "@ant-design/icons";
import { Button, Input, Space } from "antd";
import { SandwishBatchActionBar } from "@/components/sandwish-batch-action-bar";
import { SandwishFilterPanel } from "@/components/sandwish-filter-panel";
import { SandwishPage } from "@/components/sandwish-page";
import { SandwishTable } from "@/components/sandwish-table";
import type { SandwishTableProps } from "@/components/sandwish-table";
import "./list-page.css";

export interface ListPageFilterState {
    closeFilter: () => void;
    filterOpen: boolean;
    openFilter: () => void;
    toggleFilter: () => void;
}

export interface ListPageProps<RecordType extends object = object> extends Omit<
    SandwishTableProps<RecordType>,
    "title"
> {
    batchActions?: ReactNode;
    batchClassName?: string;
    addText?: ReactNode;
    defaultFilterOpen?: boolean;
    enableAdd?: boolean;
    description?: ReactNode;
    enableFilter?: boolean;
    enableSearch?: boolean;
    eyebrow?: ReactNode;
    filterActive?: boolean;
    filter?: ReactNode | ((filterState: ListPageFilterState) => ReactNode);
    filterClassName?: string;
    filterText?: ReactNode;
    filterOpen?: boolean;
    onAdd?: () => void;
    onFilterOpenChange?: (open: boolean) => void;
    onSearchChange?: (value: string) => void;
    pageActions?: ReactNode | ((filterState: ListPageFilterState) => ReactNode);
    pageClassName?: string;
    searchPlaceholder?: string;
    searchShortcut?: ReactNode;
    searchValue?: string;
    selectedCount?: number;
    subjectName?: string;
    tableAside?: ReactNode;
    tableAsidePlacement?: "left" | "right";
    title: ReactNode;
}

export const ListPage = <RecordType extends object = object>({
    batchActions,
    batchClassName,
    addText,
    defaultFilterOpen = false,
    enableAdd = false,
    description,
    enableFilter = false,
    enableSearch = false,
    eyebrow,
    filterActive = false,
    filter,
    filterClassName,
    filterText = "筛选",
    filterOpen,
    onAdd,
    onFilterOpenChange,
    onSearchChange,
    pageActions,
    pageClassName,
    searchPlaceholder,
    searchShortcut,
    searchValue = "",
    selectedCount = 0,
    subjectName,
    tableAside,
    tableAsidePlacement = "right",
    title,
    ...tableProps
}: ListPageProps<RecordType>) => {
    const [internalFilterOpen, setInternalFilterOpen] = useState(defaultFilterOpen);
    const actualFilterOpen = enableFilter ? (filterOpen ?? internalFilterOpen) : false;
    const setFilterOpen = (open: boolean) => {
        if (filterOpen === undefined) {
            setInternalFilterOpen(open);
        }
        onFilterOpenChange?.(open);
    };
    const filterState: ListPageFilterState = {
        closeFilter: () => setFilterOpen(false),
        filterOpen: actualFilterOpen,
        openFilter: () => setFilterOpen(true),
        toggleFilter: () => setFilterOpen(!actualFilterOpen)
    };
    const resolvedPageActions =
        typeof pageActions === "function" ? pageActions(filterState) : pageActions;
    const resolvedFilter = typeof filter === "function" ? filter(filterState) : filter;
    const resolvedSearchPlaceholder =
        searchPlaceholder ?? (subjectName ? `搜索${subjectName}...` : "搜索...");
    const resolvedAddText = addText ?? (subjectName ? `新增${subjectName}` : undefined);
    const headerActions = (
        <Space className="list-page-actions">
            {enableSearch ? (
                <Input
                    allowClear
                    className={[
                        "list-page-search",
                        actualFilterOpen ? "list-page-search-hidden" : ""
                    ]
                        .filter(Boolean)
                        .join(" ")}
                    placeholder={resolvedSearchPlaceholder}
                    prefix={<SearchOutlined />}
                    suffix={
                        searchShortcut ? (
                            <span className="list-page-search-shortcut">{searchShortcut}</span>
                        ) : null
                    }
                    value={searchValue}
                    onChange={(event) => onSearchChange?.(event.target.value)}
                />
            ) : null}
            {enableFilter ? (
                <Button
                    className={
                        actualFilterOpen || filterActive
                            ? "list-page-filter-toggle-active"
                            : undefined
                    }
                    icon={<FilterOutlined />}
                    aria-expanded={actualFilterOpen}
                    onClick={filterState.toggleFilter}
                >
                    {filterText}
                </Button>
            ) : null}
            {enableAdd && resolvedAddText ? (
                <Button type="primary" icon={<PlusOutlined />} onClick={onAdd}>
                    {resolvedAddText}
                </Button>
            ) : null}
            {resolvedPageActions}
        </Space>
    );

    return (
        <SandwishPage
            actions={headerActions}
            className={pageClassName}
            description={description}
            eyebrow={eyebrow}
            title={title}
        >
            {enableFilter && resolvedFilter ? (
                <SandwishFilterPanel open={actualFilterOpen} className={filterClassName}>
                    {resolvedFilter}
                </SandwishFilterPanel>
            ) : null}

            {batchActions ? (
                <SandwishBatchActionBar
                    actions={batchActions}
                    className={batchClassName}
                    selectedCount={selectedCount}
                />
            ) : null}

            {tableAside ? (
                <div
                    className={[
                        "list-page-table-area",
                        `list-page-table-area-aside-${tableAsidePlacement}`
                    ].join(" ")}
                >
                    <div className="list-page-table-main">
                        <SandwishTable<RecordType> {...tableProps} />
                    </div>
                    <aside className="list-page-table-aside">{tableAside}</aside>
                </div>
            ) : (
                <SandwishTable<RecordType> {...tableProps} />
            )}
        </SandwishPage>
    );
};
