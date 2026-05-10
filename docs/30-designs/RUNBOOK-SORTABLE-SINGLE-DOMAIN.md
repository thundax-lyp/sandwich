# RUNBOOK-单域 Sortable 排序改造

## 1. Purpose
为单一 `Sortable` 平铺域建立可执行的改造流程，固定排序过程为 `priority` 交换，不做插值重排，且在创建时以 `max+step` 追加落位，确保排序口径稳定、可回放、可收敛。

## 2. Scope
- 适用范围：单一领域内的 `FlatSort` 排序改造（如 `Dict`、`Role`、`User`、`StoredObject`、`Member` 之一）。
- 不适用：`TreeSort`（如 `Department`、`Menu`）的树结构移动逻辑。
- 目标状态：
  - API 入参仅接收排序实体 ID 列表。
  - API 入参显式为 `orderedIds + sortDirection`（`ASC/DESC`，默认 `ASC`）。
  - Service 完成排序语义校验与重写。
  - Infra 仅执行持久化更新。
  - 新建实体 `priority` 按排序域内 `max+step` 追加，无需排序接口参与初始落位。
  - 排序方向支持 `ASC/DESC`。

## 3. Execution Order
1. 任务初始化与边界冻结
   - 锁定单域：实体类、列表查询入口、排序查询链路。
   - 在本次改造中确认无新增树形接口，属于 `FlatSort`。
   - 在改造日志中记录：
     - 实体全限定名
     - 列表查询参数（用于确定排序域）
     - 排序方向参数名与默认值
     - `step` 取值与创建边界

2. 创建路径调整（createDomain）
   - 在 Service 的新增流程中追加排序码生成：
     - 计算排序域当前 `max(priority)`。
     - 生成 `newPriority = max(priority) + step`（`step >= 1`，默认 `10`）。
     - 空域时以 `0` 作为 base。
   - 新建路径仅做落位，不触发排序重排。
   - 允许重复写入请求并发时，使用 DB 唯一冲突重试或行锁规约（由已有唯一约束与 Service 重试策略兜底）。

3. API 层准备（不落地实现，仅形成改造清单）
   - 定义/更新重排入参为 `orderedIds + sortDirection`。
   - `sortDirection` 仅支持枚举：`ASC`、`DESC`，默认 `ASC`。
   - 从入参、响应、文档示例中移除 `priority` 直传与回传。
   - 不新增任何 `id` 以外的排序权重字段。

4. Service 层改造清单（核心）
   - 获取目标排序域当前可见实体 ID 集合。
   - 校验输入：
     - `orderedIds` 非空
     - 无重复
     - 与目标集合一致（不存在缺失、越界、超集）
     - 越权实体全部拒绝
   - 确认 `orderedIds` 与目标域一致性：默认要求完整覆盖目标域。
   - 生成交换序列（交换式，不插值）：
     - 计算目标顺序映射（`targetIndex`）。
     - 以 `currentIndex` 与 `targetIndex` 的错位对进行最小交换对齐（例如 transposition）。
     - 每次交换仅改两个实体的 `priority`。
   - 在事务内按交换序列执行，禁用插值、禁用 `1,2,3...` 重写。
   - 不做 UI/前端业务语义判断。
   - 统一返回错误码：
     - `SORT_MISSING_ID`
     - `SORT_DUPLICATE_ID`
     - `SORT_EMPTY_INPUT`
     - `SORT_CONCURRENT_MODIFICATION`
     - `SORT_DB_FAILURE`

5. Infra 层改造清单
   - 保留 Service 的交换序列输出，执行单次/批量更新。
   - 单次交换使用临时优先级规避唯一约束冲突：
     - 读取参与交换实体的 `oldPriorityA`、`oldPriorityB`。
     - `UPDATE A SET priority = tmp`（`tmp` 取域内当前最大值 + 1 或业务约定安全哨兵）。
     - `UPDATE B SET priority = oldPriorityA`。
     - `UPDATE A SET priority = oldPriorityB`。
   - 每次交换完成后立即持久化当前步结果。
   - 不在 SQL 层引入排序策略，更新语句与 Service 计算保持一一对应。
   - 重试/幂等策略由 Service 触发，Infra 仅保留持久化能力。

