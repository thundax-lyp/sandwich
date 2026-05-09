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
- 所有被审计对象都可以生成可读、可查、可 diff 的审计日志。
- 业务表不再保存 `create_date` / `create_by` / `update_date` / `update_by` 等通用审计字段。
- 业务 Entity、DO、DAO、Mapper、Query、缓存 DTO 和 API 查询入口不再依赖 `createUserId` / `updateUserId` / `createBy` / `updateBy`。
- API 响应模型不再暴露业务表通用审计字段。
- 审计字段集中由 Audit 模块管理；业务对象变更事实固定写入 `audit_log`，当前审计状态固定写入 `audit_meta`。
- 测试和架构约束可以阻止审计能力回流到业务表审计字段体系。

边界：

- 失败请求、登录、登出和安全事件归属 `sys_log` 或安全日志。
- 业务数据回滚、外部审计投递和 outbox 派生能力不进入本 RUNBOOK。
- Service 方法规约化是 Audit 接入前置条件，不进入本 RUNBOOK 执行范围。
- infra Cache 只存在于 DAO implementation 内部，不进入 Audit 契约。
- 如果未来某个业务确实需要按创建人、更新人、创建时间或更新时间筛选，该字段必须作为该业务对象的业务字段重新建模，不得复用通用审计字段。
- 当前项目先统一移除基于通用审计字段的查询条件，不新增替代业务字段。
- Audit 自身表字段不属于拆除范围。
- `sys_log` 不是业务 Audit，不进入本 RUNBOOK 的字段拆除范围。
- Auth token、session 和 login event 运行态对象与本 RUNBOOK 无关。

通用审计字段等价名固定包括：

- 业务模型字段：`createdAt`、`createAt`、`createDate`、`createUserId`、`createdBy`、`createBy`、`updatedAt`、`updateAt`、`updateDate`、`updateUserId`、`updatedBy`、`updateBy`。
- 持久化字段：`createDate`、`createBy`、`updateDate`、`updateBy`。
- 数据库列：`created_at`、`create_at`、`create_date`、`created_by`、`create_by`、`create_user_id`、`updated_at`、`update_at`、`update_date`、`updated_by`、`update_by`、`update_user_id`。

允许使用的业务时间或业务主体字段必须带具体业务语义，例如 `publishedAt`、`submittedAt`、`lastLoginAt`、`lockedUntil`、`expiredAt`。

## 3. Execution Plan

本 RUNBOOK 分 8 个阶段完成。每个阶段必须进入 `TODO.md` 拆成文件级任务，经人工审核后执行。

### 3.1 移除基于审计字段的查询

目标：

- 在拆除业务表审计字段前，先移除所有基于通用审计字段的查询入口。
- Service Query、DAO 条件、Mapper SQL、缓存 DTO 和 API 查询请求不再使用 `createUserId` / `updateUserId` / `createBy` / `updateBy` 作为过滤条件。
- 后台列表查询不再支持按通用审计字段过滤。
- 列表默认排序、显式排序和 Mapper order by 不再依赖 `create_date` / `update_date` 等通用审计字段。

执行内容：

- 删除 `*Query` 中仅用于通用审计字段过滤的字段。
- 删除 API `*QueryRequest` 中仅用于通用审计字段过滤的字段。
- 删除 InterfaceAssembler 中通用审计字段查询条件装配。
- 删除 DAO interface、DAO implementation、Mapper XML 和测试中通用审计字段过滤逻辑。
- 删除 Wrapper、LambdaQuery、Mapper XML 和手写 SQL 中通用审计字段过滤与排序逻辑。
- 本阶段只移除查询条件；展示属性和存量字段在 3.2 按 domain 拆除。

固定执行顺序：

1. `sys`：移除系统管理查询中的通用审计字段过滤。
2. `auth`：移除认证配置和运行态查询中的通用审计字段过滤。
3. `storage`：移除存储查询中的通用审计字段过滤。
4. `assist`：移除辅助任务查询中的通用审计字段过滤。
5. `member`：移除会员查询中的通用审计字段过滤。

执行策略：

- 本阶段按 domain 提交。
- 每个 domain 的 Query、DAO、Mapper 和测试必须在同一提交内闭环。
- 删除查询能力后，不新增按创建人、更新人、创建时间或更新时间过滤的替代字段。
- 如果列表排序依赖通用审计字段，改用 `priority`、`id` 或已有业务时间字段；不得新增替代审计排序字段。

临时编译窗口：

- 单个 domain 开发期间允许该 domain 相关测试短暂失败。
- 每个 domain 提交前，相关模块必须可编译。

可验证点：

```bash
mvn -pl sandwish-biz -am compile
mvn -pl sandwish-infra -am compile
rg "createUserId|updateUserId|createBy|updateBy" sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api -g "*Query.java" -g "*QueryRequest.java" -g "*Dao.java" -g "*DaoImpl.java" -g "*Mapper.xml" -g "*InterfaceAssembler.java"
```

提交边界：

- 按 domain 提交。
- 每个提交必须移除该 domain 的通用审计字段查询能力。

### 3.2 按 domain 拆除业务表审计字段

