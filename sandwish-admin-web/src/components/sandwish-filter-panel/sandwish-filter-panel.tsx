import type { ReactNode } from "react";
import { Button } from "antd";
import "./sandwish-filter-panel.css";

export interface SandwishFilterPanelField {
    label: ReactNode;
    name: string;
    render: () => ReactNode;
}

export interface SandwishFilterPanelProps {
    children?: ReactNode;
    className?: string;
    fields?: SandwishFilterPanelField[];
    onApply?: () => void;
    onReset?: () => void;
    open: boolean;
    resetDisabled?: boolean;
}

export const SandwishFilterPanel = ({
    children,
    className,
    fields,
    onApply,
    onReset,
    open,
    resetDisabled = false
}: SandwishFilterPanelProps) => {
    const structuredContent = fields?.length ? (
        <div className="sandwish-filter-panel-form">
            <div className="sandwish-filter-panel-fields">
                {fields.map((field) => (
                    <div className="sandwish-filter-panel-field" key={field.name}>
                        <div className="sandwish-filter-panel-label">{field.label}</div>
                        <div className="sandwish-filter-panel-control">{field.render()}</div>
                    </div>
                ))}
                <div className="sandwish-filter-panel-field sandwish-filter-panel-action-field">
                    <div className="sandwish-filter-panel-actions">
                        <Button disabled={resetDisabled} onClick={onReset}>
                            重置
                        </Button>
                        <Button type="primary" onClick={onApply}>
                            查询
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    ) : null;

    return (
        <div
            className={[
                "sandwish-filter-panel",
                open ? "sandwish-filter-panel-open" : "",
                className
            ]
                .filter(Boolean)
                .join(" ")}
        >
            {children ?? structuredContent}
        </div>
    );
};
