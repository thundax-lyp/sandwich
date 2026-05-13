import type { ReactNode } from "react";
import { useState } from "react";
import { SandwishBatchActionBar } from "@/components/sandwish-batch-action-bar";
import { SandwishFilterPanel } from "@/components/sandwish-filter-panel";
import { SandwishPage } from "@/components/sandwish-page";
import { SandwishTable } from "@/components/sandwish-table";
import type { SandwishTableProps } from "@/components/sandwish-table";

export interface SandwishListPageFilterState {
    closeFilter: () => void;
    filterOpen: boolean;
    openFilter: () => void;
    toggleFilter: () => void;
}

export interface SandwishListPageProps<RecordType extends object = object>
    extends Omit<SandwishTableProps<RecordType>, "title"> {
    batchActions?: ReactNode;
    batchClassName?: string;
    defaultFilterOpen?: boolean;
    description?: ReactNode;
    enableFilter?: boolean;
    eyebrow?: ReactNode;
    filter?: ReactNode | ((filterState: SandwishListPageFilterState) => ReactNode);
    filterClassName?: string;
    filterOpen?: boolean;
    onFilterOpenChange?: (open: boolean) => void;
    pageActions?: ReactNode | ((filterState: SandwishListPageFilterState) => ReactNode);
    pageClassName?: string;
    selectedCount?: number;
    title: ReactNode;
}

export const SandwishListPage = <RecordType extends object = object>({
    batchActions,
    batchClassName,
    defaultFilterOpen = false,
    description,
    enableFilter = false,
    eyebrow,
    filter,
    filterClassName,
    filterOpen,
    onFilterOpenChange,
    pageActions,
    pageClassName,
    selectedCount = 0,
    title,
    ...tableProps
}: SandwishListPageProps<RecordType>) => {
    const [internalFilterOpen, setInternalFilterOpen] = useState(defaultFilterOpen);
    const actualFilterOpen = enableFilter ? filterOpen ?? internalFilterOpen : false;
    const setFilterOpen = (open: boolean) => {
        if (filterOpen === undefined) {
            setInternalFilterOpen(open);
        }
        onFilterOpenChange?.(open);
    };
    const filterState: SandwishListPageFilterState = {
        closeFilter: () => setFilterOpen(false),
        filterOpen: actualFilterOpen,
        openFilter: () => setFilterOpen(true),
        toggleFilter: () => setFilterOpen(!actualFilterOpen)
    };
    const resolvedPageActions =
        typeof pageActions === "function" ? pageActions(filterState) : pageActions;
    const resolvedFilter =
        typeof filter === "function" ? filter(filterState) : filter;

    return (
        <SandwishPage
            actions={resolvedPageActions}
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

            <SandwishTable<RecordType> {...tableProps} />
        </SandwishPage>
    );
};
