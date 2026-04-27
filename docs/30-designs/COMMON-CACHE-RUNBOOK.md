# COMMON CACHE RUNBOOK

## 1. Purpose

本文档指导判断是否建立 `sandwish-common-cache`，以及建立后哪些缓存能力可以进入 common。

本手册是临时执行型 RUNBOOK。对应 TODO 完成后，必须在同一收口提交中删除本文件，避免临时迁移判断长期停留在治理文档中。

## 2. Scope

当前范围：

- `sandwish-common-cache` 是否需要作为独立模块存在
- 缓存抽象、key 规范、TTL 策略、命名空间和缓存异常处理
- JetCache 作为候选底层实现时的公共适配边界
- infra 缓存支撑对象与 common 缓存能力的职责切分
- 各模块是否需要依赖 common 缓存能力

不在范围内：

- 不直接迁移任何业务缓存链路
- 不直接引入 JetCache
- 不直接删除 `CrudServiceImpl` 缓存方法
- 不把业务缓存 key、业务失效时机或业务预加载策略放入 common
- 不改变 Service、DAO、Mapper 或 XML 行为

## 3. Fixed Boundary

固定口径：

- `common-cache` 只能承载无业务语义的缓存基础能力
- 业务缓存命中、回源、回填和失效编排归属 infra
- 业务缓存 key 的业务段、业务失效条件和跨业务联动不得进入 common
- JetCache 类型不得暴露到 Service、Controller、Entity、Request 或 Response
- 如果缓存能力只服务一条业务链路，优先留在 infra，不上升为 common 模块

## 4. Decision Gates

只有同时满足以下条件，才允许建立 `sandwish-common-cache`：

1. 至少两类模块需要复用同一套缓存基础能力
2. 能明确区分 common 抽象与 infra 业务实现
3. common 中的类名、包名和接口方法不包含具体业务对象名称
4. 引入后能让 `CrudServiceImpl` 或 infra 缓存实现变薄
5. 不要求 Service 感知缓存实现细节

任一条件不满足时，不建立 `common-cache`；先在 `sandwish-infra` 内部建立缓存支撑对象。

## 5. Candidate Shape

若允许建立模块，目标形态为：

```text
sandwish-common
  + sandwish-common-cache
      + cache key / namespace / ttl support
      + cache operation abstraction
      + optional JetCache adapter without business semantics

sandwish-infra
  + business cache support
  + Dao implementation cache orchestration
  + Mapper / Database fallback
```

## 6. Execution Steps

执行本手册时固定步骤：

1. 读取 `CACHE-INFRA-BOUNDARY-RUNBOOK.md`
2. 读取 `cache-current-usage-inventory` 的执行结果
3. 读取 `cache-infra-contract-design` 的执行结果
4. 列出候选 common 能力与必须留在 infra 的能力
5. 判断是否满足 Decision Gates
6. 若满足，拆分 `sandwish-common-cache` 建模 TODO
7. 若不满足，收窄为 infra 内部缓存支撑 TODO
8. 更新 `TODO.md`
9. 删除本 RUNBOOK
10. 单独提交

## 7. TODO Split Rules

如果决定建立 `sandwish-common-cache`，后续 TODO 必须至少拆成：

1. `common-cache-baseline-module`
   - 新增 common 子模块和最小依赖
   - 不引入 JetCache
   - 不迁移业务缓存链路

2. `common-cache-contract-support`
   - 固定 key、namespace、TTL、cache operation 等无业务语义接口
   - 不出现具体业务对象名称

3. `infra-cache-support-adapter`
   - 在 infra 中适配 common-cache
   - 承载业务缓存 key、失效和回源编排

4. `common-cache-jetcache-adapter`
   - 在边界稳定后引入 JetCache 适配
   - JetCache 类型不得越过 common-cache / infra 边界

禁止将以上内容合并成一个大 TODO。

## 8. Completion Criteria

本 RUNBOOK 对应 TODO 完成时必须满足：

- 已明确是否建立 `sandwish-common-cache`
- 已明确 common 与 infra 的缓存职责边界
- `TODO.md` 已新增或收窄后续可执行任务
- 本文件已删除
- 删除本文件与 TODO 更新在同一个 commit 中完成

