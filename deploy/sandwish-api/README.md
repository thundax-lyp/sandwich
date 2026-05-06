# Sandwish API Deploy README

本文档用途：人工操作说明。
部署边界以 `docs/00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md` 为准。

## Purpose

本目录提供 `sandwish-admin-api` 和 `sandwish-front-api` 的 Docker Compose 部署样例。

## Topology

包含服务：

- `nginx`
- `sandwish-admin-api`
- `sandwish-front-api`
- `mysql`
- `redis`
- `minio`

流量路径：

- 后台 API：`client -> nginx -> sandwish-admin-api`
- 前台 API：`client -> nginx -> sandwish-front-api`

## Build

```bash
mvn -q -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

产物路径：

- `sandwish-admin-api/target/sandwish-admin-api.jar`
- `sandwish-front-api/target/sandwish-front-api.jar`

## Start

复制环境变量样例：

```bash
cp deploy/sandwish-api/.env.example deploy/sandwish-api/.env
```

本地私密配置可以使用：

```bash
cp deploy/sandwish-api/.env.example deploy/sandwish-api/.env.dev
```

启动：

```bash
docker compose --env-file deploy/sandwish-api/.env -f deploy/sandwish-api/docker-compose.yml up -d
```

使用本地私密配置启动：

```bash
docker compose --env-file deploy/sandwish-api/.env.dev -f deploy/sandwish-api/docker-compose.yml up -d
```

停止：

```bash
docker compose --env-file deploy/sandwish-api/.env -f deploy/sandwish-api/docker-compose.yml down
```

## Access

- 后台 API：`http://127.0.0.1:18080/admin-api`
- 前台 API：`http://127.0.0.1:18080/front-api`
- MinIO API：`http://127.0.0.1:19000`
- MinIO Console：`http://127.0.0.1:19001`

## Smoke Check

真实环境冒烟脚本见 `scripts/smoke/`。脚本通过环境变量指定 API 地址和访问 token，不绑定 staging 环境。

本地 Compose 启动后可执行：

```bash
scripts/smoke/smoke-all.sh
```

需要认证态检查时，复制 `scripts/smoke/.env.example` 为 `.env.smoke`，填入实际 token 后再执行脚本。

## Data Initialization

MySQL 首次初始化会按以下顺序自动导入数据库脚本：

1. `db/schema/system.sql`
2. `db/data/system.sql`
3. `db/schema/auth.sql`
4. `db/data/auth.sql`
5. `db/schema/storage.sql`
6. `db/data/storage.sql`
7. `db/schema/member.sql`
8. `db/data/member.sql`

初始化只在 `sandwish-mysql-data` volume 为空时自动执行。若需要重新初始化本地数据，先停止服务并删除该 volume。

## Default Account

- 默认部门：`GitHub`
- 默认用户：`developer`
- 默认密码：部署默认密码，首次登录后应立即修改

## Storage Backend

对象存储运行配置使用 `SANDWISH_OSS_TYPE`：

- `local`：使用本地文件存储，对应 `SANDWISH_OSS_LOCAL_*`
- `s3`：使用 S3 API 存储，对应 `SANDWISH_OSS_S3_*`

业务数据库中的 `storage_type` 字段仍写入领域枚举值：`LOCAL_FILE` 或 `OSS`。

## Environment Variables

单应用运行样例：

- `.env.example`

本地私密配置使用根目录 `dev.env`，由 `.env.example` 复制后填写，不提交到 git。`dev.env` 只放 admin/front 共享依赖配置；入口差异通过启动命令指定。

后台入口：

```bash
set -a; . ./dev.env; set +a
SERVER_PORT=20009 SERVER_SERVLET_CONTEXT_PATH=/admin-api java -jar sandwish-admin-api/target/sandwish-admin-api.jar
```

前台入口：

```bash
set -a; . ./dev.env; set +a
SERVER_PORT=20002 SERVER_SERVLET_CONTEXT_PATH=/front-api java -jar sandwish-front-api/target/sandwish-front-api.jar
```

Compose 部署样例：

- `deploy/sandwish-api/.env.example`

关键变量：

- `SANDWISH_ADMIN_API_JAR`
- `SANDWISH_FRONT_API_JAR`
- `SANDWISH_DB_URL`
- `SANDWISH_DB_USERNAME`
- `SANDWISH_DB_PASSWORD`
- `SANDWISH_REDIS_URI`
- `SANDWISH_LOG_STORAGE_PATH`
- `SANDWISH_OSS_TYPE`
- `SANDWISH_OSS_S3_ENDPOINT`
- `SANDWISH_OSS_S3_BUCKET`

## Troubleshooting

- API 容器启动失败，先检查 jar 路径是否指向已打包产物。
- 登录或业务接口报库表不存在，先确认 MySQL volume 是否为首次初始化，或手动按 `db/AGENT.md` 顺序导入脚本。
- 上传对象失败，先检查 MinIO endpoint、bucket、access key、secret key。
