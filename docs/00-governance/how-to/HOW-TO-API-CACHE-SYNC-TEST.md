# HOW-TO API Cache Sync Test

## 1. Purpose

本文档定义 Sandwich `*-api` 多实例缓存同步验证流程。

目标是固定用同一个 API 应用的 A/B/C 三个实例验证 Redis / JetCache 运行态和业务缓存是否跨实例可见，避免只验证单实例或只验证 nginx 转发。

## 2. Scope

当前范围：

- `sandwish-admin-api`
- `sandwish-front-api`
- `sandwish-open-api`
- `scripts/smoke/smoke-admin-api-cache-sync.sh`
- `scripts/smoke/smoke-front-api-cache-sync.sh`
- `scripts/smoke/smoke-open-api-cache-sync.sh`
- `scripts/smoke/smoke-cache-sync-all.sh`

不在范围内：

- 不定义生产负载均衡拓扑
- 不替代普通 smoke
- 不替代 k6 压测
- 不验证数据库主从复制

## 3. Bounded Context

缓存同步验证固定按“同一个 `*-api` 起三份实例”执行。

固定实例角色：

- A：第一个应用实例
- B：第二个应用实例
- C：第三个应用实例

固定验证方式：

1. 在一个实例写入缓存或运行态。
2. 在另外两个实例读取、刷新或重放同一个运行态。
3. 按 A -> B -> C、B -> C -> A、C -> A -> B 三个方向执行。

脚本不负责启动 A/B/C。A/B/C 可以来自本机三个端口、三个 Docker service、三个 Docker Compose project、k8s 三个 pod port-forward 或其他测试拓扑。

## 4. When To Use

以下场景固定执行缓存同步验证：

- `CacheType`、JetCache、Redis 配置或缓存 key 规则变化后
- 认证会话、预认证会话、access token、refresh token 或 nonce 运行态变化后
- 部署拓扑从单实例变为多实例前
- nginx、网关或服务发现配置支持多实例前
- 上线前需要确认多实例共享缓存语义时

## 5. Do Not Use For

以下场景不使用本文档作为唯一依据：

- 单接口功能正确性测试
- Controller URL 挂载冒烟
- 高并发容量评估
- 数据库写入一致性验证
- 生产环境破坏性写入测试

## 6. Pre-Checks

执行前固定完成以下检查：

1. A/B/C 三个实例使用同一个 Redis。
2. A/B/C 三个实例使用同一套数据库和必要外部依赖。
3. A/B/C 三个实例的 `context-path` 与单实例部署一致。
4. A/B/C 三个实例可以被脚本直接访问，不经过随机负载均衡入口。
5. Open API 脚本已经准备可用的 API key 和 API secret。
6. 测试环境允许创建预认证会话和 Open API 签名读请求。

## 7. Steps

### 7.1 启动三实例

按当前测试环境启动同一个 API 应用的三个实例。

本机 jar 示例：

```bash
SERVER_PORT=21011 SERVER_SERVLET_CONTEXT_PATH=/admin-api java -jar sandwish-admin-api.jar
SERVER_PORT=21012 SERVER_SERVLET_CONTEXT_PATH=/admin-api java -jar sandwish-admin-api.jar
SERVER_PORT=21013 SERVER_SERVLET_CONTEXT_PATH=/admin-api java -jar sandwish-admin-api.jar
```

三个实例必须指向同一个 `SANDWISH_REDIS_URI`。

### 7.2 配置实例地址

后台 API：

```bash
export SANDWICH_ADMIN_API_A_BASE_URL=http://127.0.0.1:21011/admin-api
export SANDWICH_ADMIN_API_B_BASE_URL=http://127.0.0.1:21012/admin-api
export SANDWICH_ADMIN_API_C_BASE_URL=http://127.0.0.1:21013/admin-api
```

前台 API：

```bash
export SANDWICH_FRONT_API_A_BASE_URL=http://127.0.0.1:22011/front-api
export SANDWICH_FRONT_API_B_BASE_URL=http://127.0.0.1:22012/front-api
export SANDWICH_FRONT_API_C_BASE_URL=http://127.0.0.1:22013/front-api
```

开放 API：

```bash
export SANDWICH_OPEN_API_A_BASE_URL=http://127.0.0.1:23011/open-api
export SANDWICH_OPEN_API_B_BASE_URL=http://127.0.0.1:23012/open-api
export SANDWICH_OPEN_API_C_BASE_URL=http://127.0.0.1:23013/open-api
export SANDWICH_OPEN_CONTEXT_PATH=/open-api
export SANDWICH_SMOKE_OPEN_API_KEY=实际 API key
export SANDWICH_SMOKE_OPEN_API_SECRET=实际 API secret
```

### 7.3 执行单入口验证

后台 API：

```bash
scripts/smoke/smoke-admin-api-cache-sync.sh
```

前台 API：

```bash
scripts/smoke/smoke-front-api-cache-sync.sh
```

开放 API：

```bash
scripts/smoke/smoke-open-api-cache-sync.sh
```

### 7.4 执行全量验证

三类入口都准备好后执行：

```bash
scripts/smoke/smoke-cache-sync-all.sh
```

## 8. Files To Touch

固定文件：

- `scripts/smoke/smoke-admin-api-cache-sync.sh`
- `scripts/smoke/smoke-front-api-cache-sync.sh`
- `scripts/smoke/smoke-open-api-cache-sync.sh`
- `scripts/smoke/smoke-cache-sync-all.sh`
- `scripts/smoke/lib/smoke-common.sh`
- `scripts/smoke/.env.example`
- `.env.test.example`
- `scripts/smoke/README.md`

文档入口：

- `docs/00-governance/how-to/HOW-TO-API-CACHE-SYNC-TEST.md`
- `docs/AGENT.md`

不要触碰：

- `deploy/docker-compose.yml`，除非测试拓扑固定进入项目部署样例
- `deploy/nginx/default.conf`，除非要验证 nginx upstream 策略
- 生产环境 `.env` 私密文件

## 9. Common Mistakes

- A/B/C 三个地址实际指向同一个实例。
- 通过随机负载均衡入口访问，无法确认请求命中了哪个实例。
- 三个实例没有使用同一个 Redis。
- 只执行普通 smoke，把单实例可用误判为多实例缓存同步可用。
- Open API nonce 使用进程内内存，导致 A 使用后 B/C 仍可接受同一个 nonce。
- `CacheType.BOTH` 场景下使用 JetCache `PUT_IF_ABSENT` 验证原子写入；JetCache 2.5.16 的 multi-level cache 不支持该方法。

## 10. Verification

脚本级检查：

```bash
bash -n scripts/smoke/smoke-admin-api-cache-sync.sh
bash -n scripts/smoke/smoke-front-api-cache-sync.sh
bash -n scripts/smoke/smoke-open-api-cache-sync.sh
bash -n scripts/smoke/smoke-cache-sync-all.sh
```

运行级验收：

- 后台 A/B/C 预认证会话 create / refresh 链路全部返回 2xx。
- 前台 A/B/C 预认证会话 create / refresh 链路全部返回 2xx。
- 开放接口同一个 nonce 首个实例返回 200，另外两个实例返回 401。
- 任一方向失败时，不进入容量压测。

## 11. Commit Guidance

缓存同步脚本、HOW-TO 和 smoke 文档改动使用：

```text
Test(cache): 中文说明
```

如果同时修复缓存实现，代码、测试和脚本可以放在同一个 commit，但提交说明必须点出具体能力。

## 12. Open Items

无