目标：

- 从业务表、业务 Entity、DO、PersistenceAssembler、缓存 DTO 和测试中移除通用审计字段。
- 删除 `Auditable` 对业务 Entity 的约束。
- 删除 `AuditFieldInterceptor`，使 common-mybatis 不再为业务表填充通用审计字段。
- 普通业务 API 响应不再透出通用审计字段。

执行内容：

- 删除 `Auditable` 接口及业务 Entity 的 `implements Auditable`。
- 删除业务 Entity 的 `createUserId` / `updateUserId` 字段。
- 删除 DO/DataObject 的 `createBy` / `updateBy` 字段。
- 删除 PersistenceAssembler 中 `createBy` / `updateBy` 与 `createUserId` / `updateUserId` 的转换。
- 删除缓存 DTO 中通用审计字段。
- 删除 API Response、InterfaceAssembler 和相关测试中的通用审计字段展示逻辑。
- 删除 `db/schema/*.sql` 业务表中的 `create_date` / `create_by` / `update_date` / `update_by` 列。
- 删除 `db/data/*.sql` 中业务表通用审计字段初始化值。
- 删除业务数据库设计文档中的通用审计字段描述。
- 删除需求文档中通用审计字段作为业务对象属性的描述。
- 删除 `DATABASE-RULES.md` 中业务表固定声明通用审计字段的规则。
- 删除 `AuditFieldInterceptor` 和 `MybatisPlusConfiguration` 中对应 bean。
- 更新 common-mybatis、biz、infra 和架构测试。

固定执行顺序：

1. `common-mybatis`：删除 `AuditFieldInterceptor`，同步 MyBatis-Plus 配置和测试。
2. 治理和业务文档：同步 `DATABASE-RULES.md`、相关 `*-REQUIREMENTS.md`、相关 `*-DATABASE-DESIGN.md`。
3. SQL 基线：同步 `db/schema/*.sql` 和 `db/data/*.sql`。
4. `sys`：拆除系统管理域业务表审计字段。
5. `auth`：拆除认证域业务表审计字段。
6. `storage`：拆除存储域业务表审计字段。
7. `assist`：拆除辅助域业务表审计字段。
8. `member`：拆除会员域业务表审计字段。

执行策略：

- 本阶段必须按固定执行顺序推进。
- 每个 domain 必须在 `TODO.md` 中列出文件级任务。
- 每个 domain 完成后必须形成可提交边界。
- 每个存在目标审计对象的 domain，必须先完成业务表审计字段拆除，再进入同一 domain 的 Audit 接入任务。
- 当前项目只维护 schema/data 基线，不新增 migration 脚本。

临时编译窗口：

- common-mybatis 改造期间，允许依赖旧拦截器的模块在当前未提交工作区短暂不可编译。
- 单个 domain 拆除期间，允许该 domain 相关 infra 和测试短暂不可编译。
- 不允许跨 domain 共享不可编译状态。
- 每个 domain 提交前，相关模块必须可编译。

可验证点：

```bash
mvn -pl sandwish-common/sandwish-common-mybatis -am compile
mvn -pl sandwish-biz -am compile
mvn -pl sandwish-infra -am compile
rg "Auditable|AuditFieldInterceptor|createUserId|updateUserId|createBy|updateBy|create_date|update_date|create_by|update_by" sandwish-common sandwish-biz sandwish-infra db docs
```

提交边界：

- common-mybatis 拦截器改造单独提交。
- 治理文档、需求文档、数据库设计和 SQL 基线按同一语义提交。
- 业务模块按 domain 提交。
- 本阶段结束后，业务表通用审计字段只允许出现在明确列入 Open Items 的位置。