6. 并发与事务收口
   - Service 统一在事务中完成整域交换，失败即回滚。
   - 先按实体 ID 升序对参与实体加锁（`FOR UPDATE`），统一锁定顺序避免死锁。
   - 锁定与更新范围必须覆盖交换序列涉及的所有实体（或域内候选全集，按本次策略选择）。
   - 并发重试策略：
     - 同一排序请求最多重试 3 次。
     - 捕获数据库锁等待超时、死锁、唯一约束冲突与快照提交失败，判定为可重试并映射为 `SORT_CONCURRENT_MODIFICATION`。
     - 超出重试上限映射为 `SORT_DB_FAILURE`。
   - 数据库错误码执行映射（示例）：
     - MySQL：
       - `ErrorCode=1205`：`Lock wait timeout exceeded; try restarting transaction` -> `SORT_CONCURRENT_MODIFICATION`
       - `ErrorCode=1213`：`Deadlock found when trying to get lock` -> `SORT_CONCURRENT_MODIFICATION`
       - `ErrorCode=1207`（可选）：`Can't execute because table was locked` -> `SORT_CONCURRENT_MODIFICATION`
       - `ErrorCode=1062`：`Duplicate entry`（唯一约束冲突）-> `SORT_CONCURRENT_MODIFICATION`
     - PostgreSQL：
       - `SQLState=55P03`：`lock_not_available` -> `SORT_CONCURRENT_MODIFICATION`
       - `SQLState=40P01`：`deadlock_detected` -> `SORT_CONCURRENT_MODIFICATION`
       - `SQLState=40001`：`serialization_failure` -> `SORT_CONCURRENT_MODIFICATION`
       - `SQLState=23505`：`unique_violation`（唯一约束冲突）-> `SORT_CONCURRENT_MODIFICATION`
     - SQL Server：
       - `ErrorCode=1205`：死锁 -> `SORT_CONCURRENT_MODIFICATION`
       - `ErrorCode=1222`：锁请求超时 -> `SORT_CONCURRENT_MODIFICATION`
     - 兜底规则：数据库异常未命中上述映射时，记录完整错误码并返回 `SORT_DB_FAILURE`。

7. 链路收口与文档同步
   - 如需调整的前置约束（`priority` 是否加唯一约束、索引方向、现网数据清理）同步到对应数据库文档 TODO。
   - 更新领域设计文档中的列表排序约束口径（只需改动受影响域）。
   - 形成关闭条件：
     - API 不再接收/返回排序值
     - Service 可独立完成排序交换并回滚
     - Infra 仅做持久化

## 8. Verification
1. 结构一致性
   - 创建路径落位为 `max+step`，无排序接口参与初始顺序决定。
   - 查询链路仍只按 `priority` + 排序方向下推。
   - API 协议不包含 `priority` 字段。
2. 逻辑一致性
   - 新建实体 `priority` 大于域内当前最大值。
   - `orderedIds` 与目标域差异直接拒绝并返回 `SORT_MISSING_ID`。
   - 重复输入返回 `SORT_DUPLICATE_ID`。
   - 交换前后仅涉及参与交换实体的 `priority` 变更。
3. 并发与回滚
   - 模拟并发重排，必须避免部分更新；失败统一走 `SORT_DB_FAILURE` 或并发错误码。
4. 稳态校验
    - 重复提交同一 `orderedIds`，最终顺序保持不变。
    - 查询刷新与提交顺序一致。
    - 连续建新实体后，通过排序移动可再次稳定调整（无重复 `priority`）。

## 9. Rollback
- 回滚方式：撤销本域内 Service 与 API 改造提交，恢复到改造前 Query/Command 签名。
- 回滚判据：任何一次收口验证失败且无法在当前提交内修正。

## 10. Open Items
无
