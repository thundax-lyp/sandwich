# HOW-TO API Load Test

## 1. Purpose

本文档定义 Sandwich `*-api` Docker 部署后的压测操作流程、脚本入口和报告规范。

目标是让后台 API、前台 API 和开放 API 的压测固定按同一套最小闭环执行，避免把部署错误、接口挂载错误、认证配置错误和真实容量问题混在一起判断。

## 2. Scope

当前范围：

- `sandwish-admin-api`
- `sandwish-front-api`
- `sandwish-open-api`
- `deploy/docker-compose.yml`
- `deploy/nginx/default.conf`
- `scripts/smoke/`
- `scripts/load/`
- `reports/load/`

不在范围内：

- 不定义生产 SLA
- 不替代数据库专项压测
- 不替代 JVM、MySQL、Redis、MinIO 或 RocketMQ 调优手册
- 不把写入接口压测作为默认动作

## 3. Bounded Context

Sandwich Docker 部署固定通过 nginx 暴露三个 API context-path：

- 后台 API：`/admin-api`
- 前台 API：`/front-api`
- 开放 API：`/open-api`

压测必须先确认 Docker / nginx / context-path / Controller mapping 可用，再进入吞吐和延迟测试。

压测分三层：

1. `smoke-all.sh`：核心入口冒烟，验证主要运行链路。
2. `smoke-api-surface.sh`：全部 Controller URL 点火，验证接口面可达。
3. `run-k6-api-read.sh`：读链路压力测试，输出 JSON 和 Markdown 报告。

## 4. When To Use

以下场景固定使用本文档：

- Docker Compose 部署到服务器后做上线前验证
- 调整 nginx、context-path、容器环境变量或镜像版本后做回归
- 评估 `*-api` 在当前服务器规格下的读链路容量
- 生成可保存的 API 压测报告

## 5. Do Not Use For

以下场景不使用本文档作为唯一依据：

- 生产环境极限压测
- 大规模写入、删除、上传或数据污染型压测
- 数据库索引、慢 SQL、连接池的专项压测
- 前端页面渲染性能测试

生产环境只允许低强度只读压测。极限压测固定使用独立压测环境。

## 6. Pre-Checks

压测前固定完成以下检查：

1. 被测环境使用独立测试数据，不直接压生产数据。
2. 压测机与被测服务分离，避免压测进程和 API 容器争用 CPU、内存和网络。
3. Docker Compose 服务已经启动。
4. `scripts/smoke/smoke-all.sh` 已通过。
5. `scripts/smoke/smoke-api-surface.sh` 已通过。
6. 后台认证态压测已经准备 `SANDWICH_SMOKE_ADMIN_TOKEN`。
7. 开放 API 签名压测已经准备 `SANDWICH_SMOKE_OPEN_API_KEY` 和 `SANDWICH_SMOKE_OPEN_API_SECRET`。
8. 已确认本次是否允许写入测试数据。默认不允许写入。

## 7. Steps

### 7.1 准备环境变量

复制压测环境变量样例：

```bash
cp .env.test.example .env.load
```

固定按实际环境填写：

- `SANDWICH_PUBLIC_BASE_URL`
- `SANDWICH_ADMIN_BASE_URL`
- `SANDWICH_FRONT_BASE_URL`
- `SANDWICH_OPEN_BASE_URL`
- `SANDWICH_SMOKE_ADMIN_TOKEN`
- `SANDWICH_SMOKE_OPEN_API_KEY`
- `SANDWICH_SMOKE_OPEN_API_SECRET`
- `SANDWICH_LOAD_STAGES`

`.env.test.example` 同时覆盖 smoke 和 load 变量。`scripts/smoke/.env.example` 与 `scripts/load/.env.example` 只作为专项变量参考。

### 7.2 部署正确性检查

执行核心冒烟：

```bash
scripts/smoke/smoke-all.sh
```

执行全部接口点火：

```bash
scripts/smoke/smoke-api-surface.sh
```

`smoke-api-surface.sh` 使用空请求、无效参数或未签名请求验证 Controller URL 可达。该脚本不作为业务成功率指标。

### 7.3 执行读链路压测

执行 k6 读链路压测：

```bash
scripts/load/run-k6-api-read.sh
```

