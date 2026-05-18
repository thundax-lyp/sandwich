# Sandwich Integration Test Environment

本文档用途：说明集成测试 Docker 环境、数据初始化、测试运行、清理和排错入口。

阶段拆解以 `TODO.md` 为准。正式部署样例仍以 `deploy/README.md` 和 `deploy/docker-compose.yml` 为准。

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

RocketMQ 发送超时可通过 `SANDWISH_IT_ROCKETMQ_SEND_TIMEOUT_MS` 调整，默认 `15000` 毫秒。该值只作用于 `application-it.yml`，用于覆盖集成测试冷启动时的首次路由发现耗时。

查看服务状态：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml ps
```

## Health Check

执行集成测试前，`mysql`、`redis`、`rocketmq-namesrv` 和 `rocketmq-broker` 必须全部处于 `healthy` 状态。

检查命令：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml ps
```

期望状态：

```text
mysql                 Up ... (healthy)
redis                 Up ... (healthy)
rocketmq-namesrv      Up ... (healthy)
rocketmq-broker       Up ... (healthy)
```

服务未健康时查看日志：

```bash
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml logs mysql
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml logs redis
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml logs rocketmq-namesrv
docker compose --env-file deploy/integration/.env -f deploy/integration/docker-compose.yml logs rocketmq-broker
```

RocketMQ 还需要确认 Broker 注册地址是宿主机可访问地址：

```bash
docker exec integration-rocketmq-broker-1 \
  /home/rocketmq/rocketmq-5.4.0/bin/mqadmin clusterList -n rocketmq-namesrv:9876
```

期望 `#Addr` 为 `127.0.0.1:20911`。如果看到 `172.*:10911`，宿主机上的集成测试会连接容器内网地址并导致同步发送超时。

如怀疑 Docker 内 RocketMQ 权限异常，检查 Broker 日志和存储目录：

```bash
docker logs integration-rocketmq-broker-1
docker exec integration-rocketmq-broker-1 \
  sh -lc 'grep -R "Permission denied\\|permission denied" -n /home/rocketmq/logs /home/rocketmq/store 2>/dev/null || true'
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

调试单组 IT 时可用 Surefire 指定类名：

```bash
mvn -pl sandwish-admin-api -am -Dtest=AdminAuthSessionIT test
mvn -pl sandwish-front-api -am -Dtest=FrontAuthSessionIT test
mvn -pl sandwish-open-api -am -Dtest=OpenApiSignatureIT,OpenSubmissionQueryIT,OpenSubmissionMutationIT,OpenSubmissionUploadIT test
```

接口覆盖清单见 `deploy/integration/API-COVERAGE.md`。新增或修改 Controller 时，必须同步该清单和对应 `*IT.java`。

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
- RocketMQ 同步发送超时：先确认本地测试进程可访问 `127.0.0.1:${SANDWISH_IT_ROCKETMQ_NAMESRV_PORT}` 和 Broker `127.0.0.1:${SANDWISH_IT_ROCKETMQ_BROKER_PORT}`。
- RocketMQ 多 IP 环境异常：确认 `deploy/integration/rocketmq/broker.conf` 中 `brokerIP1` 与宿主机访问地址一致。
- 集成测试拒绝启动：确认 `application-it.yml` 中 `sandwish.integration-test.enabled=true`。
- 验证码失败：确认测试请求提交的 `captcha` 命中验证码值白名单。
