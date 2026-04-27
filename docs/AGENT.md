# Docs Agent

只给 AI / harness 读。目标：少读，快读，读对。

## Core Rules

- 只加载完成当前任务必需的文档。
- 先读治理文档，再读业务文档。
- `docs/` 不是默认全量输入目录。
- 工程级规则优先于业务模块文档。
- Sandwich 固定采用三层 API 架构。
- 文档必须保持项目独立口径，不引用外部项目作为正式规则来源。

## Mandatory Entry

实现、修改、评审代码前固定先读：

1. [`00-governance/ARCHITECTURE.md`](./00-governance/ARCHITECTURE.md)

## Task Router

- 纯实现、修 bug、重构业务逻辑：
  读 `ARCHITECTURE.md`，再读对应 `10-requirements/*-REQUIREMENTS.md`
- 需要解释架构意图、规则冲突、分层取舍、小步提交意图或 AI 误改风险：
  读 `00-governance/ARCHITECTURE-INTENT.md`
- 新增类、改类名、改目录、判断模块归属：
  再读 `00-governance/NAMING-AND-PLACEMENT-RULES.md`
- 数据库、实体、DAO、Mapper、SQL、持久化查询：
  再读 `00-governance/DATABASE-RULES.md`
  再读对应 `20-database/*-DATABASE-DESIGN.md`
- 静态资源、API 文档、前后台接口入口：
  先读 `ARCHITECTURE.md`
  再读对应业务需求文档和专项设计文档
- 改文档：
  再读 `00-governance/DOCUMENT-RULES.md`
- 上线准备、运维、发布、jar 打包：
  先读 `00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`
  再读 `40-readiness/`
- TODO 协作、任务拆解、人机审阅：
  读 `00-governance/how-to/HOW-TO-RUN-TODO-COLLABORATION.md`
- 任务收口、测试检查、文档同步、小步提交：
  读 `00-governance/how-to/HOW-TO-CLOSE-A-TASK-WITH-TODO-TESTS-AND-COMMIT.md`
- 新增或修改 `HOW-TO` 操作手册：
  读 `00-governance/how-to/HOW-TO-HOW-TO.md`
- 专项方案、路线图、跨模块设计：
  按需读 `30-designs/`

## Module Router

- `sandwish-common`:
  Common 聚合模块。
- `sandwish-common-core`:
  通用工具、基础 Web、加密、i18n、Redis、存储和线程等非持久化公共技术能力。
- `sandwish-common-mybatis`:
  通用持久化对象、DAO / Service 基类、MyBatis 扫描标记、PageHelper 和数据库方言支撑。
- `sandwish-common-security`:
  通用 Spring Security 接入、权限匹配规则和不依赖业务数据的安全契约。
- `sandwish-biz`:
  业务实体、DAO、Service、业务工具与共享业务能力。
- `sandwish-admin-api`:
  后台 API 应用，包含后台 Controller、配置、过滤器、拦截器、Swagger 和静态 API 支撑资源。
- `sandwish-front-api`:
  前台 API 应用，包含前台 Controller、配置、过滤器、拦截器和访问适配。

## Layer Router

- Controller / Servlet / Filter / Interceptor:
  关注请求入口、参数接收、登录态、权限、响应组装和 Web 适配。
- Service:
  关注业务流程、事务、校验、跨 DAO 编排和业务规则。
- DAO / Mapper / Entity / SQL:
  关注持久化访问、对象映射和查询性能。
- static:
  仅保留 API 支撑静态资源，不承载核心业务规则。

## Load Limits

- 单模块任务：不要默认加载其他模块文档。
- 数据库任务：不要顺手加载全部需求文档。
- API 资源任务：只加载涉及的静态资源和对应业务文档。
- 跨模块任务：只加载涉及的模块。
- commit 整理、纯格式调整、无实现判断的机械修改：
  不额外加载业务需求文档。
- 只有当当前文档明确引用下一个文档时，才继续向下追。

## TODO Lifecycle

- 根目录 `TODO.md` 是任务执行队列，不是完成历史。
- 宏观任务进入 `TODO.md` 后，按 [`00-governance/how-to/HOW-TO-RUN-TODO-COLLABORATION.md`](./00-governance/how-to/HOW-TO-RUN-TODO-COLLABORATION.md) 完成人机讨论、任务拆解、人工审阅和执行关闭。
- 已完成任务不得在 `TODO.md` 中打勾长期保留，必须直接删除。
- 删除已完成 TODO 项必须和完成该任务的代码、文档或测试修改放在同一个 commit。
- 任务只完成一部分时，不得删除整项；必须拆分或收窄为剩余未完成内容。
- 待讨论项完成决策后，必须删除待讨论项；若仍需执行，新增明确执行项。
- 完成历史以 commit / PR 保留，不在 `TODO.md` 中重复记录。

## Commit Lifecycle

- 所有文件修改在任务结束前必须提交。
- 提交前按 [`00-governance/how-to/HOW-TO-CLOSE-A-TASK-WITH-TODO-TESTS-AND-COMMIT.md`](./00-governance/how-to/HOW-TO-CLOSE-A-TASK-WITH-TODO-TESTS-AND-COMMIT.md) 完成收口判断。
- commit 固定格式：`Type(domain): 中文说明`
- 每个 commit 只收敛一个明确判断。
- 不相关修改必须拆分提交。
- commit message 必须说明具体能力变化，不写空泛的“调整”“修改”“优化”。

## Directory Map

- `00-governance/`: 全局规则
- `00-governance/how-to/`: 操作手册
- `10-requirements/`: 业务需求
- `20-database/`: 数据库设计
- `30-designs/`: 专项设计
- `40-readiness/`: 上线与运维
- `50-prompts/`: 固定格式提示词模板
- `60-human/`: 人类阅读材料