### 3.3 实现 Audit 核心模块

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
mvn -pl sandwish-biz -am compile
mvn -pl sandwish-infra -am compile
```

提交边界：

- 可以拆成 biz 契约提交和 infra 实现提交。
- 每个提交必须保证该提交影响到的模块可编译。

### 3.4 实现审计运行时

目标：

- 建立 Service 注解到 before/after 快照、diff 和审计写入的同步事务主链路。

前置条件：

- Service 方法规约化 RUNBOOK 已完成，或当前目标 domain 的 Service 写入口已经完成规约化。
- 目标写方法的 `*Command` 必须能稳定表达目标对象 ID、业务动作输入和必要的并发控制信息。

执行内容：

- 实现 `@AuditLog(type, id, action, summary, condition, recordWhenUnchanged)`。
- 实现表达式解析、对象加载、snapshot 组装、diff 和 registry。
- 固定审计写入与业务写操作同事务。

临时编译窗口：

- 注解、切面、support 类型必须同批补齐。
- 本阶段不允许提交不可编译中间态。

可验证点：

```bash
mvn -pl sandwish-biz -am compile
```

提交边界：

- 单独提交审计运行时。

### 3.5 接入所有目标审计对象

目标：

- 让所有目标对象生成可读、可查、可 diff 的审计日志。

执行内容：

- 为目标对象实现 loader。
- 为目标对象实现 snapshot assembler。
- 在明确业务动作 Service 方法上增加 `@AuditLog`。
- 校准字段标签、展示值和敏感字段脱敏。

目标对象：

目标对象由 Audit 接入清单显式声明。`Auditable` 不是审计对象来源。

- `sys`：`User`、`Role`、`Menu`、`Department`、`Dict`。
- `assist`：`AsyncTask`。
- `member`：`Member`。

排除目标对象：

- `auth`：`OAuthClient` 只进入业务表审计字段拆除，不进入本 RUNBOOK 的 Audit 接入。
- `auth`：`OAuthAuthorization`、`PrincipalAccessToken`、`PrincipalRefreshToken` 和 `PrincipalLoginEvent` 不进入业务 Audit；登录、登出、token 生命周期和安全事件归属 `sys_log` 或安全日志。

执行策略：

- 本阶段按 domain 执行。
- 每个 domain 必须完成业务表审计字段拆除任务后，才能进入本阶段。
- 每个 domain 必须完成 Service 方法规约化任务后，才能进入本阶段。
- 每个 domain 必须同时补齐审计测试。
- `TODO.md` 必须体现同一目标审计 domain 的执行串联：移除审计字段查询、拆除业务表审计字段、Service 方法规约化完成后，再执行 Audit 接入。

临时编译窗口：

- loader、snapshot assembler 和注解接入必须在同一 domain 内闭环。
- 单个 domain 开发期间允许该 domain 测试短暂失败。
- 每个 domain 提交前，审计运行时、目标对象 Service 和测试源码必须可编译。

可验证点：

```bash
mvn -pl sandwish-biz -am compile
mvn -pl sandwish-infra -am compile
rg "Auditable|createUserId|updateUserId|createBy|updateBy|create_date|update_date|create_by|update_by" sandwish-biz/src/main/java/com/github/thundax/modules/{target-domain} sandwish-infra/src/main/java/com/github/thundax/modules/{target-domain} db docs
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
mvn -pl sandwish-admin-api -am compile
```

提交边界：

- 单独提交后台审计查询 API。

### 3.7 补齐架构约束和回归测试

目标：

- 用测试和架构规则固定新设计。

执行内容：

- 覆盖 create/update/delete/relation 审计流程。
- 覆盖幂等键、审计版本推进、无变化默认不记录、强制记录无变化动作。
- 增加前台禁止审计直查入口的架构约束。
- 增加 `@BatchAuditLog` 禁止回流的架构约束。
- 增加业务表、业务 Entity、DO、Query、API Request、API Response 和缓存 DTO 不得声明通用审计字段的架构约束。
- 增加 `AuditFieldInterceptor` 禁止回流的架构约束。

临时编译窗口：

- 架构测试新增过程中允许测试先红。
- 本阶段提交前，生产代码和测试源码必须可编译。

可验证点：

```bash
mvn -pl sandwish-biz -am compile
mvn -pl sandwish-infra -am compile
mvn -pl sandwish-admin-api -am compile
```

提交边界：

- 单独提交测试和架构约束。

### 3.8 最终收口

目标：

- 清理临时执行现场，确认 Audit 改造完成。

执行内容：

- 执行最终完整验证。
- 删除、拆分或收窄已完成 `TODO.md` 项。
- 删除本 RUNBOOK。
- 执行残留扫描。
- 最终残留扫描只允许命中 Audit 自身表、Audit 领域模型、Audit 查询模型、`sys_log`、Open Items 明确列出的业务字段和本 RUNBOOK 收口前自身内容。

临时编译窗口：

- 本阶段不允许不可编译。
- 本阶段不允许 `mvn clean install` 失败。

可验证点：

```bash
mvn clean install
rg "Auditable|AuditFieldInterceptor|createdAt|createAt|createDate|createUserId|createdBy|createBy|updatedAt|updateAt|updateDate|updateUserId|updatedBy|updateBy|created_at|create_at|create_date|created_by|create_by|create_user_id|updated_at|update_at|update_date|updated_by|update_by|update_user_id" sandwish-common sandwish-biz sandwish-infra sandwish-admin-api db docs
rg "@BatchAuditLog" sandwish-biz sandwish-infra sandwish-admin-api
rg "audit_meta|audit_log|AuditObjectRef|@AuditLog" sandwish-biz sandwish-infra sandwish-admin-api db docs
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
- 执行任务过程中只运行 `compile` 目标，不单独运行 `test` 目标。
- 完整 `mvn clean install` 只在最终清理现场前执行。

模块验证口径：

- 只改 common 基础能力：执行 `mvn -pl sandwish-common-core -am compile`。
- 改 common-mybatis 持久化基础能力：执行 `mvn -pl sandwish-common/sandwish-common-mybatis -am compile`。
- 只改 biz 契约和业务逻辑：执行 `mvn -pl sandwish-biz -am compile`。
- 改 infra 持久化：执行 `mvn -pl sandwish-infra -am compile`。
- 改后台 API：执行 `mvn -pl sandwish-admin-api -am compile`。
- 跨模块收口：执行 `mvn clean install`。

## 5. TODO Boundary

`TODO.md` 固定承担文件级执行队列职责。

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
