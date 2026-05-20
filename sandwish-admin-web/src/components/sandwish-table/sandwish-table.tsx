import type {
    DragEvent as ReactDragEvent,
    Key,
    MouseEvent as ReactMouseEvent,
    ReactNode
} from "react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { MoreOutlined } from "@ant-design/icons";
import { Button, Dropdown, Table } from "antd";
import type { MenuProps, TableProps } from "antd";
import { PAGE_SIZE_OPTIONS } from "@/types/page";
import "./sandwish-table.css";

const DEFAULT_ACTION_COLUMN_KEY = "actions";
const DEFAULT_ACTION_COLUMN_WIDTH = 116;
const DEFAULT_ACTION_COLUMN_MOBILE_WIDTH = 54;
const ACTION_BUTTON_WIDTH = 24;
const ACTION_CELL_GAP = 0;
const ACTION_CELL_PADDING = 20;
const ACTION_DIVIDER_WIDTH = 5;
const ACTION_INLINE_LIMIT = 2;
const ACTION_TEXT_BASE_WIDTH = 10;
const ACTION_TEXT_CHAR_WIDTH = 14;
const DEFAULT_MIN_COLUMN_WIDTH = 96;
const MOBILE_MEDIA_QUERY = "(max-width: 760px)";

export type SandwishTableSortPosition = "before" | "after";
export type SandwishTableRowActionType = "text" | "warning" | "danger";

export interface SandwishTableRowAction<RecordType extends object = object> {
    ariaLabel?: string;
    disabled?: boolean;
    icon?: ReactNode;
    key: Key;
    onClick: (record: RecordType) => void;
    text: string;
    type?: SandwishTableRowActionType;
}

export interface SandwishTableRowActionDivider {
    key?: Key;
    type: "divider";
}

export type SandwishTableRowActionOption<RecordType extends object = object> =
    | SandwishTableRowAction<RecordType>
    | SandwishTableRowActionDivider;

export type SandwishTableRowActions<RecordType extends object = object> =
    | SandwishTableRowActionOption<RecordType>[]
    | ((record: RecordType, index?: number) => SandwishTableRowActionOption<RecordType>[]);

export interface SandwishTableActionColumn<RecordType extends object = object> {
    inlineLimit?: number;
    options?: SandwishTableRowActions<RecordType>;
}

export type SandwishTableColumn<RecordType extends object = object> = NonNullable<
    TableProps<RecordType>["columns"]
>[number] &
    SandwishTableActionColumn<RecordType>;

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

const isActionDivider = <RecordType extends object>(
    action: SandwishTableRowActionOption<RecordType>
): action is SandwishTableRowActionDivider => {
    return "type" in action && action.type === "divider";
};

const normalizeRowActions = <RecordType extends object>(
    actions: SandwishTableRowActions<RecordType> | undefined,
    record: RecordType,
    index?: number
) => {
    return typeof actions === "function" ? actions(record, index) : (actions ?? []);
};

const countActionButtons = <RecordType extends object>(
    actions: SandwishTableRowActionOption<RecordType>[]
) => {
    return actions.filter((action) => !isActionDivider(action)).length;
};

const calculateActionButtonWidth = <RecordType extends object>(
    action: SandwishTableRowActionOption<RecordType>
) => {
    if (isActionDivider(action)) {
        return ACTION_DIVIDER_WIDTH;
    }

    if (action.icon) {
        return ACTION_BUTTON_WIDTH;
    }

    return Math.max(
        ACTION_BUTTON_WIDTH,
        action.text.length * ACTION_TEXT_CHAR_WIDTH + ACTION_TEXT_BASE_WIDTH
    );
};

const calculateActionColumnWidth = <RecordType extends object>(
    actions: SandwishTableRowActionOption<RecordType>[]
) => {
    const actionCount = countActionButtons(actions);
    if (actionCount === 0) {
        return DEFAULT_ACTION_COLUMN_MOBILE_WIDTH;
    }

    const { inlineActions, overflowActions } = splitActions(actions, ACTION_INLINE_LIMIT);
    const inlineWidth = inlineActions.reduce(
        (total, action) => total + calculateActionButtonWidth(action),
        0
    );
    const hasOverflow = actionCount > ACTION_INLINE_LIMIT;
    const itemCount = inlineActions.length + (overflowActions.length > 0 ? 1 : 0);

    return (
        ACTION_CELL_PADDING +
        inlineWidth +
        (hasOverflow ? ACTION_BUTTON_WIDTH : 0) +
        Math.max(0, itemCount - 1) * ACTION_CELL_GAP
    );
};

const splitActions = <RecordType extends object>(
    actions: SandwishTableRowActionOption<RecordType>[],
    inlineLimit: number
) => {
    const inlineActions: SandwishTableRowActionOption<RecordType>[] = [];
    const overflowActions: SandwishTableRowActionOption<RecordType>[] = [];
    let actionCount = 0;

    actions.forEach((action) => {
        if (isActionDivider(action)) {
            if (actionCount > 0 && actionCount < inlineLimit) {
                inlineActions.push(action);
                return;
            }
            if (actionCount >= inlineLimit) {
                overflowActions.push(action);
            }
            return;
        }

        if (actionCount < inlineLimit) {
            inlineActions.push(action);
        } else {
            overflowActions.push(action);
        }
        actionCount += 1;
    });

    return { inlineActions, overflowActions };
};

export interface SandwishTableProps<RecordType extends object = object> extends Omit<
    TableProps<RecordType>,
    "columns"
