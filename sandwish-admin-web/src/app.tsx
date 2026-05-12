import { QueryClientProvider } from "@tanstack/react-query";
import { ConfigProvider, theme as antdTheme } from "antd";
import zhCN from "antd/locale/zh_CN";
import { useEffect, useState } from "react";
import { queryClient } from "./query/query-client";
import { AppRouter } from "./router";
import { getStoredTheme, subscribeAdminThemeChange } from "./theme/theme-storage";

const App = () => {
    const [themeName, setThemeName] = useState<"light" | "dark">(getStoredTheme);

    useEffect(() => {
        const syncTheme = () => setThemeName(getStoredTheme());
        return subscribeAdminThemeChange(syncTheme);
    }, []);

    useEffect(() => {
        document.documentElement.dataset.theme = themeName;
    }, [themeName]);

    return (
        <ConfigProvider
            locale={zhCN}
            theme={{
                algorithm: themeName === "dark" ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
                token: {
                    colorPrimary: themeName === "dark" ? "#d7f171" : "#171717",
                    colorBgContainer: themeName === "dark" ? "#171717" : "#ffffff",
                    colorBorder: themeName === "dark" ? "#31312d" : "#deded8",
                    colorText: themeName === "dark" ? "#f5f0e8" : "#171717",
                    colorTextSecondary: themeName === "dark" ? "#a8a59d" : "#66635e",
                    borderRadius: 8,
                    fontFamily: '"Avenir Next", "PingFang SC", "Microsoft YaHei", sans-serif'
                }
            }}
        >
            <QueryClientProvider client={queryClient}>
                <AppRouter />
            </QueryClientProvider>
        </ConfigProvider>
    );
};

export default App;
