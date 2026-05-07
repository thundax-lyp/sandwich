# RUNBOOK Audit Service Reform

## 1. Purpose

本文档定义数据审计模块从已完成数据库基线到全部业务接入完成的执行手册。

RUNBOOK 固定说明执行顺序、依赖关系、允许的临时不可编译窗口、可提交边界和验证命令。文件级执行清单、逐项状态和人工审核队列固定进入 `TODO.md`。

已完成前置条件：

- Audit Requirements 已提交。
- Audit Database Design 已提交。
- Audit schema/data SQL 已提交。
- deploy MySQL 初始化顺序已同步。
- 全局雪花 ID 迁移已完成并收口。

## 2. Scope

最终目标：

- Audit 是独立业务支撑模块。
- Audit 拥有完整领域模型、数据库持久化、同步审计运行时和后台查询入口。
- 所有需要审计的 Service 写入口都使用明确业务动作 Command。
- 所有被审计对象都可以生成可读、可查、可 diff 的审计日志。
- 测试和架构约束可以阻止 Service 写入口退回散参数、真正批量写和泛化动作入口。

边界：

- 失败请求、登录、登出和安全事件归属 `sys_log` 或安全日志。
- 业务数据回滚、外部审计投递和 outbox 派生能力不进入本 RUNBOOK。
- infra Cache 只存在于 DAO implementation 内部，不进入 Audit 契约。

## 3. Execution Plan

本 RUNBOOK 分 8 个阶段完成。每个阶段必须进入 `TODO.md` 拆成文件级任务，经人工审核后执行。

### 3.1 按 domain 拆除旧持久化审计字段和 del_flag

目标：

- 在新 Audit 模块进入代码实现前，拆除旧的数据库技术审计字段体系。
- 将 `AuditFieldInterceptor` 从通用持久化自动填充能力中移除或改造成无旧审计字段依赖的能力。
- 按模块拆除 `create_date` / `create_by` / `update_date` / `update_by` / `del_flag` 对 Entity、DO、DAO 和 SQL 的影响。

执行内容：

- 调整或删除 `AuditFieldInterceptor`。
- 调整 `MybatisPlusConfiguration` 中与 `AuditFieldInterceptor` 相关的 bean。
- 更新 common-mybatis 测试。
- 按模块拆除数据库设计文档中的旧持久化审计字段和 `del_flag`。
- 按模块拆除 `db/schema/*.sql` 中的旧持久化审计字段和 `del_flag`。
- 按模块拆除 `db/data/*.sql` 中的旧持久化审计字段和 `del_flag` 初始化值。
- 按模块拆除 DO、PersistenceAssembler、DAO 查询条件和测试中的旧字段依赖。
- 删除 `Auditable` 或收窄为仍具备明确业务意义的模型。

固定执行顺序：

1. `common-mybatis`：调整或删除 `AuditFieldInterceptor`，同步 MyBatis-Plus 配置和测试。
2. `sys`：拆除系统管理域旧持久化审计字段和 `del_flag`。
3. `auth`：拆除认证域旧持久化审计字段和 `del_flag`。
4. `storage`：拆除存储域旧持久化审计字段和 `del_flag`。
5. `assist`：拆除辅助域旧持久化审计字段。
6. `member`：拆除会员域旧持久化审计字段和 `del_flag`。

执行策略：

- 本阶段必须按固定执行顺序推进。
- 每个 domain 必须在 `TODO.md` 中列出文件级任务。
- 每个 domain 完成后必须形成可提交边界。
- 每个存在目标审计对象的 domain，必须先完成旧字段拆除任务，再进入同一 domain 的 Command 改造和 Audit 接入任务。

临时编译窗口：

- common-mybatis 改造期间，允许依赖旧拦截器的模块在当前未提交工作区短暂不可编译。
- 单个 domain 拆除期间，允许该 domain 相关 infra 和测试短暂不可编译。
- 不允许跨 domain 共享不可编译状态。
- 每个 domain 提交前，相关模块必须可编译。

可验证点：

```bash
mvn -pl sandwish-common-mybatis -am test
mvn -pl sandwish-biz -am test
mvn -pl sandwish-infra -am test
rg "AuditFieldInterceptor|Auditable|createUserId|updateUserId|createBy|updateBy|delFlag|del_flag" sandwish-common sandwish-biz sandwish-infra db docs
```

