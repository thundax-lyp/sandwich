# Sandwich Smoke Scripts

本目录提供真实环境冒烟脚本。脚本按 Docker / nginx 挂载的运行入口验证 API context-path、统一响应、公开认证入口和可选认证态接口，不绑定 staging 环境。

## Usage

复制环境变量样例：

```bash
cp .env.test.example .env.smoke
```

按目标环境修改 `.env.smoke` 后执行：

```bash
scripts/smoke/smoke-all.sh
```

也可以只执行单个边界：

```bash
scripts/smoke/smoke-admin-api.sh
scripts/smoke/smoke-front-api.sh
scripts/smoke/smoke-open-api.sh
scripts/smoke/smoke-api-surface.sh
```

多实例缓存同步验证固定使用 A/B/C 三个实例地址：

```bash
scripts/smoke/smoke-admin-api-cache-sync.sh
scripts/smoke/smoke-front-api-cache-sync.sh
scripts/smoke/smoke-open-api-cache-sync.sh
scripts/smoke/smoke-cache-sync-all.sh
```

## Environment

- `SANDWICH_PUBLIC_BASE_URL`: nginx 对外基础地址，默认 `http://127.0.0.1:18080`
- `SANDWICH_ADMIN_BASE_URL`: 后台 API 基础地址，默认 `http://127.0.0.1:18080/admin-api`
- `SANDWICH_FRONT_BASE_URL`: 前台 API 基础地址，默认 `http://127.0.0.1:18080/front-api`
- `SANDWICH_OPEN_BASE_URL`: 开放 API 基础地址，默认 `http://127.0.0.1:18080/open-api`
- `SANDWICH_OPEN_CONTEXT_PATH`: 开放 API context-path，默认 `/open-api`，用于签名 canonical path
- `SANDWICH_SMOKE_ACCESS_TOKEN`: 通用访问 token
- `SANDWICH_SMOKE_ADMIN_TOKEN`: 后台访问 token，优先级高于通用 token
- `SANDWICH_SMOKE_REQUIRE_AUTH`: `true` 时，缺少 token 直接失败；默认缺少 token 时跳过认证态检查
- `SANDWICH_SMOKE_FRONT_ACCESS_TOKEN`: 前台访问 token，供前台认证态缓存同步夹具使用
- `SANDWICH_SMOKE_FRONT_REFRESH_TOKEN`: 前台 refresh token，供前台 refresh 缓存同步夹具使用
- `SANDWICH_SMOKE_STORAGE_UPLOAD`: `true` 时执行上传冒烟；默认关闭，因为上传会写入对象和数据库记录
- `SANDWICH_SMOKE_STORAGE_FILE`: 上传冒烟使用的本地文件路径
- `SANDWICH_SMOKE_OPEN_API_KEY`: Open API 签名冒烟使用的 API key
- `SANDWICH_SMOKE_OPEN_API_SECRET`: Open API 签名冒烟使用的 API secret 明文
- `SANDWICH_SMOKE_REQUIRE_OPEN_API`: `true` 时，缺少 Open API key/secret 直接失败；默认缺少时只验证未签名请求边界
- `SANDWICH_ADMIN_API_A_BASE_URL` / `SANDWICH_ADMIN_API_B_BASE_URL` / `SANDWICH_ADMIN_API_C_BASE_URL`: 后台 API 三实例缓存同步验证地址
- `SANDWICH_FRONT_API_A_BASE_URL` / `SANDWICH_FRONT_API_B_BASE_URL` / `SANDWICH_FRONT_API_C_BASE_URL`: 前台 API 三实例缓存同步验证地址
- `SANDWICH_OPEN_API_A_BASE_URL` / `SANDWICH_OPEN_API_B_BASE_URL` / `SANDWICH_OPEN_API_C_BASE_URL`: 开放 API 三实例缓存同步验证地址
- `SANDWICH_CACHE_SYNC_USER_ID`: 后台用户 by-id 缓存同步夹具
- `SANDWICH_CACHE_SYNC_ROLE_ID`: 后台角色 by-id 缓存同步夹具
- `SANDWICH_CACHE_SYNC_MENU_ID`: 后台菜单 by-id 缓存同步夹具
- `SANDWICH_CACHE_SYNC_DEPARTMENT_ID`: 后台部门 by-id 缓存同步夹具
- `SANDWICH_CACHE_SYNC_DICT_ID`: 后台字典 by-id 缓存同步夹具
- `SANDWICH_CACHE_SYNC_STORAGE_OBJECT_ID`: 后台存储对象 by-id 缓存同步夹具

## Boundaries

- `smoke-admin-api.sh`: 调用 `/admin-api/api/auth/session/pre-auth-session` 验证后台挂载；有 token 时继续验证当前用户、菜单、权限、字典分页和存储对象树接口。上传冒烟必须显式开启。
- `smoke-front-api.sh`: 调用 `/front-api/api/auth/session/pre-auth-session` 验证前台挂载，并验证登录状态接口。
- `smoke-open-api.sh`: 调用 `/open-api/api/submission/submission/page` 验证开放接口挂载；默认未签名请求应返回 401，有 Open API key/secret 时继续验证签名请求。
- `smoke-api-surface.sh`: 点火所有 Controller URL，验证 Docker / nginx context-path、security filter 和 request mapping 可达；使用空请求或无效参数避免真实 create / update / delete 写入数据。
- `smoke-admin-api-cache-sync.sh`: 使用 A/B/C 三个后台实例验证预认证会话运行态跨实例可刷新，并按 Cache 矩阵输出 `COVER` / `SKIP`。
- `smoke-front-api-cache-sync.sh`: 使用 A/B/C 三个前台实例验证预认证会话运行态跨实例可刷新，并按 Cache 矩阵输出 `COVER` / `SKIP`。
- `smoke-open-api-cache-sync.sh`: 使用 A/B/C 三个开放接口实例验证 nonce 防重放跨实例生效，并按 Cache 矩阵输出 `COVER` / `SKIP`。
- `smoke-cache-sync-all.sh`: 串行执行三类入口缓存同步验证。

脚本不提交真实 token、密码、生产连接串或环境专用配置。

`scripts/smoke/.env.example` 只保留 smoke 专项变量样例。部署后联动冒烟和压测时，固定优先使用根目录 `.env.test.example`。
