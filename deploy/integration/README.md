# Sandwich Integration Test Environment

本文档用途：说明集成测试 Docker 环境、数据初始化、测试运行、清理和排错入口。

设计和阶段拆解以 `docs/30-designs/RUNBOOK-INTEGRATION-TEST.md` 为准。正式部署样例仍以 `deploy/README.md` 和 `deploy/docker-compose.yml` 为准。

## Purpose

`deploy/integration/` 固定服务于后端集成测试。

集成测试通过真实 HTTP 入口访问 admin、front、open 三个 API 应用，并验证 `Controller -> Service -> DAO/Mapper -> Database` 主链路。

## Directory Map

目标目录：

```text
deploy/integration/
  README.md
  docker-compose.yml
  .env.example
  db/
    README.md
    00-schema/
    10-baseline/
    20-scenarios/
    90-cleanup/
```

目录职责：

- `docker-compose.yml`：启动集成测试专用 MySQL、Redis、RocketMQ。
- `.env.example`：提供集成测试环境变量样例。
- `db/00-schema/`：装载正式 schema 的集成测试入口。
- `db/10-baseline/`：所有集成测试共享的基础数据。
- `db/20-scenarios/`：按业务域拆分的场景数据。
- `db/90-cleanup/`：可重复执行的数据清理脚本。

## Prerequisites

- Docker
- Docker Compose
- Java 8
- Maven

执行集成测试前必须先启动测试 Docker 环境，并确认 MySQL、Redis、RocketMQ 服务健康。

## Start Environment

复制环境变量样例：

```bash
cp deploy/integration/.env.example deploy/integration/.env
```

启动基础环境：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml up -d
```

查看服务状态：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml ps
```

## Data Initialization

集成测试数据固定由 `deploy/integration/db/` 提供。

装载顺序固定为：

1. `db/00-schema/`
2. `db/90-cleanup/`
3. `db/10-baseline/`
4. `db/20-scenarios/`

`00-schema` 必须追溯到根目录 `db/schema/` 的正式建表脚本。`10-baseline` 和 `20-scenarios` 只承载集成测试数据，不作为产品部署数据。

## Run Tests

默认单元测试：

```bash
mvn test
```

完整集成测试：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml ps
mvn verify -Pit
```

模块级集成测试：

```bash
mvn -pl sandwish-admin-api -am verify -Pit
mvn -pl sandwish-front-api -am verify -Pit
mvn -pl sandwish-open-api -am verify -Pit
```

## Cleanup

停止集成测试环境：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml down
```

清理环境数据卷：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml down -v
```

清理 SQL、Redis key、OSS 临时目录和认证运行态数据时，优先使用 `db/90-cleanup/` 和测试支撑类，不手工删除业务表全量数据。

## Troubleshooting

- 端口冲突：检查 `deploy/integration/.env` 中 MySQL、Redis、RocketMQ 端口。
- MySQL 初始化失败：先执行 `docker compose ... down -v`，再重新启动环境。
- Redis 数据污染：确认 key prefix 使用 integration 专用前缀。
- RocketMQ 启动慢：先确认 NameServer 健康，再检查 Broker 日志。
- 集成测试拒绝启动：确认 `application-it.yml` 中 `sandwish.integration-test.enabled=true`。
- 验证码失败：确认测试请求提交的 `captcha` 命中验证码值白名单。
