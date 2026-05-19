import type {
    DragEvent as ReactDragEvent,
    Key,
    MouseEvent as ReactMouseEvent,
    ReactNode
} from "react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Table } from "antd";
import type { TableProps } from "antd";
import "./sandwish-table.css";

const DEFAULT_ACTION_COLUMN_KEY = "actions";
const DEFAULT_ACTION_COLUMN_WIDTH = 116;
const DEFAULT_ACTION_COLUMN_MOBILE_WIDTH = 54;
const DEFAULT_MIN_COLUMN_WIDTH = 96;
const MOBILE_MEDIA_QUERY = "(max-width: 760px)";

export type SandwishTableSortPosition = "before" | "after";

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

const callHandler = <EventType,>(
    handler: ((event: EventType) => void) | undefined,
    event: EventType
) => {
    if (handler) {
        handler(event);
    }
};

export interface SandwishTableProps<
    RecordType extends object = object
> extends TableProps<RecordType> {
    actionColumnKey?: Key;
    actionColumnMobileWidth?: number;
    actionColumnWidth?: number;
    getSortableRowKey?: (record: RecordType, index?: number) => Key;
    minColumnWidth?: number;
    onSort?: (
        sourceRecord: RecordType,
        targetRecord: RecordType,
        position: SandwishTableSortPosition
    ) => void;
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
    getSortableRowKey,
    minColumnWidth = DEFAULT_MIN_COLUMN_WIDTH,
    onRow,
    onSort,
    resizableColumns = true,
    responsive = true,
    rowKey,
    rowSelection,
    scroll,
    sortable = false,
    ...tableProps
}: SandwishTableProps<RecordType>) => {
    const [columnWidths, setColumnWidths] = useState<Record<string, number>>({});
    const [isMobile, setIsMobile] = useState(false);
    const [draggingRecord, setDraggingRecord] = useState<RecordType | null>(null);
    const [dropTarget, setDropTarget] = useState<{
        position: SandwishTableSortPosition;
        rowKey: Key;
    } | null>(null);
    const sortableEnabled = sortable && Boolean(onSort);

    const readRowKey = useCallback(
        (record: RecordType, index?: number): Key | undefined => {
            if (getSortableRowKey) {
                return getSortableRowKey(record, index);
            }

            if (typeof rowKey === "function") {
                return rowKey(record, index);
            }

            if (typeof rowKey === "string") {
                return record[rowKey as keyof RecordType] as Key | undefined;
            }

            if ("key" in record) {
                return record.key as Key | undefined;
            }

            return undefined;
        },
        [getSortableRowKey, rowKey]
    );

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

    const startResizeColumn = useCallback(
        (columnKey: Key, startWidth: number) => (event: ReactMouseEvent) => {
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
        },
        [minColumnWidth]
    );

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
                const actionWidth = isMobile
                    ? actionColumnMobileWidth
                    : (readNumericWidth(column.width) ?? actionColumnWidth);
                const baseWidth = isActionColumn ? actionWidth : readNumericWidth(column.width);
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
                    className:
                        [column.className, isActionColumn ? "sandwish-table-action-column" : ""]
                            .filter(Boolean)
                            .join(" ") || undefined,
                    fixed: isActionColumn ? (column.fixed ?? "right") : column.fixed,
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

    const readDropPosition = useCallback(
        (event: ReactDragEvent<HTMLElement>): SandwishTableSortPosition => {
            const rowRect = event.currentTarget.getBoundingClientRect();
            return event.clientY < rowRect.top + rowRect.height / 2 ? "before" : "after";
        },
        []
    );

    const mergedOnRow = useCallback<NonNullable<TableProps<RecordType>["onRow"]>>(
        (record, index) => {
            const rowProps = onRow ? onRow(record, index) : {};

            if (!sortableEnabled) {
                return rowProps;
            }

            const currentRowKey = readRowKey(record, index);
            const isDropTarget =
                currentRowKey !== undefined && dropTarget?.rowKey === currentRowKey;
            const sortableClassName = isDropTarget
                ? `sandwish-table-row-drop-${dropTarget.position}`
                : "";

            return {
                ...rowProps,
                className:
                    [rowProps.className, sortableClassName].filter(Boolean).join(" ") || undefined,
                draggable: true,
                onDragEnd: (event) => {
                    callHandler(rowProps.onDragEnd, event);
                    setDraggingRecord(null);
                    setDropTarget(null);
                },
                onDragEnter: (event) => {
                    callHandler(rowProps.onDragEnter, event);
                    if (
                        !draggingRecord ||
                        draggingRecord === record ||
                        currentRowKey === undefined
                    ) {
                        return;
                    }
                    setDropTarget({ rowKey: currentRowKey, position: "before" });
                },
                onDragOver: (event) => {
                    callHandler(rowProps.onDragOver, event);
                    if (
                        !draggingRecord ||
                        draggingRecord === record ||
                        currentRowKey === undefined
                    ) {
                        return;
                    }

                    event.preventDefault();
                    event.dataTransfer.dropEffect = "move";
                    setDropTarget({ rowKey: currentRowKey, position: readDropPosition(event) });
                },
                onDragLeave: (event) => {
                    callHandler(rowProps.onDragLeave, event);
                    if (currentRowKey !== undefined && dropTarget?.rowKey === currentRowKey) {
                        setDropTarget(null);
                    }
                },
                onDragStart: (event) => {
                    callHandler(rowProps.onDragStart, event);
                    const sourceRowKey = readRowKey(record, index);
                    setDraggingRecord(record);
                    event.dataTransfer.effectAllowed = "move";
                    if (sourceRowKey !== undefined) {
                        event.dataTransfer.setData("text/plain", String(sourceRowKey));
                    }
                },
                onDrop: (event) => {
                    callHandler(rowProps.onDrop, event);
                    if (!draggingRecord || draggingRecord === record) {
                        return;
                    }

                    event.preventDefault();
                    onSort?.(draggingRecord, record, readDropPosition(event));
                    setDraggingRecord(null);
                    setDropTarget(null);
                }
            };
        },
        [draggingRecord, dropTarget, onRow, onSort, readDropPosition, readRowKey, sortableEnabled]
    );

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
            onRow={mergedOnRow}
            rowKey={rowKey}
            rowSelection={rowSelection}
            scroll={{ ...scroll, x: scrollX }}
        />
    );
};
