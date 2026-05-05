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

- `sandwish-admin-api/target/interaction-admin-api.jar`
- `sandwish-front-api/target/hudong.jar`

## Start

复制环境变量样例：

```bash
cp deploy/sandwish-api/.env.example deploy/sandwish-api/.env
```

启动：

```bash
docker compose --env-file deploy/sandwish-api/.env -f deploy/sandwish-api/docker-compose.yml up -d
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

## Data Initialization

MySQL 首次初始化会按以下顺序自动导入数据库脚本：

1. `db/schema/system.sql`
2. `db/data/system.sql`
3. `db/schema/auth.sql`
4. `db/data/auth.sql`
5. `db/schema/storage.sql`
6. `db/data/storage.sql`

初始化只在 `sandwish-mysql-data` volume 为空时自动执行。若需要重新初始化本地数据，先停止服务并删除该 volume。

## Default Account

- 默认部门：`GitHub`
- 默认用户：`developer`
- 默认密码：部署默认密码，首次登录后应立即修改

## Environment Variables

关键变量：

- `SANDWISH_ADMIN_API_JAR`
- `SANDWISH_FRONT_API_JAR`
- `SANDWISH_DB_URL`
- `SANDWISH_DB_USERNAME`
- `SANDWISH_DB_PASSWORD`
- `SANDWISH_REDIS_URI`
- `SANDWISH_OSS_TYPE`
- `SANDWISH_OSS_S3_ENDPOINT`
- `SANDWISH_OSS_S3_BUCKET`

## Troubleshooting

- API 容器启动失败，先检查 jar 路径是否指向已打包产物。
- 登录或业务接口报库表不存在，先确认 MySQL volume 是否为首次初始化，或手动按 `db/AGENT.md` 顺序导入脚本。
- 上传对象失败，先检查 MinIO endpoint、bucket、access key、secret key。
