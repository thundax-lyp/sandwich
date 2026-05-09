# RUNBOOK Service Method Reform

## 1. Purpose

本文档定义 Service 方法规约化的独立执行手册。

Service 方法规约化固定作为独立复杂改造执行，不作为 Audit 剥离 RUNBOOK 的内嵌阶段。Audit 剥离可以依赖规约化后的 Service 写入口，但 Service 方法规约化本身不承载 Audit 领域模型、Audit 持久化和 Audit 后台查询实现。

RUNBOOK 固定说明执行顺序、依赖关系、允许的临时不可编译窗口、可提交边界和验证命令。文件级执行清单、逐项状态和人工审核队列固定进入 `TODO.md`。

## 2. Scope

最终目标：

- Service 写入口按明确业务动作命名。
- Service 写入口使用 Command 参数表达一次业务写操作。
- Command 固定承载写操作入参、目标对象标识、业务动作上下文和并发控制参数。
- Service 写入口固定表达单对象业务变更。
- Service 查询入口固定使用 Query 和 PageDTO 表达读取条件与分页窗口。
- Controller 负责请求对象到 Command 的入口适配。
- Service 测试围绕业务动作和 Command 编写。
- 架构约束可以阻止 Service 写入口回退为散参数、泛化动作入口和真正批量写入口。

边界：

- 本 RUNBOOK 不实现 Audit 领域模型、Audit DAO、Audit Service、Audit 注解和 Audit 后台查询。
- 本 RUNBOOK 不删除旧持久化审计字段。
- 本 RUNBOOK 不改变数据库表结构，除非某个业务对象明确需要新增业务并发 `version`。
- 本 RUNBOOK 不把 Command 作为 Controller 请求模型复用。
- 本 RUNBOOK 不要求读方法使用 Command。
- 本 RUNBOOK 不要求 DAO interface 使用 Query、PageDTO 或 Command。

## 3. Target Shape

### 3.1 Method Naming

Service 方法名固定满足：

- 方法名使用业务动作表达。
- 方法名不重复 Service 主体名。
- 查询方法使用 `get`、`list`、`page`、`count` 或 `exists` 表达读取形态。
- 写方法使用 `create`、`change`、`rename`、`enable`、`disable`、`move`、`bind`、`unbind`、`reset`、`revoke`、`grant`、`assign`、`remove` 等业务动作表达。
- 方法名不得使用持久化动词、泛化流程动词和批量技术词。

固定禁止名单：

- `insert`
- `save`
- `update`
- `modify`
- `handle`
- `operate`
- `process`
- `execute`
- `do`
- `submit`
- `manage`
- `maintain`
- `batch*`
- `deleteById`
- `select*`

命名示例：

```java
Role get(RoleQuery query);

List<Role> list(RoleQuery query);

PageDTO<Role> page(RoleQuery query, PageDTO<Role> page);

EntityId create(CreateRoleCommand command);

void rename(RenameRoleCommand command);

void grantPrivilege(GrantRolePrivilegeCommand command);

void bindMenus(BindRoleMenusCommand command);
```

### 3.2 Method Parameters

Service 方法参数固定只有三种形态：

```java
get(Query)

page(Query, PageDTO)

write(Command)
```

固定规则：

- Service 方法参数最多 2 个。
- 非分页查询方法固定接收一个 `*Query`。
- 分页查询方法固定接收一个 `*Query` 和一个 `PageDTO`。
- 写方法固定接收一个 `*Command`。
- Service 方法不接收 Domain Entity 作为写入口参数。
- Service 方法不接收散落业务字段作为公开入口参数。
- Service 方法不接收 Controller Request、Response 或 infra DO/DataObject。
- Service 方法不使用 `*PageQuery`。

参数示例：

```java
Role get(RoleQuery query);

List<Role> list(RoleQuery query);

PageDTO<Role> page(RoleQuery query, PageDTO<Role> page);

EntityId create(CreateRoleCommand command);

void rename(RenameRoleCommand command);
```

### 3.3 Query

Query 固定满足：

- 类名使用 `{业务对象名}Query`。
- Query 只承载读取过滤条件。
- Query 不承载分页窗口。
- Query 不承载 HTTP、Session、权限适配、持久化实现类型或 request 字符串解析逻辑。

### 3.4 Command

Command 固定满足：

- 类名使用业务动作命名。
- 创建操作 Command 不强制包含目标对象 ID。
- 更新、删除和关系变更 Command 固定包含目标对象 ID。
- 需要并发控制的对象固定包含 `expectedVersion`。
- Command 按业务动作聚合，不按数据库字段机械拆分。
- Command 不包含 HTTP、Servlet、Session、Cookie、Spring Security 或 Controller 专用对象。
- Command 不包含 DAO、Mapper、Cache 或持久化对象。
- Command 不复用 Controller Request 类型。

Command 示例：

