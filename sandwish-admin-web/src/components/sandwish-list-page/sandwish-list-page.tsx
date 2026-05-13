import type { ReactNode } from "react";
import { SandwishBatchActionBar } from "@/components/sandwish-batch-action-bar";
import { SandwishFilterPanel } from "@/components/sandwish-filter-panel";
import { SandwishPage } from "@/components/sandwish-page";
import { SandwishTable } from "@/components/sandwish-table";
import type { SandwishTableProps } from "@/components/sandwish-table";

export interface SandwishListPageProps<RecordType extends object = object>
    extends Omit<SandwishTableProps<RecordType>, "title"> {
    batchActions?: ReactNode;
    batchClassName?: string;
    description?: ReactNode;
    eyebrow?: ReactNode;
    filter?: ReactNode;
    filterClassName?: string;
    filterOpen?: boolean;
    pageActions?: ReactNode;
    pageClassName?: string;
    selectedCount?: number;
    title: ReactNode;
}

export const SandwishListPage = <RecordType extends object = object>({
    batchActions,
    batchClassName,
    description,
    eyebrow,
    filter,
    filterClassName,
    filterOpen = false,
    pageActions,
    pageClassName,
    selectedCount = 0,
    title,
    ...tableProps
}: SandwishListPageProps<RecordType>) => {
    return (
        <SandwishPage
            actions={pageActions}
            className={pageClassName}
            description={description}
            eyebrow={eyebrow}
            title={title}
        >
            {filter ? (
                <SandwishFilterPanel open={filterOpen} className={filterClassName}>
                    {filter}
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
