# MODEL SEPARATION RUNBOOK

## 1. Purpose

本文档是一次性工程操作手册，只指导 Controller / Service 之间的模型职责隔离。

本文档不负责 `sandwish-infra` 横切、Mapper/XML 搬迁和 DAO implementation 落位；这些动作由 [`INFRA-SPLIT-RUNBOOK.md`](./INFRA-SPLIT-RUNBOOK.md) 管理。

本文档用完即删。迁移完成后：

- 已完成任务从 `TODO.md` 删除
- 本文件删除
- 稳定规则沉淀到 `docs/00-governance/ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md` 或 `DATABASE-RULES.md`

## 2. Scope

当前范围：

- 建立 `Controller` 层 `Request` / `Response`
- 建立 `Service` 层轻量 `Entity`
- 建立 `InterfaceAssembler`
- 收敛 Controller 与 Service 的模型边界
- 按业务入口逐步迁移，不贯穿持久化实现

不在范围内：

- 不新增或调整 `sandwish-infra` 子工程
- 不迁移 Mapper interface
- 不迁移 Mapper XML
- 不迁移 DAO implementation
- 不引入额外业务分层
- 不一次性全仓迁移
- 不在本阶段删除所有历史 `BaseEntity` 依赖

## 3. Fixed Boundary

本手册只处理以下调用段：

`HTTP/API -> Controller(Request/Response) -> InterfaceAssembler -> Service(Entity)`

固定模型归属：

- `Controller` 层：`Request` / `Response`
- `Service` 层：轻量 `Entity`
- `InterfaceAssembler`：只负责 `Request/Response <-> Entity`

禁止：

- `Controller` 直接暴露 `Entity`
- `Controller` 直接依赖 `DO` / Mapper
- `Service` 依赖 `Request` / `Response`
- `InterfaceAssembler` 调用 DAO / Mapper / Service

## 4. Directory Shape

推荐目录：

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
    └── service
```

实际迁移时优先贴合现有包结构，不为了目录好看做大范围移动。

## 5. Naming Rules

- 请求模型：`*Request`
- 响应模型：`*Response`
- API 层装配器：`*InterfaceAssembler`
- 业务模型：沿用现有 `*` 或 `*Entity`

已存在的 `*QueryParam` / `*Vo` 可以作为过渡模型，但新迁移对象优先使用 `Request` / `Response` 后缀。

## 6. Execution Steps

### 6.1 Choose One API Boundary

一次只选择一个 Controller 或一组强相关接口。

选择标准：

- 入口清晰
- Service 调用清晰
- 不要求同步搬迁 Mapper/XML
- 能在一个小 commit 中解释清楚

### 6.2 Identify Current Model Leakage

记录当前入口是否存在：

- Controller 入参直接使用 `Entity`
- Controller 出参直接使用 `Entity`
- Service 方法直接接收 `Request` / `QueryParam`
- Service 方法直接返回 `Vo` / `Response`
- Controller 内手写大量字段复制

### 6.3 Add Request And Response

新增或收窄入口模型：

- `Request` 只表达 API 入参
- `Response` 只表达 API 出参
- 不把数据库审计字段、逻辑删除字段、Mapper 条件字段默认暴露给 API

### 6.4 Add InterfaceAssembler

新增 `*InterfaceAssembler`，固定只做：

- `Request -> Entity`
- `Entity -> Response`
- 必要的列表转换

禁止：

- 不访问数据库
- 不调用 Service
- 不处理事务
- 不转换 `DO`

### 6.5 Lighten Service Entity Carefully

轻量化 `Entity` 时遵守：

- 只移除当前入口已经不需要暴露给 Controller 的职责
- 不把持久化迁移混入本手册任务
- 如发现必须先完成 `DO` / Mapper 拆分，转入 [`INFRA-SPLIT-RUNBOOK.md`](./INFRA-SPLIT-RUNBOOK.md) 对应任务

### 6.6 Verify Boundary

固定检查：

- `Controller` 不依赖 `DO`
- `Controller` 不直接暴露 `Entity`
- `Service` 不依赖 `Request` / `Response`
- `InterfaceAssembler` 不调用 DAO / Mapper / Service

固定构建：

```bash
mvn -pl sandwish-admin-api,sandwish-front-api -am -DskipTests package
```

如只影响单个入口模块，可执行对应模块 package。

## 7. TODO Template

每个 Controller / Service 模型隔离 TODO 必须包含：

- 范围入口
- 当前 API 模型泄漏点
- 需要新增或收窄的 `Request`
- 需要新增或收窄的 `Response`
- 需要新增的 `InterfaceAssembler`
- Service 方法签名是否需要调整
- 验收命令

## 8. Completion Criteria

单个入口完成必须满足：

- Controller 只接收 API 请求模型
- Controller 只输出 API 响应模型
- Service 只处理业务 `Entity`
- `InterfaceAssembler` 已承担 `Request/Response <-> Entity`
- 构建通过
- TODO 项已删除、拆分或收窄

## 9. Cleanup Rules

本手册完成后必须清理：

1. 删除本文件。
2. 删除或收窄 `TODO.md` 中对应任务。
3. 将稳定规则沉淀到治理文档。
4. 将代码、文档和 TODO 清理放入同一个闭环 commit。
