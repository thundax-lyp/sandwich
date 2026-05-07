# Audit Requirements

## 1. Purpose

本文档定义 Sandwich 数据审计模块的业务需求和工程边界。

目标是为业务对象变更提供可信、可读、可查询的数据审计能力，记录业务对象被谁、在何时、通过什么入口、执行了什么动作、从什么状态变成什么状态。

## 2. Scope

需求范围：

- 数据审计对象坐标 `AuditObjectRef`
- 审计当前状态 `audit_meta`
- 审计操作流水 `audit_log`
- 审计对象快照和字段变更摘要
- Service 写操作审计注解
- 审计对象加载与快照组装契约
- Service 写方法 Command 化、单对象写入口和业务版本要求

不在范围内：

- 不替代 `sys_log` 操作日志。
- 不记录失败请求、登录、登出和安全事件。
- 不提供业务数据回滚能力。
- 不提供完整历史版本恢复能力。
- 不把数据库技术审计字段作为业务审计能力。
- 不设计批量对象混合审计日志。

## 3. Bounded Context

数据审计模块固定记录业务对象成功提交后的数据变更事实。

`sys_log` 记录接口访问、异常和操作行为摘要。`audit_log` 记录业务对象数据变更事实。两者固定分离。

审计模块以单个业务对象为最小审计单位。一个 `audit_log` 固定只对应一个 `AuditObjectRef`。

审计写入固定与业务写操作同事务提交。业务写入成功时，审计记录必须成功；审计记录失败时，业务事务必须回滚。

## 4. Module Mapping

- `sandwish-biz`：固定承载审计领域对象、审计 Service 契约、审计注解、审计对象加载和快照组装契约。
- `sandwish-infra`：固定承载审计持久化对象、DAO implementation、Mapper 和审计持久化转换。
- `sandwish-admin-api`：固定承载后台审计查询 Controller、请求对象、响应对象和接口 assembler。审计查询固定属于后台治理能力。
- `sandwish-front-api`：禁止提供审计查询入口。如需前台展示审计相关信息，必须由具体业务用例封装成受限视图，不得直接暴露审计日志或审计元数据查询能力。
- `sandwish-common`：不承载业务审计对象、业务审计注解或业务审计规则。

## 5. Core Business Objects

### 5.1 AuditObjectRef

`AuditObjectRef` 是被审计业务对象的最小坐标。

核心字段：

- `objectType`：业务对象类型，例如 `User`、`Role`、`Menu`。
- `objectId`：业务对象标识。

固定约束：

- `AuditObjectRef` 固定使用 `objectType` / `objectId` 命名。
- `AuditObjectRef` 是值对象，不单独建表。
- `objectId` 固定使用字符串表达被审计对象标识。
- `audit_meta` 和 `audit_log` 固定展开保存 `objectType` / `objectId` 字段。
- `objectType + objectId` 是审计模块定位业务对象的固定坐标。

### 5.2 AuditMeta

`AuditMeta` 表达一个 `AuditObjectRef` 的当前审计状态。

核心字段：

- `id`：审计元数据 ID。
- `objectType`：业务对象类型。
- `objectId`：业务对象标识。
- `version`：当前审计版本。
- `lastLogId`：最后一条审计日志 ID。
- `lastAction`：最后一次审计动作。
- `lastOperatorType`：最后一次操作者类型。
- `lastOperatorId`：最后一次操作者 ID。
- `lastOperatorName`：最后一次操作者显示名。
- `lastOperatedAt`：最后一次操作发生时间。
- `createdLogId`：创建对象对应的审计日志 ID。
- `createdAt`：审计元数据创建时间。

固定约束：

- `objectType + objectId` 固定唯一。
- `AuditMeta` 与 `AuditObjectRef` 是一对一关系。
- `AuditMeta` 不通过外键引用 `AuditObjectRef`，而是直接保存 `objectType` / `objectId`。
- `version` 只表达审计版本，不替代业务对象版本。
- 审计版本从 `1` 开始，随每条成功审计日志递增。
- `AuditMeta` 只表达审计游标和最后状态，不表达业务对象本身状态。

### 5.3 AuditLog

`AuditLog` 表达一个 `AuditObjectRef` 的一次成功数据变更。

核心字段：

