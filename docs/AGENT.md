# Docs Agent

只给 AI / harness 读。目标：少读，快读，读对。

## Core Rules

- 只加载完成当前任务必需的文档。
- 先读治理文档，再读业务文档。
- `docs/` 不是默认全量输入目录。
- 工程级规则优先于业务模块文档。
- Sandwich 固定采用三层架构，不迁移 Bacon 的 DDD 分层模型。
- 不默认引入 `api / interfaces / application / domain / infra` 等 DDD 目录语义。

## Mandatory Entry

实现、修改、评审代码前固定先读：

1. [`00-governance/ARCHITECTURE.md`](./00-governance/ARCHITECTURE.md)

## Task Router

- 纯实现、修 bug、重构业务逻辑：
  读 `ARCHITECTURE.md`，再读对应 `10-requirements/*-REQUIREMENTS.md`
- 新增类、改类名、改目录、判断模块归属：
  再读 `00-governance/NAMING-AND-PLACEMENT-RULES.md`
- 数据库、实体、DAO、Mapper、SQL、持久化查询：
  再读 `00-governance/DATABASE-RULES.md`
  再读对应 `20-database/*-DATABASE-DESIGN.md`
- JSP、静态资源、后台页面、前台页面：
  先读 `ARCHITECTURE.md`
  再读对应业务需求文档和专项设计文档
- 改文档：
  再读 `00-governance/DOCUMENT-RULES.md`
- 上线准备、运维、发布、WAR 打包：
  先读 `00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`
  再读 `40-readiness/`
- 专项方案、路线图、跨模块设计：
  按需读 `30-designs/`

## Module Router

- `sandwish-common`:
  通用工具、基础 Web、加密、i18n、通用 service 支撑。
- `sandwish-biz`:
  业务实体、DAO、Service、业务工具与共享业务能力。
- `sandwish-admin-api`:
  后台 WAR 应用，包含后台 Controller、配置、JSP、标签、静态资源和 vendor JAR。
- `sandwish-front-api`:
  前台 WAR 应用，包含前台 Controller、配置、JSP、静态资源和访问适配。

## Layer Router

- Controller / Servlet / Filter / Interceptor:
  关注请求入口、参数接收、登录态、权限、响应组装和 Web 适配。
- Service:
  关注业务流程、事务、校验、跨 DAO 编排和业务规则。
- DAO / Mapper / Entity / SQL:
  关注持久化访问、对象映射和查询性能。
- JSP / tag / static:
  关注展示、交互和资源引用，不承载核心业务规则。

## Load Limits

- 单模块任务：不要默认加载其他模块文档。
- 数据库任务：不要顺手加载全部需求文档。
- 页面任务：只加载涉及的 JSP、静态资源和对应业务文档。
- 跨模块任务：只加载涉及的模块。
- commit 整理、纯格式调整、无实现判断的机械修改：
  不额外加载业务需求文档。
- 只有当当前文档明确引用下一个文档时，才继续向下追。

## TODO Lifecycle

- 根目录 `TODO.md` 是任务执行队列，不是完成历史。
- 已完成任务不得在 `TODO.md` 中打勾长期保留，必须直接删除。
- 删除已完成 TODO 项必须和完成该任务的代码、文档或测试修改放在同一个 commit。
- 任务只完成一部分时，不得删除整项；必须拆分或收窄为剩余未完成内容。
- 待讨论项完成决策后，必须删除待讨论项；若仍需执行，新增明确执行项。
- 完成历史以 commit / PR 保留，不在 `TODO.md` 中重复记录。

## Directory Map

- `00-governance/`: 全局规则
- `00-governance/how-to/`: 操作手册
- `10-requirements/`: 业务需求
- `20-database/`: 数据库设计
- `30-designs/`: 专项设计
- `40-readiness/`: 上线与运维
- `50-prompts/`: 固定格式提示词模板
- `60-human/`: 人类阅读材料
