import { QueryClientProvider } from "@tanstack/react-query";
import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import { queryClient } from "./query/query-client";
import { AppRouter } from "./router";

const App = () => {
    return (
        <ConfigProvider
            locale={zhCN}
            theme={{
                token: {
                    colorPrimary: "#315c54",
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
