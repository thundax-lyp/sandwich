# Sandwich Smoke Scripts

本目录提供真实环境冒烟脚本。脚本只验证 API 入口、统一响应、认证公开接口和可选的登录态接口，不绑定 staging 环境。

## Usage

复制环境变量样例：

```bash
cp scripts/smoke/.env.example .env.smoke
```

按目标环境修改 `.env.smoke` 后执行：

```bash
scripts/smoke/smoke-all.sh
```

也可以只执行单个边界：

```bash
scripts/smoke/smoke-auth.sh
scripts/smoke/smoke-admin-api.sh
scripts/smoke/smoke-front-api.sh
scripts/smoke/smoke-storage.sh
```

## Environment

- `SANDWICH_ADMIN_BASE_URL`: 后台 API 基础地址，默认 `http://127.0.0.1:18080/admin-api`
- `SANDWICH_FRONT_BASE_URL`: 前台 API 基础地址，默认 `http://127.0.0.1:18080/front-api`
- `SANDWICH_SMOKE_ACCESS_TOKEN`: 通用访问 token
- `SANDWICH_SMOKE_ADMIN_TOKEN`: 后台访问 token，优先级高于通用 token
- `SANDWICH_SMOKE_REQUIRE_AUTH`: `true` 时，缺少 token 直接失败；默认缺少 token 时跳过认证态检查
- `SANDWICH_SMOKE_STORAGE_UPLOAD`: `true` 时执行上传冒烟；默认关闭，因为上传会写入对象和数据库记录
- `SANDWICH_SMOKE_STORAGE_FILE`: 上传冒烟使用的本地文件路径

## Boundaries

- `smoke-auth.sh`: 调用后台公开认证表单接口，验证应用和认证公开链路可用。
- `smoke-admin-api.sh`: 调用后台公开接口；有 token 时继续验证当前用户接口。
- `smoke-front-api.sh`: 调用前台登录状态接口，验证前台入口可用。
- `smoke-storage.sh`: 有 token 时验证存储只读接口；上传冒烟必须显式开启。

脚本不提交真实 token、密码、生产连接串或环境专用配置。
