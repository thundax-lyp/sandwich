# INFRA SPLIT RUNBOOK

## 1. Purpose

本文档是项目级迁移操作手册，只指导按业务模块横切 `sandwish-infra`。

目标是先把持久化实现承载位置、包结构和依赖方向稳定下来，再按模块逐步迁移 DAO implementation、Mapper、Mapper XML、`DO` 与 `PersistenceAssembler`。

Controller / Service 的 `Request` / `Response` / `Entity` 隔离由 [`MODEL-SEPARATION-RUNBOOK.md`](./MODEL-SEPARATION-RUNBOOK.md) 管理。

本手册作为老工程治理与迁移的长期参考文档保留在项目中。

## 2. Scope

当前范围：

- 使用已建立的 `sandwish-infra` 持久化实现模块
- 按业务模块建立 infra 包骨架
- 从 `sandwish-biz` 中逐步抽取持久化实现
- 建立 `DO` / `DataObject`
- 建立 `PersistenceAssembler`
- 将 Mapper interface、Mapper XML、DAO implementation 迁入 `sandwish-infra`

不在范围内：

- 不改变 Sandwich 三层架构
- 不引入额外业务分层
- 不在本手册中处理 Controller `Request/Response`
- 不在本手册中处理 `InterfaceAssembler`
- 不一次性贯穿单个对象的 Controller / Service / DAO / Mapper 全链路
- 不一次性全仓迁移

## 3. Fixed Boundary

本手册只处理以下调用段：

`Service(Entity) -> DAO interface -> infra DAO impl -> PersistenceAssembler -> Mapper(DO) -> DB`

固定模型归属：

- `sandwish-biz`：`Entity`、`Service`、DAO interface
- `sandwish-infra`：`DO` / `DataObject`、DAO implementation、Mapper、Mapper XML、`PersistenceAssembler`

固定依赖方向：

- `sandwish-infra` 依赖 `sandwish-biz`
- `sandwish-admin-api` / `sandwish-front-api` 依赖 `sandwish-infra`
- `sandwish-biz` 不依赖 `sandwish-infra`
- `sandwish-infra` 不依赖 `sandwish-admin-api` / `sandwish-front-api`

## 4. Module Mapping

### 4.1 `sandwish-biz`

职责：

- 轻量业务 `Entity`
- `Service`
- DAO interface
- 业务规则和事务边界

禁止：

- 不放 `DO`
- 不放 MyBatis Mapper implementation
- 不放 Mapper XML
- 不依赖 `sandwish-infra`

### 4.2 `sandwish-infra`

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

## 5. Directory Shape

推荐目录：

```text
sandwish-biz
└── src/main/java/com/github/thundax/modules/{module}
    ├── entity
    ├── service
    └── dao

sandwish-infra
└── src/main/java/com/github/thundax/modules/{module}/persistence
    ├── dataobject
    ├── assembler
    ├── mapper
    └── dao
```

实际迁移时优先贴合现有包结构，不为了目录好看做大范围移动。

## 6. Naming Rules

- 持久化对象：`*DO`
- 持久化装配器：`*PersistenceAssembler`
- DAO 接口：沿用现有 `*Dao` / `*DAO`
- DAO 实现：`*DaoImpl`
- Mapper：`*Mapper`

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

### 7.2 Build Module Infra Skeletons

先按模块横切建立 infra 骨架，不贯穿任何单个业务对象。

模块顺序：

1. `sys`
2. `storage`
3. `assist`
4. `member`
5. `auth`

每个模块只新增空承载目录或最小占位文件：

- `dataobject`
- `assembler`
- `mapper`
- `dao`

本步骤验收：

- 包路径稳定
- 不移动现有 Mapper/XML
- 不调整 Controller / Service
- 构建不受影响

### 7.3 Split Module Infra TODOs

第三步按模块拆 infra 迁移 TODO。

每个模块 TODO 必须包含：

- 范围模块
- 当前持久化链路
- 需要新增的 `DO`
- 需要新增的 `PersistenceAssembler`
- 需要迁移的 Mapper / XML / DAO implementation
- DAO interface 是否需要去除 MyBatis 扫描标记
- 验收命令

拆分原则：

- 一个 TODO 只覆盖一个业务模块或一个清晰持久化子链路
- 不包含 Controller / Service API 模型改造
- 不把全仓 Entity 轻量化写成一个大任务
- 未完成部分必须继续留在 TODO 中

### 7.4 Migrate One Persistence Chain

对单个持久化链路执行：

1. 从现有 `Entity` 中识别数据库字段。
2. 新增 `*DO` 承载数据库字段。
3. 新增 `*PersistenceAssembler`。
4. DAO interface 保留在 `sandwish-biz`。
5. DAO implementation 移入 `sandwish-infra`。
6. Mapper interface 移入 `sandwish-infra`。
7. Mapper XML 移入 `sandwish-infra` 对应目录。
8. DAO implementation 使用 `PersistenceAssembler` 完成 `Entity <-> DO` 转换。

### 7.5 Verify Dependency Direction

迁移后必须满足：

- `sandwish-biz` 不依赖 `sandwish-infra`
- `sandwish-infra` 不依赖 `sandwish-admin-api` / `sandwish-front-api`
- DAO implementation 只在 `sandwish-infra`
- Mapper interface 和 Mapper XML 位于 `sandwish-infra`
- `Service` 不依赖 `DO`

固定构建：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

## 8. Completion Criteria

单个模块 infra 迁移完成必须满足：

- 模块持久化实现已迁入 `sandwish-infra`
- `PersistenceAssembler` 已承担 `Entity <-> DO`
- DAO interface 仍位于 `sandwish-biz`
- Service 不感知 `DO`
- 构建通过
- TODO 项已删除、拆分或收窄

## 9. Cleanup Rules

本手册完成后必须清理：

1. 删除本文件。
2. 删除或收窄 `TODO.md` 中对应任务。
3. 将稳定规则沉淀到治理文档。
4. 将代码、文档和 TODO 清理放入同一个闭环 commit。
