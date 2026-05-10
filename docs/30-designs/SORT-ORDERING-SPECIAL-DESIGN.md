# Sortable 排序专项设计

## 1. Purpose
定义统一、可稳定还原的列表排序机制，并约束不同排序模型的使用边界。

核心目标：
- 前端在排序操作时，仅提交有序实体 ID；
- 后端通过顺序写回 `priority` 实现排序；
- `priority` 在平铺域内唯一且由服务端全权管理；
- 多用户并发情况下排序结果可预期、可重复、可回放。

## 2. Scope
- 本设计适用于需要手动调整展示顺序的列表实体。
- 树结构实体采用 `lft/rgt` 进行顺序控制，不参与 `priority` 重排。
- 本设计覆盖 `admin-api` 与 `front-api` 的可见列表排序行为。
- 本设计不定义 `Tree` 节点移动和 `lft/rgt` 的结构性重排方案。

## 3. Bounded Context
- `Sortable` 排序域内实体分为两类：
  - `FlatSort`：平铺列表，使用 `priority` 控制顺序。
  - `TreeSort`：树结构列表，使用 `lft` 控制顺序与层级。
- `priority` 仅用于 `FlatSort` 的显示顺序权重，不承载业务状态语义。
- `TreeSort` 的列表顺序固定由树结构索引产生，`priority` 不参与该链路。
- 每次 `FlatSort` 重排通过“全量顺序提交”完成，不使用局部增量交换。
- `FlatSort` 与 `TreeSort` 接口完全解耦；不共享重排入口、请求结构与返回约定。

## 4. Sortable 实体映射清单（当前全量）
以下为当前项目内所有 `implements Sortable` 的实体，按当前查询链路归一到排序域。

| 实体 | 排序模式 | 当前查询排序键 | 列表边界 |
|---|---|---|---|
| `com.github.thundax.modules.sys.entity.Dict` | `FlatSort` | `type, priority, id` | `type` |
| `com.github.thundax.modules.sys.entity.Role` | `FlatSort` | `priority, id` | `status` |
| `com.github.thundax.modules.sys.entity.User` | `FlatSort` | `priority, id` | `departmentId` 与列表过滤条件（`status`、`privilege`、`loginName`、`name`） |
| `com.github.thundax.modules.sys.entity.Department` | `TreeSort` | `lft` | `parentId` 与树边界（`lft/rgt`） |
| `com.github.thundax.modules.sys.entity.Menu` | `TreeSort` | `lft` | `parentId` 与树边界（`lft/rgt`），`visibility/maxRank` 仅作过滤 |
| `com.github.thundax.storage.entity.StoredObject` | `FlatSort` | `id DESC, priority ASC` | `objectStatus` 与列表过滤条件（`mimeType`、`ownerId`、`ownerType`、`referenceStatus`、`referenceOwnerId`、`referenceOwnerType`、`name`、`remarks`） |
| `com.github.thundax.modules.member.entity.Member` | `FlatSort` | `priority, name` | `status` 与列表过滤条件（`name`、`remarks`） |
| `com.github.thundax.assist.entity.AsyncTask` | `FlatSort（仅定义）` | `无列表查询排序链路` | `N/A（无列表重排）` |

### 4.1 模块边界
- `sandwish-admin-api` / `sandwish-front-api`：
  - 提供 `FlatSort` 重排接口与 `TreeSort` 专用重排接口（若树形链路新增可按树形语义独立实现）。
  - 前端不允许提交 `priority` 数值。
- `sandwish-biz`：
  - 实现 `FlatSort` 与 `TreeSort` 分域校验：排序域完整性、越权检测。
- `sandwish-infra`：
  - 提供重排写库能力：`FlatSort` 仅更新 `priority`；`TreeSort` 仅更新树结构索引或树移动字段。
- 两类排序链路不能互相调用彼此入口，也不共享幂等键/并发键。

## 5. Core Business Objects
- `SortableEntity`
  - 标识：`id`
  - 排序键：`priority`
- `SortMode`
  - `FlatSort`：`priority` 重排模式。
  - `TreeSort`：`lft/rgt` 树结构排序模式。

## 6. Global Constraints
1. `priority` 不允许外部任意输入。
2. 仅 `FlatSort` 域支持 `priority` 重排；`TreeSort` 不支持 `priority` 重排。
3. `TreeSort` 列表查询固定按 `lft` 排序；`FlatSort` 列表查询固定按 `priority` 排序。
4. `FlatSort` 同一次排序域内 `priority` 唯一。
5. 重排请求必须为 `orderedIds`，不接收优先级数值。
6. 重排一次覆盖该域内指定集合的全部顺序；不允许仅交换局部片段导致歧义。
7. 重排接口必须在一次事务中执行并保证幂等。
8. 返回结果包含成功与失败语义，不返回排序键重算过程。

