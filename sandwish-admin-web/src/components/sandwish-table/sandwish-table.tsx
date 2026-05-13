import type { Key, MouseEvent as ReactMouseEvent, ReactNode } from "react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Table } from "antd";
import type { TableProps } from "antd";

const DEFAULT_ACTION_COLUMN_KEY = "actions";
const DEFAULT_ACTION_COLUMN_WIDTH = 116;
const DEFAULT_ACTION_COLUMN_MOBILE_WIDTH = 54;
const DEFAULT_MIN_COLUMN_WIDTH = 96;
const MOBILE_MEDIA_QUERY = "(max-width: 760px)";

const readColumnKey = <RecordType extends object>(
    column: NonNullable<TableProps<RecordType>["columns"]>[number]
): Key | undefined => {
    if ("key" in column && column.key !== undefined) {
        return column.key;
    }

    if ("dataIndex" in column) {
        const dataIndex = column.dataIndex;
        if (Array.isArray(dataIndex)) {
            return dataIndex.join(".");
        }
        if (typeof dataIndex === "string" || typeof dataIndex === "number") {
            return dataIndex;
        }
    }

    return undefined;
};

const readNumericWidth = (width: unknown) => {
    return typeof width === "number" && Number.isFinite(width) ? width : undefined;
};

const sumColumnWidths = <RecordType extends object>(
    columns: NonNullable<TableProps<RecordType>["columns"]>
): number => {
    return columns.reduce((total, column) => {
        if ("children" in column && Array.isArray(column.children)) {
            return total + sumColumnWidths(column.children);
        }

        return total + (readNumericWidth(column.width) ?? 0);
    }, 0);
};

export interface SandwishTableProps<RecordType extends object = object>
    extends TableProps<RecordType> {
    actionColumnKey?: Key;
    actionColumnMobileWidth?: number;
    actionColumnWidth?: number;
    minColumnWidth?: number;
    resizableColumns?: boolean;
    responsive?: boolean;
    sortable?: boolean;
}

export const SandwishTable = <RecordType extends object = object>({
    actionColumnKey = DEFAULT_ACTION_COLUMN_KEY,
    actionColumnMobileWidth = DEFAULT_ACTION_COLUMN_MOBILE_WIDTH,
    actionColumnWidth = DEFAULT_ACTION_COLUMN_WIDTH,
    className,
    columns,
    minColumnWidth = DEFAULT_MIN_COLUMN_WIDTH,
    resizableColumns = true,
    responsive = true,
    rowSelection,
    scroll,
    sortable = false,
    ...tableProps
}: SandwishTableProps<RecordType>) => {
    const [columnWidths, setColumnWidths] = useState<Record<string, number>>({});
    const [isMobile, setIsMobile] = useState(false);

    useEffect(() => {
        if (!responsive || typeof window.matchMedia !== "function") {
            return undefined;
        }

        const mediaQueryList = window.matchMedia(MOBILE_MEDIA_QUERY);
        const updateMobile = () => setIsMobile(mediaQueryList.matches);

        updateMobile();
        mediaQueryList.addEventListener("change", updateMobile);
        return () => mediaQueryList.removeEventListener("change", updateMobile);
    }, [responsive]);

    const startResizeColumn = useCallback((columnKey: Key, startWidth: number) => (event: ReactMouseEvent) => {
        event.preventDefault();
        event.stopPropagation();

        const widthKey = String(columnKey);
        const startX = event.clientX;

        const resizeColumn = (moveEvent: MouseEvent) => {
            const nextWidth = Math.max(minColumnWidth, startWidth + moveEvent.clientX - startX);
            setColumnWidths((currentWidths) => ({
                ...currentWidths,
                [widthKey]: nextWidth
            }));
        };

        const stopResizeColumn = () => {
            document.removeEventListener("mousemove", resizeColumn);
            document.removeEventListener("mouseup", stopResizeColumn);
        };

        document.addEventListener("mousemove", resizeColumn);
        document.addEventListener("mouseup", stopResizeColumn);
    }, [minColumnWidth]);

    const normalizedColumns = useMemo(() => {
        if (!columns) {
            return columns;
        }

        const normalizeColumns = (
            currentColumns: NonNullable<TableProps<RecordType>["columns"]>
        ): NonNullable<TableProps<RecordType>["columns"]> => {
            return currentColumns.map((column) => {
                if ("children" in column && Array.isArray(column.children)) {
                    return {
                        ...column,
                        children: normalizeColumns(column.children)
                    };
                }

                const columnKey = readColumnKey(column);
                const isActionColumn = columnKey === actionColumnKey;
                const widthKey = columnKey === undefined ? undefined : String(columnKey);
                const baseWidth = isActionColumn
                    ? isMobile
                        ? actionColumnMobileWidth
                        : readNumericWidth(column.width) ?? actionColumnWidth
                    : readNumericWidth(column.width);
                const currentWidth =
                    widthKey && columnWidths[widthKey] !== undefined
                        ? columnWidths[widthKey]
                        : baseWidth;
                const plainTitle =
                    typeof column.title === "function" ? undefined : (column.title as ReactNode);
                const canResize =
                    resizableColumns &&
                    !isActionColumn &&
                    columnKey !== undefined &&
                    currentWidth !== undefined &&
                    typeof column.title !== "function";
                const titleNode = canResize ? (
                    <span className="sandwish-table-column-title">
                        {plainTitle}
                        <span
                            aria-hidden="true"
                            className="sandwish-table-column-resize-handle"
                            onMouseDown={startResizeColumn(columnKey, currentWidth)}
                        />
                    </span>
                ) : (
                    column.title
                );

                return {
                    ...column,
                    className: [
                        column.className,
                        isActionColumn ? "sandwish-table-action-column" : ""
                    ]
                        .filter(Boolean)
                        .join(" ") || undefined,
                    fixed: isActionColumn ? column.fixed ?? "right" : column.fixed,
                    title: titleNode,
                    width: currentWidth ?? column.width
                };
            });
        };

        return normalizeColumns(columns);
    }, [
        actionColumnKey,
        actionColumnMobileWidth,
        actionColumnWidth,
        columnWidths,
        columns,
        isMobile,
        resizableColumns,
        startResizeColumn
    ]);

    const scrollX = useMemo(() => {
        if (scroll?.x !== undefined || !normalizedColumns) {
            return scroll?.x;
        }

        const totalWidth = sumColumnWidths(normalizedColumns);
        return totalWidth > 0 ? totalWidth : undefined;
    }, [normalizedColumns, scroll?.x]);

    return (
        <Table<RecordType>
            {...tableProps}
            className={[
                "sandwish-table",
                sortable ? "sandwish-table-sortable" : "sandwish-table-static",
                rowSelection ? "sandwish-table-selectable" : "",
                className
            ]
                .filter(Boolean)
                .join(" ")}
            columns={normalizedColumns}
            rowSelection={rowSelection}
            scroll={{ ...scroll, x: scrollX }}
        />
    );
};