提交边界：

- common-mybatis 拦截器改造单独提交。
- 业务模块按 domain 提交。
- 本阶段结束后，旧持久化审计字段和 `del_flag` 只允许出现在已审阅且仍具备业务意义的位置。

### 3.2 实现 Audit 核心模块

目标：

- 打通 Audit 领域对象、DAO 契约、持久化实现和 `AuditService`。

执行内容：

- 实现 `AuditObjectRef`、`AuditMeta`、`AuditLog`、snapshot、changed field 和 Audit 枚举。
- 实现 `audit_meta` / `audit_log` 的 DO、Mapper、DAO implementation 和 assembler。
- 实现 `AuditService` 的写日志、幂等、创建 meta 和推进 version。

临时编译窗口：

- 新增 biz 契约但 infra 尚未补齐时，`sandwish-biz` 必须可编译。
- 新增 infra 持久化过程中，允许 `sandwish-infra` 在当前未提交工作区短暂不可编译。
- 本阶段提交前，`sandwish-biz` 和 `sandwish-infra` 必须全部可编译。

可验证点：

```bash
mvn -pl sandwish-biz -am test
mvn -pl sandwish-infra -am test
```

提交边界：

- 可以拆成 biz 契约提交和 infra 实现提交。
- 每个提交必须保证该提交影响到的模块可编译。

### 3.3 实现审计运行时

目标：

- 建立 Service 注解到 before/after 快照、diff 和审计写入的同步事务主链路。

执行内容：

- 实现 `@AuditLog(type, id, action, summary, condition, recordWhenUnchanged)`。
- 实现表达式解析、对象加载、snapshot 组装、diff 和 registry。
- 固定审计写入与业务写操作同事务。

临时编译窗口：

- 注解、切面、support 类型必须同批补齐。
- 本阶段不允许提交不可编译中间态。

可验证点：

```bash
mvn -pl sandwish-biz -am test
```

提交边界：

- 单独提交审计运行时。

### 3.4 改造 Service 写入口

目标：

- 将所有需要审计的 Service 写入口改造成明确业务动作 Command。

执行内容：

- Service 写方法改为接收 Command。
- Command 按业务动作命名。
- Service 写方法按业务动作命名。
- 状态变更、关系维护、凭证重置等动作使用明确入口。
- 批量 HTTP 请求拆解为多个单对象 Command。
- 需要并发控制的对象引入业务 `version` 和 `expectedVersion`。

执行策略：

- 本阶段按 domain 执行。
- 每个 domain 必须完成旧字段拆除任务后，才能进入本阶段。
- 每个 domain 在 `TODO.md` 中列出对象范围和文件级任务。
- 每个 domain 完成后必须形成可提交边界。

临时编译窗口：

- 单个 domain 迁移过程中，允许当前 domain 相关 Controller / Service / 测试短暂不可编译。
- 不允许跨 domain 共享不可编译状态。
- 每个 domain 提交前，相关模块必须可编译。

可验证点：

```bash
mvn -pl sandwish-biz -am test
mvn -pl sandwish-admin-api -am test
```

提交边界：

- 按 domain 提交。
- 每个提交必须保持对应 domain 的 Controller、Service、DAO、测试同步可用。

### 3.5 接入所有目标审计对象

目标：

- 让所有目标对象生成可读、可查、可 diff 的审计日志。

执行内容：

- 为目标对象实现 loader。
- 为目标对象实现 snapshot assembler。
- 在明确业务动作 Service 方法上增加 `@AuditLog`。
- 校准字段标签、展示值和敏感字段脱敏。

目标对象：

目标对象来自当前实现 `Auditable` 的 domain 实体。

- `sys`：`User`、`Role`、`Menu`、`Department`、`Dict`。
- `auth`：`AuthSession`、`OAuthClient`、`OAuthAuthorization`、`OAuthAccessToken`、`OAuthRefreshToken`。
- `assist`：`AsyncTask`。
- `member`：`Member`。

执行策略：

- 本阶段按 domain 执行。
- 每个 domain 必须完成旧字段拆除任务和 Command 改造后，才能进入本阶段。
- 每个 domain 必须同时补齐审计测试。
- `TODO.md` 必须体现同一目标审计 domain 的执行串联：拆旧字段完成后，执行 Command 改造，再执行 Audit 接入。

