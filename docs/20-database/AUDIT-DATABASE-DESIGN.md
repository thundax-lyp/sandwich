# AUDIT DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 数据审计模块的数据库表、字段映射、关系约束和持久化规则。

本文档以 `AUDIT-REQUIREMENTS.md` 的数据审计模型为基础，固定 `audit` 拥有的审计元数据和审计日志目标持久化设计。

建表 SQL 见 [`../../db/schema/audit.sql`](../../db/schema/audit.sql)，初始化脚本见 [`../../db/data/audit.sql`](../../db/data/audit.sql)。

## 2. Scope

当前覆盖范围：

- `audit_meta`
- `audit_log`
- 对应 `DO/DataObject`
- 对应 MyBatis Mapper
- 对应 DAO implementation
- 对应 `PersistenceAssembler`

当前不覆盖范围：

- `sys_log`
- 失败请求日志
- 登录、登出和安全事件日志
- 外部审计投递表
- outbox 派生表
- 生产数据变更脚本

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- Audit 表固定使用 `audit_` 前缀。
- Audit 自身表主键固定使用雪花 ID。
- Audit 自身表主键数据库类型固定为 `bigint`。
- Audit 自身 `DO/DataObject.id` Java 类型固定为 `Long`。
- Audit 自身表主键由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `audit_meta.object_id` 和 `audit_log.object_id` 固定使用字符串表达被审计对象标识。
- `audit_meta.version` 和 `audit_log.version` 表达审计版本。
- `audit_meta.version` 固定只表达审计版本，业务对象 `version` 固定由业务对象表保存。
- Audit 表字段固定由审计游标、操作事实、快照证据和查询条件组成。
- Audit 表生命周期固定通过审计追加和审计版本推进表达。
- `audit_log` 固定只追加，不回写既有记录。
- `before_snapshot`、`after_snapshot` 和 `changed_fields` 固定保存 JSON 文本。
- snapshot 字段固定只保存脱敏后的审计展示字段。

## 4. Naming Rules

- 审计元数据表固定为 `audit_meta`。
- 审计日志表固定为 `audit_log`。
- 审计对象类型列固定为 `object_type`。
- 审计对象标识列固定为 `object_id`。
- 审计版本列固定为 `version`。
- 审计幂等键列固定为 `idempotency_key`。
- 快照结构版本列固定为 `snapshot_schema_version`。
- 变更前快照列固定为 `before_snapshot`。
- 变更后快照列固定为 `after_snapshot`。
- 字段变更摘要列固定为 `changed_fields`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `audit_meta` | `AuditMetaDO` | `AuditMetaMapper` | `AuditMeta` |
| `audit_log` | `AuditLogDO` | `AuditLogMapper` | `AuditLog` |

## 6. Table Design

### 6.1 audit_meta

`audit_meta` 保存一个 `AuditObjectRef` 的当前审计状态。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 审计元数据主键，雪花 ID |
| `object_type` | `objectType` | `objectType` | 是 | 被审计对象类型 |
| `object_id` | `objectId` | `objectId` | 是 | 被审计对象标识，字符串表达 |
| `version` | `version` | `version` | 是 | 当前审计版本 |
| `last_log_id` | `lastLogId` | `lastLogId` | 是 | 最后一条审计日志 ID |
| `last_action` | `lastAction` | `lastAction` | 是 | 最后一次审计动作 |
| `last_operator_type` | `lastOperatorType` | `lastOperatorType` | 是 | 最后一次操作者类型 |
| `last_operator_id` | `lastOperatorId` | `lastOperatorId` | 否 | 最后一次操作者 ID |
| `last_operator_name` | `lastOperatorName` | `lastOperatorName` | 否 | 最后一次操作者显示名 |
| `last_operated_at` | `lastOperatedAt` | `lastOperatedAt` | 是 | 最后一次操作发生时间 |
| `created_log_id` | `createdLogId` | `createdLogId` | 是 | 创建审计元数据对应的审计日志 ID |
| `created_at` | `createdAt` | `createdAt` | 是 | 审计元数据创建时间 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `object_type + object_id` 固定唯一。
- `version` 从 `1` 开始，随每条成功审计日志递增。
- `last_log_id` 固定指向同一个 `AuditObjectRef` 的最后一条 `audit_log.id`。
- `created_log_id` 固定指向同一个 `AuditObjectRef` 的第一条 `audit_log.id`。
- `audit_meta` 直接展开保存 `object_type` / `object_id`。

索引：

- 主键：`pk_audit_meta(id)`
- 联合唯一索引：`uk_audit_meta_object(object_type, object_id)`
- 普通索引：`idx_audit_meta_last_operated(last_operated_at)`
- 普通索引：`idx_audit_meta_last_operator(last_operator_type, last_operator_id, last_operated_at)`

### 6.2 audit_log

