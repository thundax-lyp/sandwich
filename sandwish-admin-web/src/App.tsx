import { ConfigProvider } from "antd";
import zhCN from "antd/locale/zh_CN";
import { AppRouter } from "./router";

export default function App() {
    return (
        <ConfigProvider
            locale={zhCN}
            theme={{
                token: {
                    colorPrimary: "#315c54",
                    borderRadius: 8,
                    fontFamily: "\"Avenir Next\", \"PingFang SC\", \"Microsoft YaHei\", sans-serif"
                }
            }}
        >
            <AppRouter />
        </ConfigProvider>
    );
}
