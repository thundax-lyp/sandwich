# MODEL SEPARATION RUNBOOK

## 1. Purpose

本文档是一次性工程操作手册，用于指导 Sandwich 从“Entity 兼持久化职责”迁移到“分层模型隔离”。

本文档用完即删。迁移完成后：

- 已完成任务从 `TODO.md` 删除
- 本文件删除
- 稳定规则沉淀到 `docs/00-governance/ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md` 或 `DATABASE-RULES.md`

## 2. Scope

当前范围：

- 使用已建立的 `sandwish-infra` 持久化实现模块
- 从 `sandwish-biz` 中抽取持久化实现
- 建立 `Request/Response`、`Entity`、`DO/DataObject` 的模型隔离
- 建立层间 `Assembler`
- 选择一个业务域完成试点迁移

不在范围内：

- 不改变 Sandwich 三层架构
- 不引入额外业务分层
- 不一次性全仓迁移
- 不在本阶段重写全部 API 契约
- 不在本阶段删除所有历史 `BaseEntity` 依赖

## 3. Bounded Context

本次调整的目标是轻量化 `Entity`。

固定模型归属：

- `Controller` 层：`Request` / `Response`
- `Service` 层：轻量 `Entity`
- `DAO` 层：`DO` / `DataObject`

固定调用链：

`Controller -> InterfaceAssembler -> Service(Entity) -> DAO interface -> infra DAO impl -> PersistenceAssembler -> Mapper(DO) -> DB`

## 4. Module Mapping

### 4.1 `sandwish-admin-api` / `sandwish-front-api`

职责：

- `Controller`
- `Request`
- `Response`
- `InterfaceAssembler`
- Web 安全、过滤器、拦截器和 Swagger 入口

禁止：

- 不直接暴露 `Entity`
- 不直接依赖 `DO`
- 不直接访问 Mapper

### 4.2 `sandwish-biz`

职责：

- 轻量业务 `Entity`
- `Service`
- DAO interface
- 业务规则和事务边界

禁止：

- 不放 `DO`
- 不放 MyBatis Mapper
- 不放 Mapper XML
- 不依赖 `sandwish-infra`

### 4.3 `sandwish-infra`

职责：

- `DO` / `DataObject`
- MyBatis Mapper
- Mapper XML
- DAO implementation
- `PersistenceAssembler`
- 数据库类型转换和分页查询实现

禁止：

- 不承载业务流程
- 不暴露 HTTP 模型
- 不让 `Controller` 直接调用

## 5. Naming Rules

- `Request`: `*Request`
- `Response`: `*Response`
- API 层装配器：`*InterfaceAssembler`
- 业务模型：沿用现有 `*` 或 `*Entity`
- 持久化对象：`*DO`
- 持久化装配器：`*PersistenceAssembler`
- DAO 接口：沿用现有 `*Dao` / `*DAO`
- DAO 实现：`*DaoImpl`
- Mapper：`*Mapper`

## 6. Directory Shape

推荐试点目录：

```text
sandwish-admin-api 或 sandwish-front-api
└── src/main/java/com/github/thundax/modules/{module}
    ├── api
    ├── request
    ├── response
    └── assembler

sandwish-biz
└── src/main/java/com/github/thundax/modules/{module}
    ├── entity
    ├── service
    └── dao

sandwish-infra
└── src/main/java/com/github/thundax/modules/{module}/persistence
    ├── dataobject
    ├── mapper
    ├── assembler
    └── dao
```

实际迁移时优先贴合现有包结构，不为了目录好看做大范围移动。

## 7. Execution Steps

### 7.1 Confirm `sandwish-infra`

第一步确认子工程已经存在，只提供承载位置，不迁移业务代码。

本步骤全仓只执行一次。后续所有业务模块迁移都复用同一个 `sandwish-infra`，不得为每个业务模块重复新增子工程。

固定模块关系：

- root `pom.xml` 包含 `sandwish-infra`
- `sandwish-infra` 依赖 `sandwish-biz`
- `sandwish-admin-api` / `sandwish-front-api` 依赖 `sandwish-infra`
- `sandwish-biz` 不依赖 `sandwish-infra`

本步骤验收：

- Maven reactor 能识别 `sandwish-infra`
- `sandwish-admin-api` 和 `sandwish-front-api` 能通过 `sandwish-infra` 装配持久化实现
- `sandwish-biz` 不反向依赖 `sandwish-infra`
- 不迁移任何业务模块
- 后续模块 TODO 不再包含“新增子工程”动作

固定验证：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

### 7.2 Update Architecture Documents

第二步先落架构治理口径，再进入模块迁移。

必须更新：

- `docs/00-governance/ARCHITECTURE.md`
- `docs/00-governance/NAMING-AND-PLACEMENT-RULES.md`
- `docs/00-governance/DATABASE-RULES.md`

更新重点：

