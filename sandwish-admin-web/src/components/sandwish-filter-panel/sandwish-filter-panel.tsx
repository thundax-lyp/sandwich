import type { ReactNode } from "react";
import { Button, Col, Row } from "antd";
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
            <Row className="sandwish-filter-panel-fields" gutter={[12, 12]} align="bottom">
                {fields.map((field) => (
                    <Col
                        className="sandwish-filter-panel-field"
                        key={field.name}
                        xs={24}
                        sm={12}
                        lg={6}
                    >
                        <div className="sandwish-filter-panel-label">{field.label}</div>
                        <div className="sandwish-filter-panel-control">{field.render()}</div>
                    </Col>
                ))}
                <Col
                    className="sandwish-filter-panel-field sandwish-filter-panel-action-field"
                    xs={24}
                    sm={12}
                    lg={6}
                >
                    <div className="sandwish-filter-panel-actions">
                        <Button disabled={resetDisabled} onClick={onReset}>
                            重置
                        </Button>
                        <Button type="primary" onClick={onApply}>
                            查询
                        </Button>
                    </div>
                </Col>
            </Row>
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