`audit_log` 保存一个 `AuditObjectRef` 的一次成功数据变更事实。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 审计日志主键，雪花 ID |
| `meta_id` | `metaId` | `metaId` | 是 | 审计元数据 ID |
| `object_type` | `objectType` | `objectType` | 是 | 被审计对象类型 |
| `object_id` | `objectId` | `objectId` | 是 | 被审计对象标识，字符串表达 |
| `version` | `version` | `version` | 是 | 本次审计后的审计版本 |
| `previous_version` | `previousVersion` | `previousVersion` | 是 | 本次审计前的审计版本 |
| `action` | `action` | `action` | 是 | 审计动作 |
| `idempotency_key` | `idempotencyKey` | `idempotencyKey` | 是 | 审计幂等键 |
| `operator_type` | `operatorType` | `operatorType` | 是 | 操作者类型 |
| `operator_id` | `operatorId` | `operatorId` | 否 | 操作者 ID |
| `operator_name` | `operatorName` | `operatorName` | 否 | 操作者显示名 |
| `source` | `source` | `source` | 是 | 操作来源 |
| `request_id` | `requestId` | `requestId` | 否 | 请求标识 |
| `trace_id` | `traceId` | `traceId` | 否 | 链路标识 |
| `remote_addr` | `remoteAddr` | `remoteAddr` | 否 | 请求来源地址 |
| `summary` | `summary` | `summary` | 否 | 审计摘要 |
| `snapshot_schema_version` | `snapshotSchemaVersion` | `snapshotSchemaVersion` | 是 | 快照结构版本 |
| `before_snapshot` | `beforeSnapshot` | `beforeSnapshot` | 否 | 变更前审计快照 JSON |
| `after_snapshot` | `afterSnapshot` | `afterSnapshot` | 否 | 变更后审计快照 JSON |
| `changed_fields` | `changedFields` | `changedFields` | 否 | 字段变更摘要 JSON |
| `occurred_at` | `occurredAt` | `occurredAt` | 是 | 操作发生时间 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `meta_id` 复用 `audit_meta.id`。
- `object_type` / `object_id` 冗余保存，用于按对象查询和保证日志证据自包含。
- `previous_version` 在对象第一条审计日志中固定为 `0`。
- `idempotency_key` 固定唯一。
- `snapshot_schema_version` 从 `1` 开始。
- `before_snapshot`、`after_snapshot` 和 `changed_fields` 固定由审计快照对象序列化生成。
- `audit_log` 固定通过追加记录保留审计历史。

索引：

- 主键：`pk_audit_log(id)`
- 联合唯一索引：`uk_audit_log_idempotency(idempotency_key)`
- 普通索引：`idx_audit_log_object(object_type, object_id, occurred_at)`
- 普通索引：`idx_audit_log_meta_version(meta_id, version)`
- 普通索引：`idx_audit_log_operator(operator_type, operator_id, occurred_at)`
- 普通索引：`idx_audit_log_action(action, occurred_at)`
- 普通索引：`idx_audit_log_request(request_id)`
- 普通索引：`idx_audit_log_trace(trace_id)`
- 普通索引：`idx_audit_log_occurred(occurred_at)`

## 7. Relationship Rules

- `audit_meta` 与 `AuditObjectRef` 是一对一关系。
- `audit_log` 与 `AuditObjectRef` 是多对一关系。
- `audit_log.meta_id` 指向 `audit_meta.id`。
- 跨表一致性固定由 Service 事务、唯一约束和版本推进共同保证。
- `object_type + object_id` 是跨业务域审计对象坐标，固定独立于被审计对象主表结构。
- 被审计对象删除后，审计记录必须继续保留。

## 8. Persistence Rules

- `sandwish-biz` 固定保留 `AuditMeta`、`AuditLog`、`AuditObjectRef`、Service 和 DAO interface。
- `sandwish-infra` 固定承载 `AuditMetaDO`、`AuditLogDO`、Mapper、DAO implementation 和 `PersistenceAssembler`。
- `AuditObjectRef` 是值对象，不单独持久化为表。
- `AuditMetaDO.id` 和 `AuditLogDO.id` 固定使用 `Long`。
- `AuditLogDO.metaId` 固定使用 `Long`，复用 `AuditMetaDO.id`。
- `AuditMetaDO.objectId` 和 `AuditLogDO.objectId` 固定使用 `String`。
- `AuditLogDO` 只提供 insert 和 query 持久化能力。
- `AuditMetaDO` 只允许创建和推进版本，不承载业务对象状态。
- DAO implementation 负责保证同一 `AuditObjectRef` 的 `audit_meta.version` 连续推进。
- DAO implementation 负责用 `idempotency_key` 唯一约束防止重复审计日志。

## 9. Query Model Rules

- 后台按 `object_type + object_id` 查询对象审计历史时，固定读取 `audit_log`。
- 后台按 `object_type + object_id` 查询当前审计状态时，固定读取 `audit_meta`。
- 后台审计日志列表固定支持按 `object_type`、`object_id`、`action`、`operator_type`、`operator_id`、`source`、`request_id` 和 `occurred_at` 时间范围过滤。
- 审计日志分页排序固定使用 `occurred_at desc, id desc`。
- 审计对象历史分页排序固定使用 `version desc`。
- 审计查询固定由后台 API 承载。

## 10. Open Items

无
