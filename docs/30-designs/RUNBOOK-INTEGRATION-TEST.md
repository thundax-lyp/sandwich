# Integration Test Runbook

## Purpose

本 RUNBOOK 用于编排 Sandwich 集成测试体系建设。

目标状态：

- 集成测试使用独立配置、独立 Docker 环境和独立测试数据。
- 集成测试通过真实 HTTP 入口访问 API，覆盖 `Controller -> Service -> DAO/Mapper -> Database` 主链路。
- 测试用例分布覆盖全部业务功能；鉴权域按关键链路覆盖，不要求覆盖每一种外部登录和授权变体。
- 默认单元测试命令保持轻量，集成测试使用独立 profile 和独立命令运行。

## Scope

范围内：

- `deploy/integration/` 集成测试环境目录。
- `deploy/integration/README.md` 集成测试环境操作说明。
- `deploy/integration/db/` 集成测试 schema、baseline 数据、scenario 数据和 cleanup 脚本。
- 后端集成测试专用 `application-it.yml`。
- MySQL、Redis、RocketMQ 集成测试 Docker 环境。
- 验证码白名单和测试 profile 防误连保护。
- admin、front、open 三个 API 入口的完整链路集成测试。
- 业务接口覆盖清单、测试数据维护规则和验收命令。

范围外：

- 不替代现有单元测试、架构测试和 controller contract 测试。
- 不把集成测试数据放入根目录 `db/` 作为产品部署数据。
- 不把集成测试默认并入 `mvn test`。
- 不在第一阶段接入真实外部 OAuth、短信、企业微信、GitHub、S3 或生产 MQ。

## Bounded Context

集成测试固定验证真实运行组合，不重复覆盖单元测试已经验证的内部实现细节。

核心验证对象：

- Spring bean 装配。
- HTTP request binding、Jackson 序列化、统一响应包装和异常转换。
- Filter、Interceptor、当前用户上下文和权限适配。
- Service 事务边界和跨 DAO 编排。
- MyBatis SQL、字段映射、分页、排序、状态更新和唯一约束。
- Redis 缓存、验证码白名单、nonce、token 或会话状态。
- RocketMQ 发送链路和测试 topic/group 基线。
- 本地 OSS 文件上传、下载和清理链路。

测试命名固定使用 `*IT.java`，并通过 Maven integration profile 运行。

## Directory Plan

固定目标目录：

```text
deploy/integration/
  README.md
  docker-compose.yml
  .env.example
  db/
    00-schema/
    10-baseline/
    20-scenarios/
    90-cleanup/
```

目录职责：

- `00-schema/`：装载正式 schema 的集成测试入口，来源必须可追溯到根目录 `db/`。
- `10-baseline/`：所有集成测试共享的基础账号、角色、菜单、权限、字典、OpenClient、会员和系统配置数据。
- `20-scenarios/`：按业务域准备的测试场景数据。
- `90-cleanup/`：清理测试数据、Redis key、临时对象和 MQ 测试状态的脚本入口。

## Configuration Plan

集成测试配置固定使用 `application-it.yml`。

配置必须包含：

- `spring.profiles.active=it` 运行口径。
- `sandwish.integration-test.enabled=true` 防误连开关。
- MySQL、Redis、RocketMQ 连接信息。
- 集成测试专用 Redis key prefix。
- 集成测试专用 RocketMQ topic、group 和 tag 前缀。
- 本地 OSS 临时目录。
- 验证码值白名单，例如 `6666`、`8888`。
- OpenAPI 固定 `clientId`、`clientSecret`、nonce 和 IP 白名单数据口径。

测试启动时必须校验 `sandwish.integration-test.enabled=true`，未开启时集成测试直接失败。

验证码白名单固定按提交的 `captcha` 值判断。请求中的 `captcha` 命中白名单值时，验证码校验放行；白名单不得绑定手机号、邮箱、登录名或用户 ID。该规则用于充分覆盖注册、登录和验证码失败链路。

## Data Plan

集成测试数据固定独立维护在 `deploy/integration/db/`。

基础数据必须覆盖：

- 后台管理员账号、角色、菜单、权限和部门。
- 当前用户信息、密码更新、头像上传所需账号状态。
- 字典、日志、审计元数据和审计日志基础记录。
- OpenClient、OpenClient 权限、签名密钥和 IP 白名单。
- 前台会员、登录标识、认证凭据和认证会话。
- submission、submission image、storage object、object reference、multipart upload 基础记录。

场景数据按业务域拆分，单个 SQL 文件只服务一个业务域或一条可验收链路。

测试数据使用稳定业务编号、稳定登录名和稳定对象 key。测试结束后清理脚本必须能重复执行。

## Testcase Distribution

测试用例分布固定覆盖全部业务功能。

鉴权域例外规则：

