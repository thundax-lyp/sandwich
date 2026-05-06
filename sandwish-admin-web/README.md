# sandwish-admin-web

Sandwich 后台管理前端工程，对接 `sandwish-admin-api`。

## 技术栈

- React
- Vite
- TypeScript
- Ant Design
- @ant-design/icons

## 开发命令

```bash
npm install
npm run dev
```

默认开发服务端口为 `5173`，后台接口代理目标通过 `.env` 中的 `VITE_ADMIN_API_BASE_URL` 配置。

## 目录说明

```text
src/
  App.tsx              后台管理端应用壳
  main.tsx             React 入口
  assets/              全局样式等静态资源
  components/          页面组件
```