```java
CreateDictCommand
ChangeDictInfoCommand
ChangeDictPriorityCommand
DeleteDictCommand
RenameRoleCommand
BindRoleMenusCommand
```

### 3.5 Domain Entity Boundary

Service 写入口固定不接收 Domain Entity。

固定规则：

- `update(DomainEntity entity)` 是不合规 Service 写入口。
- `save(DomainEntity entity)` 是不合规 Service 写入口。
- `saveOrUpdate(DomainEntity entity)` 是不合规 Service 写入口。
- 业务确实存在“编辑基础信息”动作时，使用 `changeXxxInfo(*Command)` 表达。
- Command 只列出本业务动作允许修改的字段。

### 3.6 Batch Boundary

Service 层固定不提供真正批量写业务语义。

固定规则：

- 批量 HTTP 请求由 Controller 或入口编排拆解为多个单对象 Command。
- 每个单对象 Command 独立调用一个 Service 写入口。
- 批量导入、批量同步等确有业务意义的场景必须建模为独立业务对象或独立任务，不复用普通单对象写入口。
- 条件清理动作允许使用 `deleteByXxx(*Query)`，但不得使用 `batch*` 命名，不得使用 `ids` 作为普通业务对象批量删除入口。

## 4. Execution Plan

本 RUNBOOK 分 8 个阶段完成。每个阶段必须进入 `TODO.md` 拆成文件级任务，经人工审核后执行。

### 4.1 同步 Service 方法规约文档

目标：

- 将 Service 方法命名和参数三态规则沉淀到长期规约文档。

执行内容：

- 更新 `docs/00-governance/ARCHITECTURE.md` 的 Service 边界规则。
- 更新 `docs/00-governance/NAMING-AND-PLACEMENT-RULES.md` 的 Service 命名、Query、Command 和 PageDTO 放置规则。
- 更新 `docs/AGENT.md` 的文档路由，使 Service 方法规约化任务能定位到本 RUNBOOK 和治理文档。
- 必要时更新相关需求文档中仍保留旧 Service 入参口径的位置。

可验证点：

```bash
rg "LAYER_SERVICE_BOUNDARY_TYPES|NAME_SERVICE_QUERY|Service 方法|Command|PageDTO" docs/00-governance docs/AGENT.md docs/10-requirements
```

提交边界：

- 单独提交规约文档。

### 4.2 建立架构测试基线

目标：

- 用测试固定 Service 方法名和参数组合的目标规则。

执行内容：

- 新增或扩展 `sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceNamingArchitectureTest.java`。
- 新增 Service 参数组合架构测试。
- 新增 Command / Query / PageDTO 边界架构测试。
- 新增 `batch*` 方法名禁止测试。
- 在测试中维护临时放行清单；放行清单不写入规约正文。

可验证点：

```bash
mvn -pl sandwish-biz -am test
```

提交边界：

- 单独提交架构测试基线。
- 如果现有代码尚未满足规则，本阶段允许测试以待审阅任务形式先落计划，不提交失败测试。

### 4.3 盘点 Service 写入口

目标：

- 明确当前所有 Service 写入口、散参数入口、泛化动作入口和批量写入口。

执行内容：

- 按 domain 扫描 `sandwish-biz` Service interface 和 implementation。
- 按 domain 扫描 `sandwish-admin-api` 和 `sandwish-front-api` Controller 调用点。
- 识别 create、update、delete、status change、relation change、credential reset、token revoke 等写动作。
- 识别 `update(DomainEntity entity)`、`save(DomainEntity entity)`、`batch*`、`deleteById` 和散参数入口。
- 将待改造对象和文件级任务写入 `TODO.md` 待审阅任务项。

可验证点：

```bash
rg "interface .*Service|class .*ServiceImpl|batch|deleteById|update|insert|save|bind|unbind|reset|revoke" sandwish-biz sandwish-admin-api sandwish-front-api
```

提交边界：

- 本阶段只形成 `TODO.md` 任务拆解时单独提交。

### 4.4 按 domain 建立 Query 和 Command

目标：

- 为单个 domain 建立 Service 入参模型。

执行内容：

- 新增或调整 `*Query`。
- 新增或调整 `*Command`。
- Command 按业务动作聚合，不按字段机械拆分。
- 查询分页继续使用 `*Query` + `PageDTO`，不新增 `*PageQuery`。
- 保持 DAO 契约不因 Query / Command 规约化被迫改造。

可验证点：

```bash
mvn -pl sandwish-biz -am test
```

提交边界：

- 按 domain 提交。

### 4.5 按 domain 改造 Service 契约

目标：

- 将单个 domain 的 Service 写入口改造为业务动作方法和 Command 参数。

执行内容：

