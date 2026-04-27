# Common Cache JetCache Runbook

## 1. Goal

建立 `sandwish-common-cache`，用 JetCache 承接通用缓存能力，逐步移除 `sandwish-common-core` 中的 `RedisClient`。

目标不是把 RedisClient 换一个名字搬进 common，而是把直接 Redis 操作收敛为明确的缓存能力：

- common-cache 只暴露缓存基础设施和极薄的通用 cache operation。
- 业务 key、TTL、版本号、回源和失效策略仍归属对应业务/infra 组件。
- Controller、Service、Servlet、Filter 不直接依赖 Redis、JetCache 或 generic cache client。
- 迁移完成后删除 `com.github.thundax.common.utils.redis.RedisClient`。

## 2. Reference

参考 `../bacon` 的 JetCache 使用方式：

- 根 `pom.xml` 管理 `jetcache.version` 和 JetCache 依赖版本。
- common 技术模块启用 JetCache 注解能力。
- infra/cache support 使用 `@CreateCache` 创建强类型缓存。
- JetCache 配置留在运行应用配置中，应用选择 remote/local/both。

Sandwich 与 Bacon 的差异：

- Sandwich 是 Spring Boot 2.0.5 / Java 8，不能直接复制 Bacon 的 Java 17 语法。
- Sandwich 本次建立显式 `sandwish-common-cache`，避免继续把缓存能力塞回 `common-core`。
- Sandwich 迁移期允许 RedisClient 和 common-cache 短期并存，但新增代码不得依赖 RedisClient。

## 3. Module Boundary

### `sandwish-common-cache`

职责：

- 依赖 JetCache。
- 启用 JetCache 注解能力。
- 提供项目统一的 JetCache 基线配置。
- 提供必要的极薄通用缓存操作适配，且不得携带业务 key 规则。

禁止：

- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。
- 不承载业务 cache support。
- 不出现 `DictCache`、`UserCache`、`AccessTokenCache` 等业务命名。
- 不暴露 Redis API、`StringRedisTemplate`、`RedisTemplate`。
- 不复刻 `RedisClient` 的 keys、hash、pattern delete 大而全能力。

### `sandwish-infra`

职责：

- 在 DAO/cache support 内使用 JetCache。
- 持有业务 key、namespace、TTL、版本号和失效语义。
- 用业务 DAO/Store 接口对上层隐藏缓存实现。

### API / Biz

职责：

- 通过业务 Service、DAO、Store 访问状态。
- 不直接注入 JetCache 或 Redis。

## 4. Migration Order

### Step 1: Establish Common Cache Module

新增：

- `sandwish-common/sandwish-common-cache`
- root `pom.xml` dependencyManagement 中补齐 JetCache starter 依赖。
- `sandwish-common/pom.xml` 注册模块。
- `sandwish-common-cache` 提供 JetCache 基线配置。

验收：

- `mvn -q -pl sandwish-common/sandwish-common-cache -am compile -DskipTests` 通过。
- `sandwish-common-core` 不新增 JetCache 依赖。

### Step 2: Wire Applications

后台和前台应用依赖 `sandwish-common-cache`，并在运行配置中补齐 JetCache 配置。

验收：

- `sandwish-admin-api`、`sandwish-front-api` 能编译。
- JetCache 配置不影响当前 RedisClient 迁移期运行。

### Step 3: Replace Sys CacheSupport Backing

优先迁移系统类 cache support：

- `DictCacheSupport`
- `MenuCacheSupport`
- `OfficeCacheSupport`
- `RoleCacheSupport`
- `UserCacheSupport`

处理原则：

- 使用 `@CreateCache` 创建强类型 cache。
- 保留现有 key、TTL、版本和失效语义。
- JetCache 类型不外泄到 Service/Controller/Entity/Request/Response。

验收：

- CacheSupport 不再 import `RedisClient`。
- 相关 DAO/service 编译通过。

### Step 4: Replace Storage CacheSupport Backing

迁移：

- `StorageCacheSupport`

处理原则：

- 使用 `@CreateCache` 创建强类型 cache。
- 保留现有 key、TTL、版本和失效语义。
- JetCache 类型不外泄到 Service/Controller/Entity/Request/Response。

验收：

- `StorageCacheSupport` 不再 import `RedisClient`。

### Step 5: Replace Auth DAO State Stores

迁移以下 DAO 实现的 RedisClient 依赖：

- `AccessTokenDaoImpl`
- `LoginFormDaoImpl`
- `LoginLockDaoImpl`
- `PermissionDaoImpl`

处理原则：

- 保持现有业务 DAO 接口为语义边界。
- JetCache 只留在 infra 实现。
- 不新增泛化 `CacheService`。

验收：

- infra DAO 不再 import `RedisClient`。

验收：

- auth DAO 不再 import `RedisClient`。

### Step 6: Replace Assist DAO State Stores

迁移：

- `AsyncTaskDaoImpl`
- `KeypairPrivateKeyDaoImpl`

处理原则：

- 保持现有业务 DAO 接口为语义边界。
- JetCache 只留在 infra 实现。
- 不新增泛化 `CacheService`。

验收：

- assist DAO 不再 import `RedisClient`。

### Step 7: Replace Front Direct Callers

迁移：

- `SessionCacheServiceImpl`
- `DictUtils`

处理原则：

- 直接调用点迁到业务 Store/DAO 或已有业务缓存路径。
- 不让 front 入口直接注入 JetCache。

验收：

- `sandwish-front-api` 不再直接 import `RedisClient`。

### Step 8: Replace Biz SMS Direct Caller

迁移：

- `SmsValidateCodeServlet`

处理原则：

- Servlet 不通过 `SpringContextHolder` 获取缓存能力。
- 不让 Servlet 直接注入 JetCache。

验收：

- `sandwish-biz` 不再直接 import `RedisClient`。

### Step 9: Delete RedisClient

完成全仓迁移后删除：

- `sandwish-common-core/src/main/java/com/github/thundax/common/utils/redis/RedisClient.java`
- common-core 中与 Redis client 职责相关依赖。

验收：

```bash
rg -n "com.github.thundax.common.utils.redis.RedisClient|RedisClient" sandwish-common sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api -g '*.java'
mvn -q -pl sandwish-admin-api,sandwish-front-api -am compile -DskipTests
```

## 5. Verification

每一步至少运行：

```bash
mvn -q -pl sandwish-admin-api,sandwish-front-api -am compile -DskipTests
```

模块基线步骤可先运行：

```bash
mvn -q -pl sandwish-common/sandwish-common-cache -am compile -DskipTests
```

## 6. Non-Goals

- 不升级 Spring Boot。
- 不一次性重写所有缓存调用点。
- 不改变现有业务缓存 key。
- 不改变现有 TTL 和失效时机，除非对应任务明确说明。
- 不把 Redis 命令封装原样迁移为 common-cache API。
