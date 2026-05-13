import { Drawer } from "antd";
import type { DrawerProps } from "antd";
import "./sandwish-drawer.css";

export type SandwishDrawerSize = "full" | "large" | "middle" | "small";

export interface SandwishDrawerProps extends Omit<DrawerProps, "size" | "width"> {
    size?: SandwishDrawerSize;
}

export const SandwishDrawer = ({
    className,
    placement = "right",
    size = "small",
    ...drawerProps
}: SandwishDrawerProps) => {
    return (
        <Drawer
            {...drawerProps}
            className={[
                "sandwish-drawer",
                `sandwish-drawer-${size}`,
                className
            ]
                .filter(Boolean)
                .join(" ")}
            placement={placement}
            width={`var(--sandwish-drawer-${size}-width)`}
        />
    );
};
