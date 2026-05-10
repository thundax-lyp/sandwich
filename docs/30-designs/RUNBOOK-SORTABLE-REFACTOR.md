# RUNBOOK: Sortable 排序专项改造（全局版本）

> 目的：废弃旧版 RUNBOOK，统一建立一份面向全量 Sortable 的可执行改造手册。本文聚焦“可落地、可验收、可排障”。

## 1. 目标
- 统一将所有 `FlatSort` 排序改为“前端提交 `orderedIds + sortDirection`，服务端交换 `priority`”模式。
- 将 `priority` 完全交给服务端管理，禁止外部直接输入。
- 保持树形列表与平铺列表排序链路彻底解耦，树形仅保留树操作。
- 在并发场景保证原子性与可回滚。

## 2. 术语与边界
- **实体（Entity）**：实现 `Sortable` 的领域对象。
- **域（Scope）**：排序集合，必须是可验证的列表边界（如 `type`、`status`、`departmentId` 等）。
- **FlatSort**：平铺列表排序，使用 `priority`。
- **TreeSort**：树形排序，使用 `lft/rgt`，不走本 RUNBOOK 的 priority 重排。
- **有序输入**：`orderedIds` 表示目标顺序。

## 3. 全局统一约束（必须遵守）
1. API 入参只允许 `orderedIds + sortDirection`。
2. 允许 `sortDirection = ASC | DESC`，默认 `ASC`。
3. `priority` 不出现在请求体、响应体、命令模型中。
4. 排序过程只允许执行“实体间 priority 交换”，严禁插值重排（如 `1,2,3...` 全量重算）。
5. `FlatSort` 全局 `priority` 唯一（不分 domain）。
6. 单次排序请求在事务内完成，失败全回滚。
7. 并发冲突返回统一错误码。
8. 任何排序校验失败按错误码快速失败，不允许部分更新。

## 4. 旧链路迁移策略（从旧版到新版）
- 替换所有单点 priority 修改接口（如 `changePriority`）为批量排序接口。
- 剔除列表快照、列表签名等非核心字段。
- 统一排序接口、响应与错误码。
- 建立 `priority` 全局唯一约束，避免重复值导致交换排序冲突。

## 5. 统一 API 协议
- 请求体：
  - `orderedIds: List<Long>`（或对应 Id 类型）
  - `sortDirection: SortDirection`（可选，默认 `ASC`）
- 响应体：
  - `success`/`errorCode` 或现有统一响应约定
  - 不返回排序细节，不返回 `priority`

### 错误码映射
- `SORT_EMPTY_INPUT`
- `SORT_DUPLICATE_ID`
- `SORT_MISSING_ID`
- `SORT_CONCURRENT_MODIFICATION`
- `SORT_DB_FAILURE`

## 6. 排序算法（交换式）
- 在服务层构建目标顺序索引图后，仅对错位实体生成最小交换序列。
- 每次交换两个实体的 `priority`：
  1) 读取 A/B 当前 `priority`
  2) 将 A 更新为中间值（tmp）
  3) 将 B 更新为 A 原值
  4) 将 A 更新为 B 原值
- 所有交换在同一事务内执行。
- 说明：`step` 不参与排序运算，`step` 仅用于新增实体初始落位。

## 7. 创建路径（Create Domain）
- 创建新实体时不参与排序链路决策。
- 新增 `priority` 写法：
  - `newPriority = max(priority) + step`
  - `step = 10`（固定）
- 空域：从 `10` 起始。
- 由 DB 全局唯一约束与服务重试兜底并发。

## 8. 并发与事务
- 排序方法使用事务。
- 影响实体范围应尽可能一次性加锁（按 ID 排序加锁，避免死锁）。
- 重试策略：建议 3 次，超限返回错误码。
- 并发异常映射：
  - MySQL `1205/1213/1207/1062`
  - PostgreSQL `55P03/40P01/40001/23505`
  - SQL Server `1205/1222`
  - 未覆盖到的异常写入原始错误码并返回 `SORT_DB_FAILURE`

## 9. 数据库约束
- FlatSort 必须满足全局唯一约束：`UNIQUE(priority)`。
- 若历史存在重复，先清洗再建约束。
- 列表查询索引应覆盖 `scope + priority + id`（或当前最常用过滤字段 + priority）。

## 10. 全量落地执行步骤（按域）

### 10.1 预检查
- 产出当前 Sortable 全量清单（含当前排序域定义）。
- 标注 FlatSort 与 TreeSort。
- 识别受影响文件（Controller / Service / DAO / SQL）。

### 10.2 API 改造（每个 FlatSort 域）
- 新增/替换 `sort` 接口。
- 去除所有 priority 入参。
- 接口签名统一带 `orderedIds + sortDirection`。

### 10.3 Service 改造（每个 FlatSort 域）
- `sort` 前置校验（空、重复、越界、越权、域一致）。
- 生成交换序列并执行。
- 统一异常翻译为错误码。

### 10.4 DAO/Infra 改造（每个 FlatSort 域）
- 提供域内查询排序（ASC/DESC）。
- 提供域内 `maxPriority`。
- 提供批量/单点 priority 更新能力，服务端计算序列。

### 10.5 创建路径改造（每个 FlatSort 域）
- create 时写 `max + step`。
- 不允许调用排序 API 介入新建实体顺位。

### 10.6 TreeSort 保护
- 明确不改动 TreeSort 的 FlatSort 排序。
- 保留树移动接口并做 boundary 保护。

## 11. 交付验收清单
- `FlatSort` 域不再接收 priority。
- `orderedIds` 与实际可见集合不一致时返回 `SORT_MISSING_ID`。
- 重复 id 返回 `SORT_DUPLICATE_ID`。
- `sortDirection` 生效（ASC/DESC 均验证）。
- 重复提交同一序列结果稳定。
- 并发冲突可回放，异常码可观测。
- DB 层 `priority` 全局唯一约束生效。

## 12. 任务终止条件
- 上述清单逐项通过且有对应变更记录后，可关闭 SORTABLE 改造任务。
- 未通过条目不允许收尾提交。
