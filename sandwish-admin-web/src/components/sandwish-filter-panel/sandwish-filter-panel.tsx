import type { ReactNode } from "react";

export interface SandwishFilterPanelProps {
    children: ReactNode;
    className?: string;
    open: boolean;
}

export const SandwishFilterPanel = ({ children, className, open }: SandwishFilterPanelProps) => {
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
            {children}
        </div>
    );
};
