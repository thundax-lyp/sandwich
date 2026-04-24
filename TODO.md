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

- [ ] `auth`：横向盘点认证模块 infra 迁移链路并拆分子 TODO
  - 范围对象：认证、令牌、登录表单、密码相关 Redis 持久化链路
  - 处理动作：先横向盘点认证模块每条持久化链路，按对象或清晰子链路拆出后续 infra 迁移 TODO，优先拆分 `AccessToken` 在线令牌链路、`LoginForm` 登录表单链路和密码支撑链路
  - 验收点：认证模块形成可逐条执行的 infra 迁移任务清单；本项不直接迁移任何单个业务对象
  - [ ] `auth-access-token`：拆分 `AccessToken` 在线令牌链路 infra 迁移任务
    - 范围对象：`AccessToken` 在线令牌链路
    - 处理动作：盘点当前 `AccessTokenDao`、`AccessTokenDaoImpl` 和 Redis key 读写路径，拆出后续需要迁入 infra 的 Redis persistence implementation、DAO implementation、Mapper 和 Mapper XML，明确在线令牌保存、激活、删除、按 token / userId 查询和在线数统计的迁移边界
    - 验收点：`AccessToken` 链路形成可独立执行的 infra 迁移 TODO；本项不直接迁移代码
  - [ ] `auth-login-form`：拆分 `LoginForm` 登录表单链路 infra 迁移任务
    - 范围对象：`LoginForm` 登录表单链路
    - 处理动作：盘点当前 `LoginFormDao`、`LoginFormDaoImpl` 和 Redis key 读写路径，拆出后续需要迁入 infra 的 Redis persistence implementation、DAO implementation、Mapper 和 Mapper XML，明确登录表单创建、刷新、验证码、短信验证码、私钥与删除的迁移边界
    - 验收点：`LoginForm` 链路形成可独立执行的 infra 迁移 TODO；本项不直接迁移代码
  - [ ] `auth-password-support`：拆分密码支撑链路 infra 迁移任务
    - 范围对象：登录失败计数、账号锁定和密码校验相关 Redis 持久化链路
    - 处理动作：盘点 `AuthApiController` 中的失败次数与锁定 Redis 读写链路，拆出后续需要迁入 infra 的 Redis persistence implementation、DAO implementation、Mapper 和 Mapper XML，明确密码输错计数、锁定状态和解锁清理的迁移边界
    - 验收点：密码支撑链路形成可独立执行的 infra 迁移 TODO；本项不直接迁移代码

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

## P0 - 持久化表达改造

- [ ] `sys`：拆分系统域持久化表达改造任务
  - 范围对象：用户、角色、菜单、机构、日志、字典、上传文件相关持久化链路
  - 处理动作：先横向盘点 `sys` 域每条持久化链路，按对象或清晰子链路拆出后续持久化表达改造 TODO，标明各自涉及的 `Service`、Dao interface、DaoImpl、Mapper、Mapper XML、`DO` 和 `PersistenceAssembler`，以及是否依赖通用查询契约、`DO.query`、`DO extends ...`
  - 验收点：系统域形成可逐条执行的持久化表达改造任务清单；本项不直接改造任何单条持久化链路代码

- [ ] `storage`：拆分存储域持久化表达改造任务
  - 范围对象：存储文件、存储业务绑定相关持久化链路
  - 处理动作：先横向盘点 `storage` 域每条持久化链路，按对象或清晰子链路拆出后续持久化表达改造 TODO，标明各自涉及的 `Service`、Dao interface、DaoImpl、Mapper、Mapper XML、`DO` 和 `PersistenceAssembler`，以及是否依赖通用查询契约、`DO.query`、`DO extends ...`
  - 验收点：存储域形成可逐条执行的持久化表达改造任务清单；本项不直接改造任何单条持久化链路代码

- [ ] `assist`：拆分辅助域持久化表达改造任务
  - 范围对象：签名、异步任务相关持久化链路
  - 处理动作：先横向盘点 `assist` 域每条持久化链路，按对象或清晰子链路拆出后续持久化表达改造 TODO，标明各自涉及的 `Service`、Dao interface、DaoImpl、Mapper、Mapper XML、`DO` 和 `PersistenceAssembler`，以及是否依赖通用查询契约、`DO.query`、`DO extends ...`
  - 验收点：辅助域形成可逐条执行的持久化表达改造任务清单；本项不直接改造任何单条持久化链路代码

- [ ] `member`：拆分会员域持久化表达改造任务
  - 范围对象：会员查询、会员资料相关持久化链路
  - 处理动作：先横向盘点 `member` 域每条持久化链路，按对象或清晰子链路拆出后续持久化表达改造 TODO，标明各自涉及的 `Service`、Dao interface、DaoImpl、Mapper、Mapper XML、`DO` 和 `PersistenceAssembler`，以及是否依赖通用查询契约、`DO.query`、`DO extends ...`
  - 验收点：会员域形成可逐条执行的持久化表达改造任务清单；本项不直接改造任何单条持久化链路代码

- [ ] `common-persistence`：拆分公共持久化契约改造任务
  - 范围对象：`CrudDao`、`TreeDao` 及其对通用查询入口、父类字段和树结构父类的依赖
  - 处理动作：先横向盘点公共持久化契约中的通用查询入口、基类字段假设和树结构假设，按清晰子链路拆出后续改造 TODO，标明各自涉及的公共接口、受影响业务域和替换顺序
  - 验收点：公共持久化契约形成可逐条执行的改造任务清单；本项不直接修改任何公共接口或基类代码

- [ ] `common-assembler`：拆分公共 PersistenceAssembler 收敛任务
  - 范围对象：各业务域 `PersistenceAssembler` 中的 `Entity.query <-> DO.query` 转换和 `DO` 继承依赖
  - 处理动作：先横向盘点各业务域 `PersistenceAssembler` 当前承担的 query 转换和父类字段装配职责，按对象或清晰子链路拆出后续收敛 TODO，标明各自涉及的 `Entity`、`DO` 和装配规则调整点
  - 验收点：公共 `PersistenceAssembler` 收敛形成可逐条执行的任务清单；本项不直接修改任何装配代码

- [ ] `docs/00-governance`：拆分持久化表达改造治理同步任务
  - 范围对象：`ARCHITECTURE.md`、`DATABASE-RULES.md`、`NAMING-AND-PLACEMENT-RULES.md`、`TODO.md`
  - 处理动作：先盘点治理文档中与 DAO 查询契约、`DO` 定义、`PersistenceAssembler` 职责相关的现有规则，拆出后续治理同步 TODO，标明需要调整的规则点和对应迁移前置条件
  - 验收点：治理同步形成可逐条执行的文档任务清单；本项不直接修改治理文档内容