临时编译窗口：

- loader、snapshot assembler 和注解接入必须在同一 domain 内闭环。
- 单个 domain 开发期间允许该 domain 测试短暂失败。
- 每个 domain 提交前，审计运行时、目标对象 Service 和测试必须通过。

可验证点：

```bash
mvn -pl sandwish-biz -am test
mvn -pl sandwish-infra -am test
```

提交边界：

- 按 domain 提交。
- 每个提交必须包含对应 domain 的审计测试。

### 3.6 新增后台审计查询 API

目标：

- 后台提供审计治理查询能力。

执行内容：

- 提供对象审计历史查询。
- 提供审计日志分页查询。
- 提供审计元数据读取。
- 响应模型只暴露后台治理需要的信息。

临时编译窗口：

- Controller、Request、Response 和 assembler 必须同批补齐。
- 本阶段不允许提交不可编译中间态。

可验证点：

```bash
mvn -pl sandwish-admin-api -am test
```

提交边界：

- 单独提交后台审计查询 API。

### 3.7 补齐架构约束和回归测试

目标：

- 用测试和架构规则固定新设计。

执行内容：

- 覆盖 create/update/delete/relation 审计流程。
- 覆盖幂等键、审计版本推进、无变化默认不记录、强制记录无变化动作。
- 增加 Service 写入口架构约束。
- 增加前台禁止审计直查入口的架构约束。
- 增加 `@BatchAuditLog` 禁止回流的架构约束。

临时编译窗口：

- 架构测试新增过程中允许测试先红。
- 本阶段提交前，所有新增和既有相关测试必须通过。

可验证点：

```bash
mvn -pl sandwish-biz -am test
mvn -pl sandwish-infra -am test
mvn -pl sandwish-admin-api -am test
mvn install
```

提交边界：

- 单独提交测试和架构约束。

### 3.8 最终收口

目标：

- 清理临时执行现场，确认 Audit 改造完成。

执行内容：

- 删除、拆分或收窄已完成 `TODO.md` 项。
- 删除本 RUNBOOK。
- 执行残留扫描。
- 执行最终全量验证。

临时编译窗口：

- 本阶段不允许不可编译。
- 本阶段不允许测试失败。

可验证点：

```bash
rg "Auditable|createUserId|updateUserId|createBy|updateBy|delFlag|del_flag" sandwish-biz sandwish-infra sandwish-admin-api db docs
rg "batchUpdate|batchDelete|batchDeleteById|batchUpdateStatus" sandwish-biz sandwish-admin-api
rg "@BatchAuditLog" sandwish-biz sandwish-infra sandwish-admin-api
rg "audit_meta|audit_log|AuditObjectRef|@AuditLog" sandwish-biz sandwish-infra sandwish-admin-api db docs
mvn install
git status --short
```

提交边界：

- 单独提交最终收口。
- 最终提交必须包含 `TODO.md` 收窄和 RUNBOOK 删除。

## 4. Compile And Verify Rules

- 每个提交边界必须可编译。
- 允许不可编译只存在于当前未提交工作区。
- 不可编译窗口必须限制在当前阶段或当前 domain 内。
- 跨阶段推进前必须执行当前阶段可验证点。
- 进入最终收口前必须保证 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api` 和 `sandwish-front-api` 均通过测试。
- 最终收口必须执行 `mvn install`。

模块验证口径：

- 只改 common 基础能力：执行 `mvn -pl sandwish-common-core -am test`。
- 只改 biz 契约和业务逻辑：执行 `mvn -pl sandwish-biz -am test`。
- 改 infra 持久化：执行 `mvn -pl sandwish-infra -am test`。
- 改后台 API：执行 `mvn -pl sandwish-admin-api -am test`。
- 跨模块收口：执行 `mvn install`。

## 5. TODO Boundary

`TODO.md` 固定承担流水账职责。

TODO 任务必须精确到：

- 阶段编号。
- domain。
- 目标文件或文件组。
- 处理动作。
- 验收点。
- 重要度。
- 当前状态。
- 与同一 domain 上下游任务的依赖关系。

RUNBOOK 固定只保留：

- 执行顺序。
- 依赖关系。
- 可编译窗口。
- 验证命令。
- 提交边界。
- 验收闸口。

## 6. Open Items

无
