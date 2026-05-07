# RUNBOOK Global Snowflake ID Migration

## 1. Purpose

本文档定义全项目数据库主键从 UUID 字符串迁移到雪花 `Long` 的执行手册。

RUNBOOK 固定说明执行顺序、可编译窗口、验证命令和提交边界。文件级执行清单、逐项状态和人工审核队列固定进入 `TODO.md`。

## 2. Scope

最终目标：

- 全项目独立数据库表主键统一使用雪花 `Long`。
- Java 代码中的数据库主键模型、`EntityId`、DO、DAO、Service、Controller Request/Response 和测试与雪花 ID 口径一致。
- 初始化数据使用固定雪花 ID。
- Audit 模块基于已完成的全局雪花 ID 能力继续实现。

边界：

- 本 RUNBOOK 只处理 ID 迁移。
- `del_flag` 删除不进入本 RUNBOOK。
- `create_date` / `create_by` / `update_date` / `update_by` 旧持久化审计字段拆除不进入本 RUNBOOK。
- Service Command 化不进入本 RUNBOOK。
- Audit 领域、运行时和查询 API 实现不进入本 RUNBOOK。

`del_flag` 可能承载业务生命周期、查询过滤和删除语义。删除 `del_flag` 必须按 domain 单独审核，不得作为 ID 迁移的顺手改动。

## 3. Execution Plan

本 RUNBOOK 分 8 个阶段完成。每个阶段必须进入 `TODO.md` 拆成文件级任务，经人工审核后执行。

### 3.1 改文档：固定全局 ID 规则

目标：

- 固定数据库主键、领域标识、业务编号和审计对象标识的边界。

执行内容：

- 更新统一 ID 治理文档。
- 更新数据库规则文档。
- 明确 `AuditObjectRef.objectId` 继续使用 `String`。
- 明确业务编号不复用数据库主键。

临时编译窗口：

- 本阶段只改文档，不触发 Java 编译。
- 本阶段不得修改 Java 代码。

可验证点：

```bash
rg "ASSIGN_UUID|EntityId 固定使用 `String`|DO/DataObject.id.*String" docs/00-governance
git diff --check
```

提交边界：

- 单独提交全局 ID 治理规则文档。

### 3.2 改文档：更新各 domain 数据库设计

目标：

- 让所有业务域数据库设计使用雪花 `Long` 主键口径。

执行内容：

- 更新 `SYSTEM-DATABASE-DESIGN.md`。
- 更新 `AUTH-DATABASE-DESIGN.md`。
- 更新 `STORAGE-DATABASE-DESIGN.md`。
- 更新 `MEMBER-DATABASE-DESIGN.md`。
- 更新 `AUDIT-DATABASE-DESIGN.md`。

临时编译窗口：

- 本阶段只改文档，不触发 Java 编译。
- 本阶段不得修改 Java 代码到不可编译状态。

可验证点：

```bash
rg "varchar\\(64\\).*NOT NULL|ASSIGN_UUID|DO/DataObject.id.*String" docs/20-database
git diff --check
```

提交边界：

- 单独提交各 domain 数据库设计文档。

### 3.3 改 SQL：更新数据库脚本和初始化数据

目标：

- 让所有数据库基线 SQL 使用雪花 `Long` 主键和固定雪花初始化 ID。

执行内容：

- 更新全部 `db/schema/*.sql` 主键和关系字段类型。
- 更新全部 `db/data/*.sql` 固定初始化 ID。
- 更新 `db/AGENT.md`。
- 更新 deploy 初始化说明。

临时编译窗口：

- 本阶段以 SQL 和部署说明为主，可以不触发 Java 编译。
- 本阶段不得修改 Java 代码到不可编译状态。

可验证点：

```bash
rg "varchar\\(64\\).*NOT NULL" db/schema
rg "'[a-z]+-[a-z0-9-]+'" db/data
git diff --check
```

提交边界：

- 单独提交数据库脚本和初始化数据。

### 3.4 改代码：迁移 common ID 基础能力

目标：

- 让 common 层提供稳定雪花 ID 生成和 `Long` 领域标识转换能力。

执行内容：

- 调整 `EntityId`、`EntityIdCodec`、`BaseLongId` 和相关 ID 生成器。
- 清理 UUID 主键生成默认能力。
- 更新 common-core ID 测试。
- 更新 MyBatis `EntityIdTypeHandler` 和测试。

临时编译窗口：

- 本阶段允许 common 相关测试在当前未提交工作区短暂失败。
- 本阶段不允许提交不可编译中间态。

可验证点：

```bash
mvn -pl sandwish-common-core -am test
mvn -pl sandwish-common-mybatis -am test
```

提交边界：

- 可以拆成 common-core 和 common-mybatis 两个提交。
- 每个提交必须保证对应 common 模块测试通过。