- 固定 `sandwish-infra` 是持久化实现模块，不是额外业务分层
- 固定 `biz` 保留轻量 `Entity`、`Service`、DAO interface
- 固定 `infra` 承载 `DO`、Mapper、Mapper XML、DAO implementation、`PersistenceAssembler`
- 固定 `Controller` 使用 `Request/Response`
- 固定 `Service` 不感知 `Request/Response/DO`
- 固定 `DAO` 层不处理 HTTP 模型
- 固定层间转换由 `InterfaceAssembler` 与 `PersistenceAssembler` 承担

本步骤验收：

- 文档能说明为什么新增 `sandwish-infra`
- 文档能说明这不是架构换血
- 文档能指导后续模块 TODO 拆解

### 7.3 Split Module TODOs

第三步按模块拆迁移 TODO。

每个模块 TODO 必须包含：

- 范围对象
- 当前持久化链路
- 需要新增的 `DO`
- 需要新增的 `PersistenceAssembler`
- 需要迁移的 Mapper / XML / DAO implementation
- 是否需要新增 `Request/Response`
- 是否需要新增 `InterfaceAssembler`
- 验收命令

模块拆分顺序固定为：

1. `sys-dict` 试点
2. `storage`
3. `assist`
4. `member`
5. `auth`
6. `sys` 剩余对象

拆分原则：

- 一个 TODO 只覆盖一个业务模块或一个清晰子链路
- 不把全仓 Entity 轻量化写成一个大任务
- 试点模块排在第一批
- 未完成部分必须继续留在 TODO 中

### 7.4 Adjust This Runbook During Execution

执行中允许调整本临时手册。

触发条件：

- 子工程依赖方向需要修正
- 试点暴露出新的固定步骤
- `Assembler` 放置位置需要调整
- 命名规则需要收敛
- 验收命令需要变化

调整要求：

- 只记录迁移期间需要的操作步骤
- 不把稳定规则长期堆在本文件中
- 稳定规则最终沉淀到治理文档

### 7.5 Choose Pilot Module

先选择一个小而完整的业务域做试点。

固定试点：

- `sys-dict`

选择标准：

- 有完整 Controller / Service / DAO / Entity / Mapper 链路
- 表结构和查询复杂度可控
- 改动能在一个或少量 commit 内解释清楚

### 7.6 Split Persistence Model

对试点模块执行：

1. 从现有 `Entity` 中识别持久化字段。
2. 新增 `*DO` 承载数据库字段。
3. 保留或收窄 `Entity` 为业务模型。
4. 数据库审计字段、逻辑删除字段、MyBatis 细节进入 `DO`。
5. 不在 `Entity` 中新增 MyBatis 或数据库专用注解。

### 7.7 Add `PersistenceAssembler`

新增 `*PersistenceAssembler`，固定只做：

- `Entity -> DO`
- `DO -> Entity`
- 必要的列表转换

禁止：

- 不访问数据库
- 不调用 Service
- 不处理 HTTP Request / Response
- 不写业务流程

### 7.8 Move Mapper And DAO Implementation

对试点模块执行：

1. DAO interface 保留在 `sandwish-biz`。
2. DAO implementation 移入 `sandwish-infra`。
3. Mapper interface 移入 `sandwish-infra`。
4. Mapper XML 移入 `sandwish-infra` 对应资源目录。
5. DAO implementation 使用 `PersistenceAssembler` 完成 `Entity <-> DO` 转换。

### 7.9 Add `InterfaceAssembler`

如果试点接口仍直接暴露或接收 `Entity`，新增 `*InterfaceAssembler`：

- `Request -> Entity`
- `Entity -> Response`

禁止：

- 不访问 DAO / Mapper
- 不处理事务
- 不转换 `DO`

### 7.10 Verify Dependency Direction

迁移后必须满足：

- `Controller` 不依赖 `DO`
- `Service` 不依赖 `Request` / `Response` / `DO`
- `sandwish-biz` 不依赖 `sandwish-infra`
- `sandwish-infra` 不依赖 `sandwish-admin-api` / `sandwish-front-api`
- DAO implementation 只在 `sandwish-infra`

### 7.11 Build And Review

固定执行：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

如试点只影响单个入口模块，可执行对应模块 package。

## 8. Completion Criteria

试点完成必须满足：

- 一个业务域完成 `Request/Response`、`Entity`、`DO` 隔离
- `PersistenceAssembler` 已承担 `Entity <-> DO`
- `InterfaceAssembler` 已承担 `Request/Response <-> Entity`
- `Service` 不感知 `DO`
- `Controller` 不感知 `DO`
- 构建通过
- TODO 项已删除、拆分或收窄

## 9. Cleanup Rules

本手册完成后必须清理：

1. 删除本文件。
2. 删除或收窄 `TODO.md` 中对应任务。
3. 将稳定规则沉淀到治理文档。
4. 将代码、文档和 TODO 清理放入同一个闭环 commit。

## 10. Open Items

- 试点模块尚未确认。
- `DO` 后缀是否固定为唯一持久化对象后缀尚未确认。
- `InterfaceAssembler` 是否放在入口模块还是业务模块尚未最终确认。