- 调整 Service interface。
- 调整 Service implementation。
- 将查询入口改为 `*Query` 或 `*Query, PageDTO`。
- 将写入口改为 `*Command`。
- 将 `update(DomainEntity entity)` 拆成明确业务动作。
- 将方法名改为业务动作，不重复 Service 主体名。
- 更新 Service 单元测试。

执行策略：

- 本阶段按 domain 执行。
- 每个 domain 完成后必须形成可提交边界。
- 不允许跨 domain 共享不可编译状态。

临时编译窗口：

- 单个 domain 改造期间，允许该 domain 相关 Controller、Service 和测试短暂不可编译。
- 每个 domain 提交前，相关模块必须可编译。

可验证点：

```bash
mvn -pl sandwish-biz -am test
```

提交边界：

- 按 domain 提交。

### 4.6 同步入口适配

目标：

- 让 Controller 适配新的 Service Command 写入口。

执行内容：

- Controller 将 Request 组装为 Command。
- Controller 将 Request 组装为 Query。
- Controller 不把 Request 直接传入 Service。
- Controller 不承载业务写流程。
- 更新 API 入口测试。

执行策略：

- 与 4.3 同 domain 串联执行。
- 可以与同一 domain 的 Service 契约改造放入同一个提交。

可验证点：

```bash
mvn -pl sandwish-admin-api -am test
mvn -pl sandwish-front-api -am test
```

提交边界：

- 按 domain 提交。

### 4.7 处理批量和泛化入口

目标：

- 拆除真正批量写入口和泛化动作入口。

执行内容：

- 将批量写入口拆成入口层循环调用单对象 Service 写入口。
- 将泛化状态、关系和动作入口拆成明确业务动作方法。
- 对确有业务意义的批量任务建模为独立业务流程。
- 保留条件清理动作时，使用 `deleteByXxx(*Query)` 表达明确业务条件。

可验证点：

```bash
rg "batch[A-Z]|batchUpdate|batchDelete|batchDeleteById|batchUpdateStatus" sandwish-biz sandwish-admin-api sandwish-front-api
mvn -pl sandwish-biz -am test
mvn -pl sandwish-admin-api -am test
```

提交边界：

- 按 domain 或按批量能力提交。

### 4.8 补齐架构约束和最终收口

目标：

- 用测试和架构规则固定 Service 方法规约化结果，并清理临时执行现场。

执行内容：

- 增加 Service 方法参数三态约束。
- 增加 Service 方法名禁止名单约束。
- 增加 Command 命名和目录约束。
- 增加 Query 命名和目录约束。
- 增加 Controller Request 不进入 Service 的约束。
- 增加 Domain Entity 不作为 Service 写入口参数的约束。
- 增加批量写入口回流扫描。
- 清理架构测试中的临时放行清单。
- 清理 RUNBOOK、TODO、临时扫描记录和过渡说明。
- 执行残留引用扫描。
- 删除、拆分或收窄已完成 `TODO.md` 项。
- 删除本 RUNBOOK。

可验证点：

```bash
rg "RUNBOOK-SERVICE-METHOD-REFORM|Service Method Reform" docs TODO.md
rg "batch[A-Z]|batchUpdate|batchDelete|batchDeleteById|batchUpdateStatus" sandwish-biz sandwish-admin-api sandwish-front-api
rg "update\\(|save\\(|saveOrUpdate\\(|deleteById\\(" sandwish-biz/src/main/java/com/github/thundax/modules
mvn -pl sandwish-biz -am test
mvn -pl sandwish-admin-api -am test
mvn -pl sandwish-front-api -am test
mvn install
git status --short
```

提交边界：

- 单独提交架构约束和最终收口。
- 最终提交必须包含 `TODO.md` 收窄和 RUNBOOK 删除。
- 最终提交后不得残留本 RUNBOOK、已完成 TODO、临时放行清单或过渡执行说明。

## 5. Relation To Audit

Service 方法规约化与 Audit 剥离固定作为两个独立任务。

固定关系：

- Audit 剥离负责移除旧审计字段体系并实现独立 Audit 模块。
- Service 方法规约化负责稳定 Service 写入口形态。
- Audit 注解运行时可以依赖规约化后的 Command 解析目标对象 ID。
- Service 方法规约化完成前，Audit RUNBOOK 不进入业务对象注解接入阶段。
- Audit 核心模型、DAO 和查询能力不依赖 Service 方法规约化完成。

## 6. Compile And Verify Rules

- 每个提交边界必须可编译。
- 允许不可编译只存在于当前未提交工作区。
- 不可编译窗口必须限制在当前阶段或当前 domain 内。
- 跨阶段推进前必须执行当前阶段可验证点。
- 进入最终收口前必须保证 `sandwish-biz`、`sandwish-admin-api` 和 `sandwish-front-api` 均通过测试。
- 最终收口必须执行 `mvn install`。

## 7. Open Items

- Command 放置目录需要人工确认并写入 `docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`。
- 需要业务 `version` 的对象清单需要人工确认。