### 3.5 改代码：迁移 biz 领域和 DAO 契约

目标：

- 让业务 Entity、DAO interface、Service 参数和 query 中的 ID 类型与雪花 ID 口径一致。

执行内容：

- 按 domain 迁移 `sys`、`auth`、`storage`、`assist`、`member` 的 Entity ID 字段。
- 按 domain 迁移 DAO interface 的 ID 参数。
- 按 domain 迁移 Service 参数和返回值中的 ID 类型。
- 调整相关 query 对象中的 ID 字段。

临时编译窗口：

- 单个 domain 迁移过程中，允许该 domain 在当前未提交工作区短暂不可编译。
- 不允许跨 domain 共享不可编译状态。
- 每个 domain 提交前，`sandwish-biz` 必须可编译。

可验证点：

```bash
mvn -pl sandwish-biz -am test
```

提交边界：

- 按 domain 提交。
- 每个提交必须保证对应 domain 的 biz 契约可编译。

### 3.6 改代码：迁移 infra 持久化实现

目标：

- 让 DO、Mapper、DAO implementation、PersistenceAssembler 和 cache DTO 与雪花 ID 口径一致。

执行内容：

- 按 domain 迁移 DO 主键和关系字段类型。
- 按 domain 迁移 PersistenceAssembler ID 转换。
- 按 domain 迁移 DAO implementation 查询条件、缓存 key 和返回值。
- 清理 MyBatis-Plus `ASSIGN_UUID` 依赖。

临时编译窗口：

- 单个 domain 迁移过程中，允许该 domain 的 infra 和测试短暂不可编译。
- 不允许提交不可编译中间态。
- 每个 domain 提交前，`sandwish-infra` 必须可编译。

可验证点：

```bash
mvn -pl sandwish-infra -am test
```

提交边界：

- 按 domain 提交。
- 每个提交必须保证对应 domain 的 infra 持久化实现可编译。

### 3.7 改代码：迁移 API 边界和测试夹具

目标：

- 让后台 API、前台 API、测试夹具和断言与雪花 ID 口径一致。

执行内容：

- 调整 Controller Request / Response 中的 ID 字段和转换。
- 调整 InterfaceAssembler 中的 ID 转换。
- 调整测试夹具中的固定 ID。
- 调整断言中的 ID 类型。

临时编译窗口：

- 单个 API 入口迁移过程中，允许对应 API 模块测试短暂失败。
- 不允许提交不可编译中间态。

可验证点：

```bash
mvn -pl sandwish-admin-api -am test
mvn -pl sandwish-front-api -am test
```

提交边界：

- 可以按 API 入口或 domain 提交。
- 每个提交必须保证对应 API 模块可编译。

### 3.8 全局 ID 迁移收口

目标：

- 确认 UUID 主键口径已退出，雪花 ID 迁移完成。

执行内容：

- 执行残留扫描。
- 删除、拆分或收窄已完成 `TODO.md` 项。
- 删除本 RUNBOOK。
- 执行最终全量验证。

临时编译窗口：

- 本阶段不允许不可编译。
- 本阶段不允许测试失败。

可验证点：

```bash
rg "ASSIGN_UUID|UuidIdGenerator|UuidHelper" sandwish-common sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api docs db
rg "varchar\\(64\\).*NOT NULL" db/schema docs/20-database
mvn install
git status --short
```

提交边界：

- 单独提交全局 ID 迁移收口。
- 最终提交必须包含 `TODO.md` 收窄和 RUNBOOK 删除。

## 4. Compile And Verify Rules

- 每个提交边界必须可编译。
- 允许不可编译只存在于当前未提交工作区。
- 不可编译窗口必须限制在当前阶段或当前 domain 内。
- 跨阶段推进前必须执行当前阶段可验证点。
- 最终收口必须执行 `mvn install`。

模块验证口径：

- 改 common-core：执行 `mvn -pl sandwish-common-core -am test`。
- 改 common-mybatis：执行 `mvn -pl sandwish-common-mybatis -am test`。
- 改 biz：执行 `mvn -pl sandwish-biz -am test`。
- 改 infra：执行 `mvn -pl sandwish-infra -am test`。
- 改后台 API：执行 `mvn -pl sandwish-admin-api -am test`。
- 改前台 API：执行 `mvn -pl sandwish-front-api -am test`。
- 跨模块收口：执行 `mvn install`。

## 5. TODO Boundary

`TODO.md` 固定承担流水账职责。

TODO 任务必须精确到：

- 阶段编号。
- domain 或模块。
- 目标文件或文件组。
- 处理动作。
- 验收点。
- 重要度。
- 当前状态。

RUNBOOK 固定只保留：

- 执行顺序。
- 依赖关系。
- 可编译窗口。
- 验证命令。
- 提交边界。
- 验收闸口。

## 6. Open Items

无
