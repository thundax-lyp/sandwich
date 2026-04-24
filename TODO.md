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

- [ ] `sys-user-encrypt`：迁移用户加密持久化链路到 infra
  - 范围对象：`UserEncrypt`、`BaseUserEncrypt`、`UserEncryptDao`、`UserEncryptDao.xml`
  - 处理动作：新增 `UserEncryptDO`、`UserEncryptPersistenceAssembler`、`UserEncryptDaoImpl`、`UserEncryptMapper`；迁移 MySQL、达梦、人大金仓 `UserEncryptDao.xml`；`UserEncryptDao` 保留为 biz DAO interface 并去除 MyBatis 扫描标记
  - 验收点：`UserEncryptService` 不感知 `DO`；用户加密 DAO implementation、Mapper 和 XML 位于 `sandwish-infra`

- [ ] `storage`：拆分存储模块 infra 迁移任务
  - 范围对象：`Storage` 相关 Entity、DAO、Mapper/XML、Service
  - 处理动作：先横向盘点存储模块持久化链路，按对象或清晰子链路拆出后续 infra 迁移 TODO，标明各自需要的 `DO`、`PersistenceAssembler`、DAO implementation、Mapper 和 Mapper XML
  - 验收点：存储模块形成可逐条执行的 infra 迁移任务清单；本项不直接迁移任何单个业务对象

- [ ] `assist`：拆分辅助模块 infra 迁移任务
  - 范围对象：签名、密钥、异步任务等 `assist` 持久化链路
  - 处理动作：先横向盘点辅助模块每条持久化链路，按对象或清晰子链路拆出后续 infra 迁移 TODO，标明各自需要的 `DO`、`PersistenceAssembler`、DAO implementation、Mapper 和 Mapper XML
  - 验收点：辅助模块形成可逐条执行的 infra 迁移任务清单；本项不直接迁移任何单个业务对象

- [ ] `member`：拆分会员模块 infra 迁移任务
  - 范围对象：`member` Entity、DAO、Mapper/XML、Service
  - 处理动作：先横向盘点会员模块每条持久化链路，按对象或清晰子链路拆出后续 infra 迁移 TODO，标明各自需要的 `DO`、`PersistenceAssembler`、DAO implementation、Mapper 和 Mapper XML
  - 验收点：会员模块形成可逐条执行的 infra 迁移任务清单；本项不直接迁移任何单个业务对象

- [ ] `auth`：拆分认证模块 infra 迁移任务
  - 范围对象：认证、令牌、登录表单、密码相关持久化链路
  - 处理动作：先横向盘点认证模块每条持久化链路，按对象或清晰子链路拆出后续 infra 迁移 TODO，标明各自需要的 `DO`、`PersistenceAssembler`、DAO implementation、Mapper 和 Mapper XML
  - 验收点：认证模块形成可逐条执行的 infra 迁移任务清单；本项不直接迁移任何单个业务对象

## P0 - Controller / Service 模型职责隔离

- [ ] `docs/00-governance`：固化 Controller / Service 模型边界规则
  - 范围对象：`ARCHITECTURE.md`、`NAMING-AND-PLACEMENT-RULES.md`
  - 处理动作：固定 `Controller=Request/Response`、`Service=Entity`；固定 `InterfaceAssembler` 的放置、命名和禁止事项
  - 验收点：后续 API 入口迁移不需要重新讨论 `Request/Response`、`Entity` 和 `InterfaceAssembler` 的归属

- [ ] `TODO.md`：按 API 入口拆分 Controller / Service 模型隔离任务
  - 范围对象：后台与前台 Controller/API 入口
  - 处理动作：按 `docs/30-designs/MODEL-SEPARATION-RUNBOOK.md` 的 TODO 模板，为每个入口列出当前 API 模型泄漏点、待新增或收窄的 `Request/Response`、待新增 `InterfaceAssembler`、Service 方法签名是否需要调整
  - 验收点：每个入口都有可独立验收的模型隔离 TODO；TODO 不包含 Mapper/XML/DAO implementation 迁移

- [ ] `sys-dict`：拆分 Controller / Service 模型职责隔离试点任务
  - 范围对象：`Dict` 相关 Controller/API、Service、Entity、API 查询与响应模型
  - 处理动作：先盘点 `dict` 入口的 API 模型泄漏点，拆出后续试点 TODO，标明需要新增或收窄的 `Request/Response`、`InterfaceAssembler` 和 Service 方法签名调整点；不迁移 Mapper/XML/DAO implementation
  - 验收点：`sys-dict` 形成可执行的 Controller / Service 模型隔离试点清单；本项不直接改造任何入口代码

- [ ] `docs/30-designs`：模型隔离迁移收尾
  - 范围对象：`INFRA-SPLIT-RUNBOOK.md`、`MODEL-SEPARATION-RUNBOOK.md`、`TODO.md`、治理文档
  - 处理动作：两套迁移完成后删除临时手册；删除或收窄已完成 TODO；将稳定规则沉淀到治理文档
  - 验收点：临时手册不再保留，完成历史只存在于 commit / PR 中