- `id`：审计日志 ID。
- `metaId`：审计元数据 ID。
- `objectType`：业务对象类型。
- `objectId`：业务对象标识。
- `version`：本次审计后的审计版本。
- `previousVersion`：本次审计前的审计版本。
- `action`：审计动作。
- `idempotencyKey`：审计幂等键。
- `operatorType`：操作者类型。
- `operatorId`：操作者 ID。
- `operatorName`：操作者显示名。
- `source`：操作来源。
- `requestId`：请求标识。
- `traceId`：链路标识。
- `remoteAddr`：请求来源地址。
- `summary`：审计摘要。
- `snapshotSchemaVersion`：快照结构版本。
- `beforeSnapshot`：变更前审计快照 JSON。
- `afterSnapshot`：变更后审计快照 JSON。
- `changedFields`：字段变更摘要 JSON。
- `occurredAt`：操作发生时间。

固定约束：

- `idempotencyKey` 固定唯一。
- `AuditLog` 只记录成功提交的数据变更。
- 一个 `AuditLog` 固定只对应一个 `AuditObjectRef`。
- `AuditLog` 通过 `metaId` 关联 `AuditMeta`。
- `AuditLog` 直接保存 `objectType` / `objectId`，用于按对象查询和保证日志证据自包含。
- `beforeSnapshot` / `afterSnapshot` / `changedFields` 固定用于审计展示和证据读取，不作为业务对象反序列化来源。

### 5.4 AuditSnapshot

`AuditSnapshot` 是业务对象的审计可读快照。

核心字段：

- `schemaVersion`：快照结构版本。
- `objectType`：业务对象类型。
- `objectId`：业务对象标识。
- `displayName`：对象显示名称。
- `fields`：审计字段列表。

审计字段包含：

- `fieldName`：稳定字段名。
- `fieldLabel`：显示标签。
- `value`：原始值。
- `displayValue`：可读展示值。
- `valueType`：值类型。
- `sensitive`：是否敏感。

固定约束：

- `AuditSnapshot` 不直接序列化完整 domain。
- 每类审计对象固定提供审计快照视图。
- 快照字段只包含审计展示需要的稳定字段。
- 密码、token、secret、privateKey 和验证码固定不得进入快照。
- 手机号、邮箱等敏感展示值必须脱敏。

### 5.5 AuditAction

审计动作固定为：

- `CREATE`
- `UPDATE`
- `DELETE`
- `ENABLE`
- `DISABLE`
- `ARCHIVE`
- `RESTORE`
- `BIND`
- `UNBIND`
- `UPDATE_RELATION`
- `RESET_CREDENTIAL`

固定约束：

- 审计动作表达业务数据变更语义。
- 登录、登出、接口访问和异常固定归属 `sys_log` 或安全日志，不进入 `AuditAction`。

## 6. Global Constraints

### 6.1 Service Command

Service 写操作固定使用 Command 参数表达业务操作意图。

Command 固定包含：

- 目标对象 ID，创建操作除外。
- `expectedVersion`，用于需要并发控制的写操作。
- 当前操作需要的业务输入字段。

Command 固定不包含：

- 当前用户 ID。
- 请求 IP。
- `before` 对象。
- `after` 对象。
- 审计字段标签。
- 数据库技术审计字段。

### 6.2 Domain Version

需要并发控制和幂等写入的业务对象固定使用业务 `version`。

固定约束：

- 业务 `version` 属于业务对象并发控制。
- `audit_meta.version` 属于审计版本。
- 两类版本可以在展示中同时出现，但不得互相替代。
- 业务对象不通过 `Auditable` 承载审计能力。

### 6.3 Batch Write

Service 层不提供真正批量写入业务语义。

固定约束：

- 单对象写方法是业务数据变更的固定入口。
- 批量入口必须拆解为多个单对象写操作。
- 每个单对象写操作独立生成一条 `AuditLog`。
- 不提供 `@BatchAuditLog` 注解。

### 6.4 Audit Annotation

Service 写方法固定通过审计注解声明审计意图。

注解固定表达：

- `type`
- `id`
- `action`
- `summary`
- `condition`
- `recordWhenUnchanged`

固定约束：

