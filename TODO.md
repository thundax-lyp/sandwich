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

## P0 - Infra 横切拆分

- [ ] `docs/00-governance`：固化 infra 横切架构规则
  - 范围对象：`ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md`、`DATABASE-RULES.md`
  - 处理动作：固定 `sandwish-infra` 是持久化实现模块；固定 `biz` 保留 `Entity`、`Service`、DAO interface；固定 `infra` 承载 `DO/DataObject`、DAO implementation、Mapper、Mapper XML、`PersistenceAssembler`
  - 验收点：后续模块迁移不需要重新讨论 `sandwish-infra` 的定位、依赖方向和持久化模型归属

- [ ] `sandwish-infra`：按业务模块建立 infra 横切骨架
  - 范围对象：`sys`、`storage`、`assist`、`member`、`auth`
  - 处理动作：按 `docs/30-designs/INFRA-SPLIT-RUNBOOK.md` 建立每个模块的 `dataobject`、`assembler`、`mapper`、`dao` 承载目录或最小占位文件
  - 验收点：只形成 infra 横切承载骨架，不迁移任何 Controller / Service / Mapper / XML 代码，构建通过

- [ ] `TODO.md`：按业务模块拆分 infra 迁移任务
  - 范围对象：`sys`、`storage`、`assist`、`member`、`auth`
  - 处理动作：按 `docs/30-designs/INFRA-SPLIT-RUNBOOK.md` 的 TODO 模板，为每个模块列出当前持久化链路、待新增 `DO`、待新增 `PersistenceAssembler`、待迁移 Mapper/XML/DAO implementation、DAO interface 是否需要去除 MyBatis 扫描标记
  - 验收点：每个模块都有可独立验收的 infra 迁移 TODO；TODO 不包含 Controller / Service API 模型改造

- [ ] `sys`：迁移系统模块持久化实现
  - 范围对象：`Dict`、`Log`、`Menu`、`Office`、`Role`、`UploadFile`、`User`、`UserEncrypt` 等系统模块持久化链路
  - 处理动作：先从一个持久化子链路试点，再逐步建立 `DO`、`PersistenceAssembler`、DAO implementation、Mapper 和 Mapper XML 的 `sandwish-infra` 实现
  - 验收点：系统模块已迁移对象的 Service 不感知 `DO`，DAO implementation 和 Mapper/XML 位于 `sandwish-infra`

- [ ] `storage`：迁移存储模块 infra 实现
  - 范围对象：`Storage` 相关 Entity、DAO、Mapper/XML、Service
  - 处理动作：建立 `DO` 与 `PersistenceAssembler`，将持久化实现迁入 `sandwish-infra`
  - 验收点：存储模块 Service 只处理轻量 Entity，DAO implementation 和 Mapper/XML 位于 `sandwish-infra`，上传/查询/删除相关构建链路通过

- [ ] `assist`：迁移辅助模块 infra 实现
  - 范围对象：签名、密钥、异步任务等 `assist` 持久化链路
  - 处理动作：建立 `DO`、`PersistenceAssembler`、DAO implementation 与 Mapper/XML 的 `sandwish-infra` 实现
  - 验收点：`assist` 模块完成持久化模型隔离，Service 不感知 `DO`

- [ ] `member`：迁移会员模块 infra 实现
  - 范围对象：`member` Entity、DAO、Mapper/XML、Service
  - 处理动作：建立会员模块 `DO` 和 `PersistenceAssembler`，迁移 DAO implementation 与 Mapper/XML
  - 验收点：会员模块完成持久化模型隔离，Service 不感知 `DO`

- [ ] `auth`：迁移认证模块 infra 实现
  - 范围对象：认证、令牌、登录表单、密码相关持久化链路
  - 处理动作：识别业务模型与持久化模型边界，按需建立 `DO`、`PersistenceAssembler`、DAO implementation、Mapper 和 Mapper XML
  - 验收点：认证模块完成持久化模型隔离，敏感字段不在持久化转换过程中误暴露

## P0 - Controller / Service 模型职责隔离

- [ ] `docs/00-governance`：固化 Controller / Service 模型边界规则
  - 范围对象：`ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md`
  - 处理动作：固定 `Controller=Request/Response`、`Service=Entity`；固定 `InterfaceAssembler` 的放置、命名和禁止事项
  - 验收点：后续 API 入口迁移不需要重新讨论 `Request/Response`、`Entity` 和 `InterfaceAssembler` 的归属

- [ ] `TODO.md`：按 API 入口拆分 Controller / Service 模型隔离任务
  - 范围对象：后台与前台 Controller/API 入口
  - 处理动作：按 `docs/30-designs/MODEL-SEPARATION-RUNBOOK.md` 的 TODO 模板，为每个入口列出当前 API 模型泄漏点、待新增或收窄的 `Request/Response`、待新增 `InterfaceAssembler`、Service 方法签名是否需要调整
  - 验收点：每个入口都有可独立验收的模型隔离 TODO；TODO 不包含 Mapper/XML/DAO implementation 迁移

- [ ] `sys-dict`：完成 Controller / Service 模型职责隔离试点
  - 范围对象：`Dict` 相关 Controller/API、Service、Entity、API 查询与响应模型
  - 处理动作：以 `dict` 为试点建立或收窄 `Request/Response`、轻量 `Entity`、`InterfaceAssembler`，不迁移 Mapper/XML/DAO implementation
  - 验收点：`Controller` 不依赖 `DO`，Controller 不直接暴露业务 Entity，Service 不依赖 `Request/Response`，构建通过

- [ ] `docs/30-designs`：模型隔离迁移收尾
  - 范围对象：`INFRA-SPLIT-RUNBOOK.md`、`MODEL-SEPARATION-RUNBOOK.md`、`TODO.md`、治理文档
  - 处理动作：两套迁移完成后删除临时手册；删除或收窄已完成 TODO；将稳定规则沉淀到治理文档
  - 验收点：临时手册不再保留，完成历史只存在于 commit / PR 中
