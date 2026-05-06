import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), "");
    const adminApiBaseUrl = env.VITE_ADMIN_API_BASE_URL || "http://localhost:20009";

    return {
        plugins: [react()],
        server: {
            port: 5173,
            proxy: {
                "/admin-api": {
                    target: adminApiBaseUrl,
                    changeOrigin: true
                }
            }
        }
    };
});