- 鉴权必须覆盖登录、token refresh、logout、当前用户、权限失败、验证码白名单和会话失效关键链路。
- OAuth2、短信、企业微信、GitHub 等外部依赖型入口允许用替身数据覆盖协议边界和失败响应，不要求接入真实外部服务。
- 鉴权域不要求每一种登录方式都具备完整端到端成功链路。

admin API 覆盖域：

- `auth`：后台登录、验证码白名单、token 校验、token refresh、logout、权限失败。
- `sys`：当前用户、用户、角色、菜单、部门、字典、日志。
- `open`：OpenClient 创建、更新、分页、详情、状态变更和 secret reset。
- `storage`：对象上传、下载、分页、删除、排序、tree、multipart initiate/upload/complete/abort。
- `submission`：创建、分页、详情、状态变更、删除、排序和图片上传。
- `audit`：meta、history、detail、object overview、object page、page、options、fields。

front API 覆盖域：

- `auth/session`：预认证会话、账号登录、短信登录替身链路、token refresh、登录状态、check-login、logout。
- `auth/register`：账号注册、手机号验证码、手机号注册、邮箱验证码、邮箱注册。

open API 覆盖域：

- 签名成功、签名失败、nonce 重放、IP 白名单失败。
- submission 创建、分页、状态变更和图片上传。

每个业务接口至少有一个成功链路测试。对状态变更、删除、上传、签名、权限和参数校验类接口，必须同步覆盖失败链路。

## Execution Order

### Phase 1: Runbook And TODO

1. 新增本 RUNBOOK。
2. 拆分根目录 `TODO.md`，将集成测试建设任务放入 `待审阅任务项`。
3. 人工审阅 TODO 粒度、顺序和文件范围。

### Phase 2: Integration Environment

1. 新增 `deploy/integration/README.md`。
2. 新增 `deploy/integration/docker-compose.yml` 和 `.env.example`。
3. 新增 `deploy/integration/db/` 目录结构。
4. 固定 MySQL、Redis、RocketMQ 的端口、账号、database、topic 和 group 前缀。
5. 固定集成测试执行前的 Docker 环境启动和健康检查命令。

### Phase 3: Test Profile

1. 新增 `application-it.yml`。
2. 增加 integration profile 防误连保护。
3. 增加验证码白名单，只允许在 integration profile 生效。
4. 固定本地 OSS 临时目录和清理策略。

### Phase 4: Baseline Data

1. 建立 schema 装载入口。
2. 建立 baseline 数据脚本。
3. 建立 scenario 数据脚本。
4. 建立 cleanup 脚本。
5. 验证脚本可重复执行。

### Phase 5: Test Harness

1. 增加集成测试 Maven profile。
2. 配置 Failsafe 执行 `*IT.java`。
3. 增加 HTTP client、登录 token、数据库重置和测试数据装载支撑。
4. 确认 `mvn test` 不执行集成测试。

### Phase 6: Business Coverage

1. 先覆盖 admin auth、current-user、dict、user 和 open client 主链路。
2. 再覆盖 open API 签名和 submission 主链路。
3. 再覆盖 storage、multipart upload、submission image 和 audit。
4. 再覆盖 front auth/session 和 register。
5. 最后补齐剩余接口的成功链路和必要失败链路。

### Phase 7: Closure

1. 汇总接口覆盖清单。
2. 跑完整集成测试命令。
3. 更新 `deploy/integration/README.md` 中的命令、数据和排错说明。
4. 删除或收窄已完成 `TODO.md` 项。
5. 同步 Docker 运行配置并重建 `deploy/image-files/*.tar`。
6. 清理本 RUNBOOK 和残留引用。

## Verification

执行集成测试前必须先启动测试 Docker 环境，并确认 MySQL、Redis、RocketMQ 服务健康。

基础环境验收：

```bash
docker compose -f deploy/integration/docker-compose.yml up -d
```

单元测试验收：

```bash
mvn test
```

集成测试验收：

```bash
docker compose -f deploy/integration/docker-compose.yml ps
mvn verify -Pit
```

模块级集成测试验收：

```bash
mvn -pl sandwish-admin-api -am verify -Pit
mvn -pl sandwish-front-api -am verify -Pit
mvn -pl sandwish-open-api -am verify -Pit
```

数据验收：

- MySQL schema 与正式建表脚本一致。
- baseline 脚本可重复装载。
- scenario 脚本互不依赖执行顺序。
- cleanup 脚本可重复执行。
- Redis key 带 integration prefix。
- RocketMQ topic 和 group 带 integration prefix。
- OSS 临时目录可清理。

覆盖验收：

- admin、front、open 三个入口均存在 `*IT.java`。
- 每个业务接口至少有一个成功链路集成测试。
- 状态变更、删除、上传、签名、权限和参数校验类接口存在失败链路集成测试。
- 鉴权域覆盖关键链路和失败响应，不要求真实外部服务成功链路。

## Open Items

无
