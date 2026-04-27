# Cache Common Boundary Decision

## 1. Purpose

本文档记录 `sandwish-common-cache` 是否建立的边界决策。

该决策基于当前缓存现状盘点、RedisClient 迁移目标，以及参考 `../bacon` 的 JetCache 使用方式，用于指导后续缓存迁移任务。

## 2. Scope

当前范围：

- 是否建立 `sandwish-common-cache`
- common 与 infra 的缓存职责边界
- 后续 TODO 收窄方向

不在范围内：

- 不一次性迁移所有业务缓存链路
- 不删除尚有调用的 `RedisClient`
- 不升级 Spring Boot

## 3. Decision

建立 `sandwish-common-cache`。

`sandwish-common-cache` 作为 JetCache 基础设施模块存在，但业务 key、TTL、版本号、回源和失效策略仍归属对应 infra/cache support 或业务 Store/DAO。

## 4. Rationale

建立 common-cache 的原因：

- 当前目标从单点 CrudService 缓存迁移扩大为删除 common-core `RedisClient`。
- `RedisClient` 已经成为跨层通用 Redis 客户端，继续保留会让 API、biz、infra 都能直接触碰 Redis。
- JetCache 是缓存能力，不应散落在每个业务模块重复配置。
- 参考 `../bacon`，JetCache 注解启用和项目级默认配置适合放在 common 技术模块。
- common-cache 能把“缓存基础设施”从 common-core 拆出，符合 common 收敛后减少无关工具堆叠的方向。

## 5. Fixed Boundary

固定边界：

- `sandwish-common-cache` 只承载 JetCache 基线配置和极薄通用缓存能力。
- `sandwish-infra` 承载业务缓存命中、回源、回填、失效和版本维护。
- `<BusinessObject>CacheSupport` 承载业务 key、TTL、版本和失效策略。
- DAO interface 不暴露缓存语义。
- Service 不感知缓存。
- `sandwish-common-core` 不再新增 Redis/JetCache 能力。
- `RedisClient` 仅作为迁移期旧调用点存在，完成迁移后删除。

## 6. TODO Impact

`common-cache-boundary-decision` 完成后：

- `TODO.md` 切换为以 `sandwish-common-cache` 为前置的迁移顺序。
- `docs/30-designs/COMMON-CACHE-JETCACHE-RUNBOOK.md` 作为执行 runbook。
- 旧的“当前阶段不建立 common-cache”结论作废。

## 7. Open Items

无
