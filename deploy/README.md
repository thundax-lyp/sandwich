# Sandwish Deploy README

本文档用途：人工操作说明。
部署边界以 `docs/00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md` 为准。

## Purpose

本目录提供全局 Docker Compose 部署样例，包含 `sandwish-admin-web`、`sandwish-admin-api`、`sandwish-front-api`、`sandwish-open-api` 和基础设施。

## Topology

包含服务：

- `nginx`
- `sandwish-admin-api`
- `sandwish-front-api`
- `sandwish-open-api`
- `mysql`
- `redis`
- `rocketmq-namesrv`
- `rocketmq-broker`
- `minio`
- `minio-init`

流量路径：

- 后台页面：`client -> nginx -> sandwish-admin-web static files`
- 后台 API：`client -> nginx -> sandwish-admin-api`
- 前台 API：`client -> nginx -> sandwish-front-api`
- 开放 API：`client -> nginx -> sandwish-open-api`
- 前台页面：`client -> nginx -> front static files`

## Build

```bash
mvn -q -pl sandwish-admin-api,sandwish-front-api,sandwish-open-api -am -DskipTests package
cd sandwish-admin-web && npm ci && npm run build
```

产物路径：

- `sandwish-admin-api/target/sandwish-admin-api.jar`
- `sandwish-front-api/target/sandwish-front-api.jar`
- `sandwish-open-api/target/sandwish-open-api.jar`
- `sandwish-admin-web/dist`
- `sandwish/k6:dev` 压测运行镜像

一键构建 API jar、admin-web dist、本地 Docker 镜像，并导出镜像文件：

```bash
SANDWISH_IMAGE_TAG=dev deploy/build-images.sh
```

只构建并导出 k6 压测镜像：

```bash
SANDWISH_IMAGE_TAG=dev deploy/build-k6-image.sh
```

默认镜像名：

- `sandwish/admin-api:dev`
- `sandwish/front-api:dev`
- `sandwish/open-api:dev`
- `sandwish/nginx:dev`
- `sandwish/k6:dev`

k6 镜像文件固定导出为：

- `sandwish-k6-dev.tar`

脚本还会把基础设施镜像打成 `sandwish/*` 名称并导出：

- `sandwish/mysql:8.4`
- `sandwish/redis:7.4-alpine`
- `sandwish/rocketmq:5.4.0`
- `sandwish/minio:RELEASE.2025-02-28T09-55-16Z`
- `sandwish/minio-mc:RELEASE.2025-02-21T16-00-46Z`

默认上游来源仍分别为 `mysql:8.4`、`redis:7.4-alpine`、`apache/rocketmq:5.4.0`、`minio/minio:RELEASE.2025-02-28T09-55-16Z` 和 `minio/mc:RELEASE.2025-02-21T16-00-46Z`，可通过 `SANDWISH_*_SOURCE_IMAGE` 覆盖。

镜像文件默认输出到 `deploy/image-files/`，可用 `SANDWISH_IMAGE_OUTPUT_DIR` 覆盖。目标机器导入镜像：

```bash
for image in deploy/image-files/*.tar; do docker load -i "$image"; done
```

如果当前机器已经有基础设施镜像，或外网 registry 不稳定，可以跳过拉取：

```bash
SANDWISH_PULL_INFRA_IMAGES=false deploy/build-images.sh
```

## Start

复制环境变量样例：

```bash
cp deploy/.env.example deploy/.env
```

本地私密配置可以使用：

```bash
cp deploy/.env.example deploy/.env.dev
```

启动：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml up -d
```

使用本地私密配置启动：

```bash
docker compose --env-file deploy/.env.dev -f deploy/docker-compose.yml up -d
```

Compose 会等待 MySQL、Redis、MinIO 和 RocketMQ healthcheck 通过后再启动 `sandwish-admin-api`，等待 MySQL、Redis、MinIO healthcheck 通过后再启动 `sandwish-front-api` 和 `sandwish-open-api`，避免 API 容器早于基础设施可用状态启动。

MySQL 数据通过 `SANDWISH_MYSQL_DATA_PATH` 挂载到部署机器本地目录，默认路径为 `deploy/data/mysql`，便于人工备份和排查。

停止：

```bash
docker compose --env-file deploy/.env -f deploy/docker-compose.yml down
```

## Access

- 后台 API：`http://127.0.0.1:18080/admin-api`
- 后台页面：`http://127.0.0.1:18080/admin/`
- 前台 API：`http://127.0.0.1:18080/front-api`
- 开放 API：`http://127.0.0.1:18080/open-api`
- 前台页面：`http://127.0.0.1:18080/`
- MinIO API：`http://127.0.0.1:19000`
- MinIO Console：`http://127.0.0.1:19001`
- RocketMQ NameServer：`127.0.0.1:19876`
- RocketMQ Broker：`127.0.0.1:20911`

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
7. `db/schema/submission.sql`
8. `db/data/submission.sql`
9. `db/schema/open.sql`
10. `db/data/open.sql`
11. `db/schema/member.sql`
12. `db/data/member.sql`
13. `db/schema/audit.sql`
14. `db/data/audit.sql`