> {
    actionColumnKey?: Key;
    actionColumnMobileWidth?: number;
    actionColumnWidth?: number;
    columns?: SandwishTableColumn<RecordType>[];
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
    pagination,
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

    const renderRowActions = useCallback(
        (
            actionsConfig: SandwishTableRowActions<RecordType>,
            inlineLimitConfig: number | undefined,
            record: RecordType,
            index: number
        ) => {
            const actions = normalizeRowActions(actionsConfig, record, index);
            const inlineLimit = isMobile ? 0 : (inlineLimitConfig ?? ACTION_INLINE_LIMIT);
            const actionCount = countActionButtons(actions);
            const { inlineActions, overflowActions } = splitActions(actions, inlineLimit);

            if (actionCount === 0) {
                return null;
            }

            const menuItems: MenuProps["items"] = overflowActions.map((action, actionIndex) => {
                if (isActionDivider(action)) {
                    return {
                        type: "divider",
                        key: action.key ?? `divider-${actionIndex}`
                    };
                }

                return {
                    key: action.key,
                    className:
                        action.type === "warning" ? "sandwish-table-row-action-menu-warning" : "",
                    danger: action.type === "danger",
                    disabled: action.disabled,
                    icon: action.icon,
                    label: action.text
                };
            });

            return (
                <div className="sandwish-table-row-actions">
                    <span className="sandwish-table-row-actions-inline">
                        {inlineActions.map((action, actionIndex) =>
                            isActionDivider(action) ? (
                                <span
                                    aria-hidden="true"
                                    className="sandwish-table-row-action-divider"
                                    key={action.key ?? `divider-${actionIndex}`}
                                />
                            ) : (
                                <Button
                                    aria-label={action.ariaLabel ?? action.text}
                                    className={[
                                        "sandwish-table-row-action",
                                        action.type === "warning"
                                            ? "sandwish-table-row-action-warning"
                                            : ""
                                    ]
                                        .filter(Boolean)
                                        .join(" ")}
                                    danger={action.type === "danger"}
                                    disabled={action.disabled}
                                    icon={action.icon}
                                    key={action.key}
                                    type="text"
                                    onClick={() => action.onClick(record)}
                                >
                                    {action.icon ? null : action.text}
                                </Button>
                            )
                        )}
                        {overflowActions.length > 0 ? (
                            <Dropdown
                                menu={{
                                    items: menuItems,
                                    onClick: ({ key }) => {
                                        const action = overflowActions.find(
                                            (item) =>
                                                !isActionDivider(item) &&
                                                String(item.key) === String(key)
                                        );
                                        if (action && !isActionDivider(action)) {
                                            action.onClick(record);
                                        }
                                    }
                                }}
                                trigger={["click"]}
                            >
                                <Button
                                    aria-label="展开行操作"
                                    className="sandwish-table-row-action"
                                    icon={<MoreOutlined />}
                                    type="text"
                                />
                            </Dropdown>
                        ) : null}
                    </span>
                </div>
            );
        },
        [isMobile]
    );

    const calculateColumnActionWidth = useCallback(
        (actionsConfig: SandwishTableRowActions<RecordType> | undefined) => {
            if (!actionsConfig) {
                return actionColumnWidth;
            }

            if (Array.isArray(actionsConfig)) {
                return calculateActionColumnWidth(actionsConfig);
            }

            const dataSource = tableProps.dataSource ?? [];
            const widths = dataSource.map((record, index) =>
                calculateActionColumnWidth(normalizeRowActions(actionsConfig, record, index))
            );
            return widths.length > 0 ? Math.max(...widths) : actionColumnWidth;
        },
        [actionColumnWidth, tableProps.dataSource]
    );

    const normalizedColumns = useMemo(() => {
        if (!columns) {
            return columns;
        }

        const normalizeColumns = (
            currentColumns: SandwishTableColumn<RecordType>[]
        ): SandwishTableColumn<RecordType>[] => {
            return currentColumns.map((column) => {
                if ("children" in column && Array.isArray(column.children)) {
                    return {
                        ...column,
                        children: normalizeColumns(column.children)
                    };
                }

                const columnKey = readColumnKey(column);
                const isActionColumn = columnKey === actionColumnKey;
                const actionOptions = isActionColumn ? column.options : undefined;
                const widthKey = columnKey === undefined ? undefined : String(columnKey);
                const actionWidth = isMobile
                    ? actionColumnMobileWidth
                    : (readNumericWidth(column.width) ?? calculateColumnActionWidth(actionOptions));
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
                const defaultTitle = isActionColumn ? (column.title ?? "操作") : column.title;
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
                    defaultTitle
                );

                return {
                    ...column,
                    className:
                        [column.className, isActionColumn ? "sandwish-table-action-column" : ""]
                            .filter(Boolean)
                            .join(" ") || undefined,
                    fixed: isActionColumn ? (column.fixed ?? "right") : column.fixed,
                    render:
                        isActionColumn && !column.render && actionOptions
                            ? (_value: unknown, record: RecordType, index: number) =>
                                  renderRowActions(actionOptions, column.inlineLimit, record, index)
                            : column.render,
                    title: titleNode,
                    width: currentWidth ?? column.width
                };
            });
        };

        return normalizeColumns(columns);
    }, [
        actionColumnKey,
        actionColumnMobileWidth,
        calculateColumnActionWidth,
        columnWidths,
        columns,
        isMobile,
        renderRowActions,
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

    const mergedPagination = useMemo(() => {
        if (pagination === false || !pagination) {
            return pagination;
        }

        return {
            showSizeChanger: true,
            pageSizeOptions: PAGE_SIZE_OPTIONS,
            ...pagination
        };
    }, [pagination]);

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
            pagination={mergedPagination}
            rowKey={rowKey}
            rowSelection={rowSelection}
            scroll={{ ...scroll, x: scrollX }}
        />
    );
};
