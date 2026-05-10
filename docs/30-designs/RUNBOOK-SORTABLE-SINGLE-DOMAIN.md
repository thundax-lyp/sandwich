# RUNBOOK-单域 Sortable 排序改造

## 1. Purpose
为单一 `Sortable` 平铺域建立可执行的改造流程，保证排序逻辑仅由服务端统一下沉，`priority` 不在 API 层暴露，并能在一次改造中完成边界、链路和收口。

## 2. Scope
- 适用范围：单一领域内的 `FlatSort` 排序改造（如 `Dict`、`Role`、`User`、`StoredObject`、`Member` 之一）。
- 不适用：`TreeSort`（如 `Department`、`Menu`）的树结构移动逻辑。
- 目标状态：
  - API 入参仅接收排序实体 ID 列表。
  - Service 完成排序语义校验与重写。
  - Infra 仅执行持久化更新。
  - 排序方向支持 `ASC/DESC`。

## 3. Execution Order
1. 任务初始化与边界冻结
   - 锁定单域：实体类、列表查询入口、排序查询链路。
   - 在本次改造中确认无新增树形接口，属于 `FlatSort`。
   - 在改造日志中记录：
     - 实体全限定名
     - 列表查询参数（用于确定排序域）
     - 排序方向参数名与默认值

2. API 层准备（不落地实现，仅形成改造清单）
   - 定义/更新重排入参为 `orderedIds`。
   - 从入参、响应、文档示例中移除 `priority` 直传与回传。
   - 不新增任何 `id` 以外的排序权重字段。

3. Service 层改造清单（核心）
   - 获取目标排序域当前可见实体 ID 集合。
   - 校验输入：
     - `orderedIds` 非空
     - 无重复
     - 与目标集合一致（不存在缺失、越界、超集）
     - 越权实体全部拒绝
   - 确认 `sortedIds` 顺序与目标域一致性：必须覆盖目标域全部元素。
   - 根据排序方向生成 `priority` 写入序列：
     - `ASC`：`1, 2, 3...`
     - `DESC`：`N, N-1, ...`
   - 在事务内编排批量更新，不做 UI/前端业务语义判断。
   - 统一返回错误码：
     - `SORT_MISSING_ID`
     - `SORT_DUPLICATE_ID`
     - `SORT_EMPTY_INPUT`
     - `SORT_CONCURRENT_MODIFICATION`
     - `SORT_DB_FAILURE`

4. Infra 层改造清单
   - 保留 Service 的排序判断结果，执行批量更新。
   - 单次调用内仅更新 `priority`，禁止顺序副作用。
   - 更新语句必须与目标域实体 ID 一一对应。
   - 重试/幂等策略由 Service 触发，不在 SQL 层追加隐式排序策略。

5. 链路收口与文档同步
   - 如需调整的前置约束（`priority` 是否加唯一约束、索引方向、现网数据清理）同步到对应数据库文档 TODO。
   - 更新领域设计文档中的列表排序约束口径（只需改动受影响域）。
   - 形成关闭条件：
     - API 不再接收/返回排序值
     - Service 可独立完成排序重算并回滚
     - Infra 仅做持久化

## 4. Verification
1. 结构一致性
   - 查询链路仍只按 `priority` + 排序方向下推。
   - API 协议不包含 `priority` 字段。
2. 逻辑一致性
   - `orderedIds` 与目标域差异直接拒绝并返回 `SORT_MISSING_ID`。
   - 重复输入返回 `SORT_DUPLICATE_ID`。
3. 并发与回滚
   - 模拟并发重排，必须避免部分更新；失败统一走 `SORT_DB_FAILURE` 或并发错误码。
4. 稳态校验
   - 重复提交同一 `orderedIds`，最终顺序保持不变。
   - 查询刷新与提交顺序一致。

## 5. Rollback
- 回滚方式：撤销本域内 Service 与 API 改造提交，恢复到改造前 Query/Command 签名。
- 回滚判据：任何一次收口验证失败且无法在当前提交内修正。

## 6. Open Items
无
