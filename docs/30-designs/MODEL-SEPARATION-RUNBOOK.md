# MODEL SEPARATION RUNBOOK

## 1. Purpose

本文档是一次性工程操作手册，用于指导 Sandwich 从“Entity 兼持久化职责”迁移到“分层模型隔离”。

本文档用完即删。迁移完成后：

- 已完成任务从 `TODO.md` 删除
- 本文件删除
- 稳定规则沉淀到 `docs/00-governance/ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md` 或 `DATABASE-RULES.md`

## 2. Scope

当前范围：

- 新增 `sandwish-infra` 持久化实现模块
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

### 7.1 Choose Pilot Module

先选择一个小而完整的业务域做试点。

优先候选：

- `storage`
- `dict`

选择标准：

- 有完整 Controller / Service / DAO / Entity / Mapper 链路
- 表结构和查询复杂度可控
- 改动能在一个 commit 或少量 commit 内解释清楚

### 7.2 Add `sandwish-infra`

新增 Maven 模块：

- root `pom.xml` 增加 `sandwish-infra`
- `sandwish-infra` 依赖 `sandwish-biz`
- `sandwish-admin-api` / `sandwish-front-api` 依赖 `sandwish-infra`
- `sandwish-biz` 不依赖 `sandwish-infra`

### 7.3 Split Persistence Model

对试点模块执行：

1. 从现有 `Entity` 中识别持久化字段。
2. 新增 `*DO` 承载数据库字段。
3. 保留或收窄 `Entity` 为业务模型。
4. 数据库审计字段、逻辑删除字段、MyBatis 细节进入 `DO`。
5. 不在 `Entity` 中新增 MyBatis 或数据库专用注解。

### 7.4 Add `PersistenceAssembler`

新增 `*PersistenceAssembler`，固定只做：

- `Entity -> DO`
- `DO -> Entity`
- 必要的列表转换

禁止：

- 不访问数据库
- 不调用 Service
- 不处理 HTTP Request / Response
- 不写业务流程

### 7.5 Move Mapper And DAO Implementation

对试点模块执行：

1. DAO interface 保留在 `sandwish-biz`。
2. DAO implementation 移入 `sandwish-infra`。
3. Mapper interface 移入 `sandwish-infra`。
4. Mapper XML 移入 `sandwish-infra` 对应资源目录。
5. DAO implementation 使用 `PersistenceAssembler` 完成 `Entity <-> DO` 转换。

### 7.6 Add `InterfaceAssembler`

如果试点接口仍直接暴露或接收 `Entity`，新增 `*InterfaceAssembler`：

- `Request -> Entity`
- `Entity -> Response`

禁止：

- 不访问 DAO / Mapper
- 不处理事务
- 不转换 `DO`

### 7.7 Verify Dependency Direction

迁移后必须满足：

- `Controller` 不依赖 `DO`
- `Service` 不依赖 `Request` / `Response` / `DO`
- `sandwish-biz` 不依赖 `sandwish-infra`
- `sandwish-infra` 不依赖 `sandwish-admin-api` / `sandwish-front-api`
- DAO implementation 只在 `sandwish-infra`

### 7.8 Build And Review

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
