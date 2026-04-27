# Common Cache JetCache Runbook

## 1. Purpose

本文档定义 `sandwish-common-cache` 的最终运行边界。

`sandwish-common-cache` 只承载 JetCache 基线配置和缓存基础设施自动配置。业务 key、TTL、版本号、回源和失效策略固定留在对应 infra DAO 或 cache support 中。

## 2. Module Boundary

### `sandwish-common-cache`

职责：

- 依赖 JetCache starter。
- 通过 Spring Boot 2 `spring.factories` 注册 JetCache 自动配置。
- 启用 `@CreateCache` 和方法缓存注解能力。
- 设置项目级 JetCache 默认参数。

禁止：

- 不承载业务 cache support。
- 不定义业务 key、业务 TTL、业务版本号和回源策略。
- 不暴露 Redis API、`RedisTemplate`、`StringRedisTemplate` 或通用 Redis 客户端封装。
- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。

### `sandwish-infra`

职责：

- 在 DAO 和 cache support 内使用 `@CreateCache`。
- 持有业务 key、TTL、版本号、失效和回源语义。
- 通过 `sandwish-biz` 的 DAO/Store 接口对上层隐藏缓存实现。

### API / Biz

职责：

- Controller、Servlet、Filter 和 Service 通过业务 Service、DAO 或 Store 访问状态。
- 不直接注入 JetCache、Redis API 或通用缓存客户端。

## 3. Runtime Wiring

后台和前台应用固定依赖 `sandwish-common-cache`。

应用配置固定提供 JetCache 默认区：

- `jetcache.local.default`
- `jetcache.remote.default`

remote 默认使用 `redis.lettuce`，连接 URI 通过 `SANDWISH_REDIS_URI` 覆盖。本地开发默认值为 `redis://127.0.0.1:6379/0`。

## 4. Implementation Rules

- `@CreateCache` 只允许出现在 common-cache 基线配置、infra DAO、infra cache support 或入口模块明确的缓存适配实现中。
- Service、Controller、Entity、Request 和 Response 不出现 JetCache 类型。
- 新增缓存能力必须先判断是否已有业务 DAO/Store 语义边界。
- 不新增泛化 `CacheService`、`RedisService`、`CacheRepository` 或类似二次客户端。
- 批量失效不得依赖 Redis pattern delete；需要批量删除时，由所属 infra 组件维护本组件写入的 key 索引，或通过版本号让旧缓存自然失效。

## 5. Verification

常用检查：

```bash
mvn -q -pl sandwish-admin-api,sandwish-front-api -am compile -DskipTests
```

## 6. Open Items

无
