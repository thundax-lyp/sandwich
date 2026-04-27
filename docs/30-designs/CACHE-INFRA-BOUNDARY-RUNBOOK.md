# CACHE INFRA BOUNDARY RUNBOOK

## 1. Purpose

本文档指导将 `CrudServiceImpl` 中的缓存职责迁移到 infra。

目标是让 Service 只表达业务动作和事务编排，不再感知 Redis、JetCache、缓存 key、TTL、预加载、版本号或批量失效策略；缓存作为读取优化和持久化访问策略，固定归属 infra。

本手册先指导任务拆分，再指导逐项迁移。不得把本手册当作一次性全仓改造许可。

## 2. Scope

当前范围：

- `CrudServiceImpl` 中的缓存职责
- `removeAllCache`
- `preload`
- `getCacheVersion`
- `isRedisCacheEnabled`
- `getRedisCache` / `putRedisCache` / `removeRedisCache`
- 业务 `*ServiceHolder.removeAllCache`
- 直接继承 `CrudServiceImpl` 且启用缓存的业务链路
- infra DAO / Repository / cache support
- JetCache 作为候选实现

不在范围内：

- 不一次性删除 `CrudServiceImpl`
- 不一次性替换全仓 `RedisClient`
- 不改认证、会话、验证码、登录锁等非 CrudService 缓存
- 不把 JetCache 类型暴露到 Service、Controller 或 Entity
- 不改变业务查询语义
- 不为了缓存迁移同步重写 DAO / Mapper / XML

## 3. Fixed Boundary

固定口径：

- Service 不负责缓存读写、缓存失效、缓存预热和缓存版本维护
- `CrudServiceImpl` 不再作为缓存模板基类
- 缓存实现属于 infra
- JetCache 只作为 infra 缓存实现细节
- RedisClient 迁移期允许继续服务非 CrudService 缓存
- 缓存命中、回源、回填和失效必须对 Service 透明

禁止：

- 在 Service 方法上新增 JetCache 注解作为第一阶段方案
- 在 Entity、Request、Response 中加入缓存字段
- 用缓存迁移顺手改变分页、查询条件或持久化模型
- 用全局 pattern delete 作为长期缓存失效策略

## 4. Required TODO Split First

执行本手册前，必须先将 `TODO.md` 中的缓存迁移入口拆成可审阅的子任务。

拆分后的 TODO 固定满足：

- 第一项只做现状盘点，不改代码
- 每个执行项只迁移一条业务链路或一个公共支撑能力
- 每项都能独立验收
- 每项都能独立提交
- 每项都说明是否允许引入 JetCache
- 每项都说明是否允许删除 `CrudServiceImpl` 中的对应方法

禁止：

- 写一个“迁移缓存到 infra”的笼统大任务直接执行
- 未经人类审阅就展开多条链路改造
- 将 JetCache 引入、缓存边界迁移、`CrudServiceImpl` 删除混在同一项

## 5. Recommended TODO Children

首次拆分时固定按以下候选项生成 TODO，之后由人类审阅、删减或调整顺序：

1. `cache-current-usage-inventory`
   - 盘点哪些 Service 覆写 `isRedisCacheEnabled`
   - 盘点哪些调用依赖 `removeAllCache`、`preload`、`getCacheVersion`
   - 输出首条迁移链路建议

2. `cache-infra-contract-design`
   - 固定 infra 缓存支撑对象命名、包路径和职责
   - 固定缓存 key、TTL、失效和版本策略
   - 不引入 JetCache，不改业务链路

3. `cache-first-chain-migration`
   - 选择一条读多写少且缓存语义清楚的链路
   - 将缓存命中、回源、回填、失效迁入 infra
   - Service 不再调用缓存相关方法

4. `cache-jetcache-introduction`
   - 在缓存边界已经迁入 infra 后引入 JetCache
   - 固定版本、依赖模块和基础配置
   - 不迁移额外业务链路

5. `crud-service-cache-method-removal`
   - 所有 CrudService 缓存调用迁出后，删除 `CrudServiceImpl` 缓存方法
   - 收窄或删除 `CrudService.removeAllCache` 等公共契约

6. `crud-service-thinning`
   - 在缓存、分页、通用 CRUD 职责分别迁出后，评估 `CrudServiceImpl` 是否继续保留
   - 不与缓存首链路迁移混在同一提交

## 6. Candidate First Chain Rules

首条链路必须满足：

- 缓存开启方式明确
- 主要缓存按 `id` 获取的单对象
- 写操作失效范围清楚
- 不涉及登录态、权限认证、会话或验证码
- 不涉及树移动、复杂批量写和跨服务联动

优先候选：

- `Dict`
- `UploadFile`
- 简单系统配置类

不作为首条候选：

- `User`
- `Role`
- `Menu`
- `Office`
- `Member`
- 认证和会话相关链路

## 7. Target Infra Shape

每条缓存链路迁移后的目标形态：

```text
Service
  -> Dao interface
      -> infra Dao implementation
          -> CacheSupport
          -> Mapper / Database
```

职责固定为：

- Service 调用 DAO，不感知缓存
- DAO interface 表达业务数据访问契约
- infra DAO implementation 决定是否走缓存
- CacheSupport 负责 key、TTL、命中、回填、失效
- Mapper 只负责数据库访问

## 8. JetCache Introduction Rules

JetCache 引入必须发生在缓存边界迁入 infra 之后。

版本固定先评估 Spring Boot 兼容性，不照搬其他项目版本。

固定要求：

- 依赖放在最窄可行 common 子模块或 infra 支撑模块
- 配置只启用项目需要的能力
- JetCache 类型不出现在 Service / Controller / Entity
- 迁移期保留 `RedisClient` 服务非 CrudService 缓存

## 9. Execution Steps Per Chain

单条链路迁移固定步骤：

1. 读取当前 Service、DAO、infra DAO、Mapper 和 Holder 调用
2. 盘点当前缓存 key、TTL、失效触发点和调用方
3. 在 infra 新增或复用 CacheSupport
4. 将 `get` / `getMany` 缓存逻辑迁入 infra DAO implementation
5. 将写操作后的缓存失效迁入 infra DAO implementation 或明确的 infra 支撑对象
6. 删除当前 Service 对缓存方法的直接调用
7. 保持 Service 对外行为不变
8. 编译验证
9. 删除或收窄对应 TODO
10. 单独提交

## 10. Verification

每个执行子任务固定验证：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

检查项：

- 当前迁移链路 Service 不再直接读写缓存
- 当前迁移链路缓存实现只在 infra 或 common cache support 中出现
- 非 CrudService 的 RedisClient 使用未被误改
- TODO 已删除、拆分或收窄
- 本次提交不夹带无关链路改造

## 11. Completion Criteria

缓存边界迁移完成必须满足：

- `CrudServiceImpl` 不再包含缓存职责
- `CrudService` 公共契约不再暴露缓存失效能力
- 业务 Service 不再调用 `removeAllCache`、`preload` 或 `getCacheVersion`
- CrudService 缓存全部由 infra 承接
- JetCache 如已引入，只作为 infra 实现细节
- `RedisClient` 只保留给确有 Redis 语义的非 CrudService 能力

## 12. Open Items

无