- 注解只表达审计意图。
- 注解 `type` / `id` 固定映射为 `AuditObjectRef.objectType` / `objectId`。
- 注解不声明 DAO、Cache、Loader 或 Snapshot assembler 具体实现。
- 审计对象加载和快照组装通过 registry 按 `objectType` 解析。

### 6.5 Snapshot And Diff

审计快照固定由 `before` 和 `after` 生成。

固定流程：

- `CREATE`：`beforeSnapshot = null`，`afterSnapshot` 有值。
- `UPDATE`：`beforeSnapshot` 和 `afterSnapshot` 均有值。
- `DELETE`：`beforeSnapshot` 有值，`afterSnapshot = null`；状态删除时 `afterSnapshot` 可表达删除后状态。
- `ENABLE` / `DISABLE` / `UPDATE_RELATION`：按业务语义生成可读 diff。

固定约束：

- `changedFields` 固定保存可读字段差异。
- 默认无字段变化时不写审计日志。
- `recordWhenUnchanged = true` 时允许无字段变化仍记录动作。

### 6.6 Transaction

审计主链路固定同步执行，与业务写操作同事务提交。

固定约束：

- 业务写入成功，审计写入必须成功。
- 审计写入失败，业务事务必须回滚。
- 不使用纯异步审计。
- 需要外部投递或重型索引时，可以通过 outbox 承载派生能力。

## 7. Functional Requirements

### 7.1 Record Create Audit

创建业务对象成功后，系统必须记录 `CREATE` 审计日志，并创建对应 `AuditMeta`。

### 7.2 Record Update Audit

更新业务对象成功后，系统必须记录 `UPDATE` 或更具体的业务动作审计日志，并推进 `AuditMeta.version`。

### 7.3 Record Delete Audit

删除业务对象成功后，系统必须记录 `DELETE` 审计日志。

删除语义由业务对象生命周期决定。物理删除时 `afterSnapshot = null`；状态删除时 `afterSnapshot` 表达删除后业务状态。

### 7.4 Record Relation Audit

业务对象关系变更成功后，系统必须以主业务对象为审计对象记录 `UPDATE_RELATION`、`BIND` 或 `UNBIND` 审计日志。

### 7.5 Query Object Audit History

后台必须支持按 `objectType + objectId` 查询业务对象审计历史。

### 7.6 Query Audit Logs

后台必须支持按以下条件查询审计日志：

- `objectType`
- `objectId`
- `action`
- `operatorType`
- `operatorId`
- `source`
- `requestId`
- `occurredAt` 时间范围

### 7.7 Query Audit Meta

后台必须支持读取业务对象当前审计版本、最后操作、最后操作者和最后操作时间。

## 8. Key Flows

### 8.1 Create Flow

固定流程：

1. Service 执行创建业务对象。
2. 审计切面从返回值解析 `objectId`。
3. 审计对象 loader 加载 `after`。
4. 快照 assembler 生成 `afterSnapshot`。
5. 审计 Service 写入 `AuditLog`。
6. 审计 Service 创建或推进 `AuditMeta`。
7. 业务事务提交。

### 8.2 Update Flow

固定流程：

1. 审计切面从 Command 解析 `objectId`。
2. 审计对象 loader 加载 `before`。
3. Service 执行业务更新。
4. 审计对象 loader 加载 `after`。
5. 快照 assembler 生成 `beforeSnapshot` 和 `afterSnapshot`。
6. 审计 Service 生成 `changedFields`。
7. 审计 Service 写入 `AuditLog` 并推进 `AuditMeta.version`。
8. 业务事务提交。

### 8.3 Delete Flow

固定流程：

1. 审计切面从 Command 或参数解析 `objectId`。
2. 审计对象 loader 加载 `before`。
3. Service 执行业务删除。
4. 审计切面根据删除语义加载或置空 `after`。
5. 审计 Service 写入 `DELETE` 审计日志并推进 `AuditMeta.version`。
6. 业务事务提交。

## 9. Non-Functional Requirements

- 审计写入必须具备幂等能力。
- 审计查询必须支持分页。
- 审计日志必须只追加。
- 审计快照结构必须具备版本号。
- 审计模块不得依赖具体入口 API。
- 审计模块不得感知 infra Cache。
- 审计失败必须可定位到 `objectType`、`objectId`、`action` 和 `idempotencyKey`。

## 10. Open Items

无