默认压测范围：

- `/admin-api/api/auth/session/pre-auth-session`
- `/front-api/api/auth/session/pre-auth-session`
- `/front-api/api/auth/session/check-login`
- `/open-api/api/submission/submission/page` 未签名认证边界
- 有后台 token 时压后台当前用户、菜单、权限、字典分页和存储树
- 有 Open API key/secret 时压开放接口签名分页

默认不压：

- create
- update
- delete
- upload
- sort
- move

### 7.4 分阶段加压

`SANDWICH_LOAD_STAGES` 固定使用 `duration:target,duration:target` 格式。

基准示例：

```bash
SANDWICH_LOAD_STAGES=30s:5,2m:20,30s:0 scripts/load/run-k6-api-read.sh
```

阶梯示例：

```bash
SANDWICH_LOAD_STAGES=1m:20,3m:50,3m:100,1m:0 scripts/load/run-k6-api-read.sh
```

稳定性示例：

```bash
SANDWICH_LOAD_STAGES=2m:50,30m:50,2m:0 scripts/load/run-k6-api-read.sh
```

### 7.5 收集资源指标

压测期间固定收集：

- `docker stats`
- nginx access log 和 error log
- API 容器日志
- MySQL CPU、连接数、慢 SQL、锁等待
- Redis 连接数、延迟和内存
- MinIO 请求错误
- RocketMQ broker 日志和堆积

## 8. Files To Touch

固定文件：

- `scripts/load/k6-api-read.js`
- `scripts/load/run-k6-api-read.sh`
- `scripts/load/.env.example`
- `scripts/load/REPORT-TEMPLATE.md`
- `scripts/load/README.md`

报告输出目录：

- `reports/load/`

压测报告不作为默认提交内容。只有用户明确要求归档某次测试报告时，才提交 `reports/load/*.md`。

不要触碰：

- `sandwish-*-api/src/main/java`，除非压测发现了明确代码问题并进入修复任务
- `deploy/docker-compose.yml`，除非部署拓扑或环境变量确实需要调整
- 生产环境 `.env` 私密文件

## 9. Common Mistakes

- 未跑 `smoke-api-surface.sh` 就开始压测，导致把 404、405 或 502 误判为容量问题。
- 在 API 服务器本机运行大压力，导致压测进程和服务端争用资源。
- 用生产环境做极限压测。
- 默认压 create / update / delete / upload，污染测试数据。
- 只看平均响应时间，不看 P95、P99 和错误率。
- 只看 k6 结果，不看 MySQL、Redis、nginx 和 API 容器资源。
- Open API 签名压测时忘记 `SANDWICH_OPEN_CONTEXT_PATH`，导致 canonical path 不一致。

## 10. Verification

压测前验证：

```bash
bash -n scripts/load/run-k6-api-read.sh
bash -n scripts/smoke/smoke-api-surface.sh
scripts/smoke/smoke-all.sh
scripts/smoke/smoke-api-surface.sh
```

压测后固定检查：

- `reports/load/k6-summary.json` 已生成
- `reports/load/k6-report.md` 已生成
- `http_req_failed` 未超过阈值
- `http_req_duration` 的 P95、P99 未超过阈值
- API 容器无持续 5xx
- nginx 无持续 upstream error
- MySQL 无持续慢 SQL 或连接耗尽

## 11. Report Specification

压测报告固定包含：

- 测试日期、执行人、git commit、镜像版本
- 压测机规格和被测环境规格
- Docker Compose 配置差异
- 数据规模
- 压测命令和 `SANDWICH_LOAD_STAGES`
- QPS、错误率、平均响应、P95、P99、最大响应
- API、MySQL、Redis、MinIO、RocketMQ、nginx 资源指标
- 瓶颈分析
- 是否通过
- 当前容量边界
- 进入生产前必须完成事项

报告模板固定使用：

```bash
scripts/load/REPORT-TEMPLATE.md
```

## 12. Commit Guidance

压测脚本、报告模板和手册改动使用：

```text
Test(load): 中文说明
```

某次具体压测报告默认不提交。用户明确要求归档报告时，报告提交信息使用：

```text
Test(load): 归档某环境压测报告
```

## 13. Open Items

无
