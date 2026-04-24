# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前主线顺序

1. 文档治理骨架
   - 补齐架构、文档、命名、数据库和部署治理文档
2. 三层架构规则收敛
   - 固定 `Controller -> Service -> DAO/Mapper` 边界
3. 业务需求与数据库文档补齐
   - 按现有模块和业务域逐步建立 `10-requirements/` 与 `20-database/`
4. 质量与交付闭环
   - 按任务补测试、文档同步和小步提交

## P0 - 文档治理

- [ ] `docs/00-governance`：补齐治理文档基础集
  - 范围对象：`DOCUMENT-RULES.md`、`NAMING-AND-PLACEMENT-RULES.md`、`DATABASE-RULES.md`、`DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`
  - 处理动作：按 Sandwich 三层 API 架构补齐治理文档，保留文档结构、任务路由与执行闭环
  - 验收点：AI 能按 `docs/AGENT.md` 找到对应治理入口，不需要默认全量加载 `docs`

## P0 - 模型职责隔离

- [ ] `docs/00-governance`：固化模型职责隔离架构规则
  - 范围对象：`ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md`、`DATABASE-RULES.md`
  - 处理动作：固定 `sandwish-infra` 是持久化实现模块；固定 `Controller=Request/Response`、`Service=Entity`、`DAO=DO/DataObject`；固定 `InterfaceAssembler` 与 `PersistenceAssembler` 的放置、命名和禁止事项
  - 验收点：后续模块迁移不需要重新讨论 `sandwish-infra` 的定位、依赖方向和模型归属

- [ ] `TODO.md`：按业务模块拆分模型隔离迁移任务
  - 范围对象：`sys`、`storage`、`assist`、`member`、`auth`
  - 处理动作：按 `docs/30-designs/MODEL-SEPARATION-RUNBOOK.md` 的 TODO 模板，为每个模块列出当前持久化链路、待新增 `DO`、待新增 `PersistenceAssembler`、待迁移 Mapper/XML/DAO implementation、是否需要 `Request/Response` 和 `InterfaceAssembler`
  - 验收点：每个模块都有可独立验收的迁移 TODO；后续迁移任务不再包含“新增 `sandwish-infra` 子工程”动作

- [ ] `sys-dict`：完成模型职责隔离试点
  - 范围对象：`Dict` 相关 Controller/API、Service、DAO、Entity、Mapper/XML
  - 处理动作：以 `dict` 为试点建立 `Request/Response`、轻量 `Entity`、`DO/DataObject`、`InterfaceAssembler`、`PersistenceAssembler` 的完整链路；Mapper/XML/DAO implementation 迁入 `sandwish-infra`
  - 验收点：`Controller` 不依赖 `Entity/DO`，`Service` 不依赖 `Request/Response/DO`，`sandwish-biz` 不依赖 `sandwish-infra`，构建通过

- [ ] `storage`：迁移存储模块持久化实现
  - 范围对象：`Storage` 相关 Entity、DAO、Mapper/XML、Service、Controller/API、存储 VO/Converter
  - 处理动作：复用试点模式建立 `DO` 与 `PersistenceAssembler`，将持久化实现迁入 `sandwish-infra`，按需补 `Request/Response` 与 `InterfaceAssembler`
  - 验收点：存储模块 Service 只处理轻量 Entity，DAO implementation 和 Mapper/XML 位于 `sandwish-infra`，上传/查询/删除相关构建链路通过

- [ ] `assist`：迁移辅助模块持久化实现
  - 范围对象：签名、密钥、异步任务等 `assist` 持久化链路
  - 处理动作：建立 `DO`、`PersistenceAssembler`、DAO implementation 与 Mapper/XML 的 `sandwish-infra` 实现，按 API 入口需要补 `Request/Response` 与 `InterfaceAssembler`
  - 验收点：`assist` 模块完成模型隔离，Service 不感知 `DO`

- [ ] `member`：迁移会员模块持久化实现
  - 范围对象：`member` Entity、DAO、Mapper/XML、Service、前台 Controller
  - 处理动作：建立会员模块 `DO` 和 `PersistenceAssembler`，迁移 DAO implementation 与 Mapper/XML，按前台 API 入口补模型转换
  - 验收点：会员模块完成模型隔离，前台 Controller 不直接暴露持久化模型

- [ ] `auth`：迁移认证模块持久化实现
  - 范围对象：认证、令牌、登录表单、密码相关持久化链路
  - 处理动作：识别纯业务模型与持久化模型边界，按需建立 `DO`、`PersistenceAssembler` 和 `InterfaceAssembler`
  - 验收点：认证模块完成模型隔离，敏感字段不在 API 响应或日志中泄露

- [ ] `docs/30-designs`：模型隔离迁移收尾
  - 范围对象：`MODEL-SEPARATION-RUNBOOK.md`、`TODO.md`、治理文档
  - 处理动作：迁移完成后删除临时手册；删除或收窄已完成 TODO；将稳定规则沉淀到治理文档
  - 验收点：临时手册不再保留，完成历史只存在于 commit / PR 中
