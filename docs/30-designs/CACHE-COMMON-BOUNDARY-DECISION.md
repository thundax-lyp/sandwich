# CACHE COMMON BOUNDARY DECISION

## 1. Purpose

本文档记录 `sandwish-common-cache` 是否建立的边界决策。

该决策基于当前缓存现状盘点和 infra 缓存契约设计，用于指导后续 CrudService 缓存迁移任务。

## 2. Scope

当前范围：

- 是否建立 `sandwish-common-cache`
- common 与 infra 的缓存职责边界
- 后续 TODO 收窄方向

不在范围内：

- 不新增 Maven 模块
- 不引入 JetCache
- 不迁移任何业务缓存链路
- 不删除 `CrudServiceImpl` 缓存方法

## 3. Decision

当前阶段不建立 `sandwish-common-cache`。

后续 CrudService 缓存迁移固定先在 `sandwish-infra` 内部建立业务专用 `CacheSupport`。

## 4. Rationale

当前不满足建立 `sandwish-common-cache` 的条件：

- 首条 `Dict` 迁移只需要 `DictCacheSupport`，不需要跨模块公共缓存抽象。
- 当前 key、TTL、版本号和失效时机都带明确业务语义，应归属 infra。
- 当前尚未完成任何一条链路迁移，无法稳定判断 common 抽象是否能减少真实重复。
- 过早建立 common 模块会增加 Maven 模块、包路径和抽象层，不能直接降低 `CrudServiceImpl` 当前缓存复杂度。
- JetCache 尚未引入，当前阶段不需要为候选实现提前建立公共适配层。

## 5. Fixed Boundary

当前阶段固定边界：

- `sandwish-infra` 承载业务缓存命中、回源、回填、失效和版本维护。
- `<BusinessObject>CacheSupport` 承载业务 key、TTL、版本和失效策略。
- DAO interface 不暴露缓存语义。
- Service 不感知缓存。
- `sandwish-common-core` 中的 `RedisClient` 继续作为迁移期底层技术能力。
- 不新增 `sandwish-common-cache`。

## 6. TODO Impact

`common-cache-boundary-decision` 完成后：

- 删除 `TODO.md` 中的 `common-cache-boundary-decision`。
- 删除 `docs/30-designs/COMMON-CACHE-RUNBOOK.md`。
- `cache-dict-chain-migration` 不再依赖 `common-cache-boundary-decision`。
- `cache-dict-chain-migration` 固定在迁移中新增 `DictCacheSupport`，不等待 common 模块。

后续如果至少完成一条业务链路迁入 infra，并出现两个以上业务链路重复的无业务语义缓存能力，再新增独立 TODO 重新评估 common 抽象。

## 7. Open Items

无