## 7. Functional Requirements
### 7.1 前端输入契约
- 输入仅允许 `orderedIds: list<Long>`（或对应 `*Id` 类型）。
- 不允许 `priority` 随同提交。

### 7.2 服务端排序规则
- 在一次重排排序域内按 `orderedIds` 下标顺序重写 `priority`。
- 写入策略：
  - 默认步长：`1, 2, 3 ...`
  - `FlatSort` 排序域内 `priority` 全局唯一（先验收集后再重写，发现重复整体失败并回滚）。
  - 每个实体最终 `priority` 与最终顺序一一映射。
- 支持前端多次拖拽快速重试：同一 `orderedIds` 的重排结果一致。

### 7.3 排序域一致性
- 重排前校验 `orderedIds` 中每个实体都存在且可操作。
- 重排排序域与系统当前可见排序域必须一致，不一致则整体失败，不部分写入。
- 若存在已删除或无效 ID，整体失败，不部分写入。

### 7.4 校验规则
- `orderedIds` 必须非空。
- `orderedIds` 内 `id` 必须唯一。
- `orderedIds` 去重后数量必须与目标查询命中数量一致。
- 单个实体不能越权出现（不在当前用户可操作范围）。
- 任一校验失败返回明确错误码并中止更新。

### 7.5 并发与一致性
- 重排必须加锁或使用可验证的并发控制，确保并发下无交叉覆盖。
- 重排过程中任何 SQL 执行错误导致整体回滚。

### 7.6 错误语义（示例）
- `SORT_DUPLICATE_ID`：`orderedIds` 存在重复 ID。
- `SORT_MISSING_ID`：`orderedIds` 与当前可见排序域不一致（缺失/超集）。
- `SORT_CONCURRENT_MODIFICATION`：重排存在并发冲突。
- `SORT_EMPTY_INPUT`：`orderedIds` 为空。
- `SORT_DB_FAILURE`：数据库执行失败导致回滚。

### 7.7 树形列表约束
- `TreeSort` 不支持 `orderedIds` 重排。
- 树形查询顺序由 `lft` 约束，不允许与 `priority` 混排。
- `TreeSort` 与 `FlatSort` 互拆：
  - `TreeSort` 的操作仅通过树专用接口（如树节点移动）完成；
  - `FlatSort` 的操作仅通过 `orderedIds` 重排完成；
  - 两类接口互不互调，无边界回退（不存在同一请求在两套规则间切换）。

## 8. Key Flows
### 8.1 平铺排序成功流程
1. API 接收 `orderedIds`。
2. Service 校验排序域是否完整且可操作。
3. 校验输入：空值、空列表、重复、越界、越权。
4. 在事务内按顺序重写 `priority`。
5. 返回成功。

### 8.2 重排失败流程
1. 输入不合法：返回对应错误码。
2. 排序域不一致：返回 `SORT_MISSING_ID` 或 `SORT_DUPLICATE_ID`。
3. 并发冲突：返回 `SORT_CONCURRENT_MODIFICATION`。
4. 其余 SQL 错误：返回 `SORT_DB_FAILURE`。
5. 不写入任何 `priority`，保持原状。

## 9. Key Flows（数据库约束）
- `FlatSort` 对排序域内 `priority` 执行唯一约束与索引优化。
- 部署时先清理历史重复后再启用唯一约束。
- `FlatSort` 查询列表固定 `ORDER BY priority ASC`，按约定追加必要过滤条件。
- `TreeSort` 查询列表固定 `ORDER BY lft ASC`，并保持树形边界约束。

## 10. Non-Functional Requirements
- 重排 API 在高并发下可用且可回放。
- 重排执行时间与实体数量近似线性。
- 所有排序响应需具备可观测日志（请求 ID、实体数量、耗时）。

## 11. Acceptance Criteria
- 任意 `FlatSort` 列表拖拽后，刷新查询后顺序与 `orderedIds` 一致。
- 重复提交同一 `orderedIds` 不产生差异值。
- 重排排序域外 ID 提交被拒绝且无半成功。
- 跨端调用（admin/front）共享同一排序语义。
- 平铺重排排序域内 `priority` 不重复（数据约束可验证）。
- 树形实体顺序仅受 `lft` 约束，`priority` 改动不影响树序结果。

## 12. Open Items
无