初始化只在 `SANDWISH_MYSQL_DATA_PATH` 对应的数据目录为空时自动执行。若需要重新初始化本地数据，先停止服务并清空该目录。

初始化数据中的数据库主键使用固定雪花 ID，业务键保持对应业务值。

## Default Account

- 默认部门：`GitHub`
- 默认用户：`developer`
- 默认密码：部署默认密码，首次登录后应立即修改

## Storage Backend

对象存储运行配置使用 `SANDWISH_OSS_TYPE`：

- `local`：使用本地文件存储，对应 `SANDWISH_OSS_LOCAL_*`
- `s3`：使用 S3 API 存储，对应 `SANDWISH_OSS_S3_*`

业务数据库中的 `storage_type` 字段仍写入领域枚举值：`LOCAL_FILE` 或 `OSS`。

Compose 默认使用 MinIO S3 模式。MinIO 数据通过 `SANDWISH_MINIO_DATA_PATH` 挂载到部署机器本地目录，默认路径为 `deploy/data/minio`，便于人工备份。`minio-init` 会在启动时创建 `SANDWISH_OSS_S3_BUCKET` 指定的 bucket。

MinIO Server 和 MinIO Client 使用 GNU AGPLv3 许可证。当前 Compose 中的 MinIO 仅作为开发、测试和内部部署的 S3 兼容对象存储示例。若用于对外商业交付、SaaS 或其他可能触发 AGPLv3 义务的场景，应自行完成许可合规评估、购买商业许可，或通过 `SANDWISH_OSS_S3_ENDPOINT` 替换为外部 S3 兼容存储服务。

Compose 默认使用 RocketMQ 作为后台系统日志消息通道。RocketMQ 数据通过 `SANDWISH_ROCKETMQ_*_PATH` 挂载到部署机器本地目录，默认路径为 `deploy/data/rocketmq`。

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

开放接口入口：

```bash
set -a; . ./dev.env; set +a
SERVER_PORT=20010 SERVER_SERVLET_CONTEXT_PATH=/open-api java -jar sandwish-open-api/target/sandwish-open-api.jar
```

Compose 部署样例：

- `deploy/.env.example`

关键变量：

- `SANDWISH_ADMIN_API_IMAGE`
- `SANDWISH_FRONT_API_IMAGE`
- `SANDWISH_OPEN_API_IMAGE`
- `SANDWISH_NGINX_RUNTIME_IMAGE`
- `SANDWISH_MYSQL_IMAGE`
- `SANDWISH_REDIS_IMAGE`
- `SANDWISH_ROCKETMQ_IMAGE`
- `SANDWISH_MINIO_IMAGE`
- `SANDWISH_MINIO_MC_IMAGE`
- `SANDWISH_DB_URL`
- `SANDWISH_DB_USERNAME`
- `SANDWISH_DB_PASSWORD`
- `SANDWISH_REDIS_URI`
- `SANDWISH_MQ_TYPE`
- `SANDWISH_ROCKETMQ_NAME_SERVER`
- `SANDWISH_LOG_STORAGE_PATH`
- `SANDWISH_OSS_TYPE`
- `SANDWISH_OSS_S3_ENDPOINT`
- `SANDWISH_OSS_S3_BUCKET`

## Troubleshooting

- API 容器启动失败，先检查 jar 路径是否指向已打包产物。
- 登录或业务接口报库表不存在，先确认 MySQL volume 是否为首次初始化，或手动按 `db/AGENT.md` 顺序导入脚本。
- 上传对象失败，先检查 MinIO endpoint、bucket、access key、secret key。
