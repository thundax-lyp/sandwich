# Document Rules

## 1. Purpose

本文档定义项目内需求文档和 AI 输入文档的统一写作要求。  
目标是让文档适合 AI 读取、适合工程实现、适合持续维护。

## 2. Scope

当前范围：

- `docs/AGENT.md` 文档读取路由
- `docs/00-governance/` 治理文档
- `docs/00-governance/how-to/` 操作手册
- `docs/10-requirements/` 业务需求文档
- `docs/20-database/` 数据库设计文档
- `docs/30-designs/` 专项方案
- `docs/40-readiness/` 上线准备文档
- `docs/50-prompts/` 人工触发的固定格式提示词模板
- `docs/60-human/` 人类阅读材料与项目叙事
- `TODO.md` 任务执行队列的文档口径

不在范围内：

- 不替代具体业务需求
- 不替代数据库设计细节
- 不替代部署运行手册
- 不作为代码风格工具配置
- 不把 `docs/50-prompts/` 和 `docs/60-human/` 作为 AI 默认输入上下文

## 3. Governance Entry Map

治理文档固定入口：

- 架构、模块边界、三层职责：[`ARCHITECTURE.md`](./ARCHITECTURE.md)
- 命名、目录、类归属：[`NAMING-AND-PLACEMENT-RULES.md`](./NAMING-AND-PLACEMENT-RULES.md)
- admin-web 前端治理、命名、目录和交互规则：[`ADMIN-WEB-RULES.md`](./ADMIN-WEB-RULES.md)
- 数据库、DAO、Mapper、SQL、持久化对象：[`DATABASE-RULES.md`](./DATABASE-RULES.md)
- 领域标识、数据库主键和业务编号边界：[`UNIFIED-ID-DESIGN.md`](./UNIFIED-ID-DESIGN.md)
- HTTP API 注解矩阵：[`API-ANNOTATION-MATRIX.md`](./API-ANNOTATION-MATRIX.md)
- 当前用户、会员和线程上下文透传：[`CONTEXT-PROPAGATION-RULES.md`](./CONTEXT-PROPAGATION-RULES.md)
- TODO 格式、协作、删除、测试检查和提交收口规则：[`TODO-RULES.md`](./TODO-RULES.md)
- 文档写作、路由和提交口径：本文档
- 部署单元、运行入口、前后台流量边界：[`DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md`](./DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md)

新增稳定治理规则时，必须放入对应入口文档。临时执行步骤只放入 `docs/30-designs/` 或 `how-to/`，不得混入长期架构红线。

## 4. File Naming

- 文档文件名使用大写英文
- 文件名可使用 `-` 连接单词
- 文件名不得使用中文
- 文件名不得使用空格
- `HOW-TO` 文档固定放在 `docs/00-governance/how-to/`
- `HOW-TO` 文档命名固定为 `HOW-TO-XXX.md`
- `RUNBOOK` 文档固定放在 `docs/30-designs/`
- `RUNBOOK` 文档命名固定为 `RUNBOOK-XXX.md`

## 5. RUNBOOK And HOW-TO Boundary

`RUNBOOK` 和 `HOW-TO` 固定按用途区分。

`RUNBOOK` 是一次性临时执行手册，用于某一个复杂任务的执行编排。`RUNBOOK` 固定服务于具体任务，不沉淀为长期通用流程。

`RUNBOOK` 通常与一组 `TODO.md` 任务一起出现。伴随 `RUNBOOK` 出现的 `TODO.md` 任务一般精确到文件，并固定放在 `待审阅任务项`，经人工审阅后再执行。

`RUNBOOK` 最后必须被清理。清理 `RUNBOOK` 通常作为伴随 `TODO.md` 任务的最后一项，和残留引用扫描、测试验证、文档收口、工作区状态检查一起完成。

`RUNBOOK` 适用场景：

- 跨模块删除、迁移或重构
- 涉及代码、测试、文档和收口验证的一次性任务
- 需要先固定执行顺序、范围边界和验收命令的复杂任务

`HOW-TO` 是长期复用的操作手册，用于沉淀稳定、通用、可反复执行的方法。

AI 不得自行新增 `HOW-TO-*` 文档。只有用户明确准许新增 `HOW-TO-*` 时，AI 才能创建或扩展 `HOW-TO` 文档。

当任务目标是执行某个复杂清理、迁移或改造时，固定优先使用 `RUNBOOK-*`。当任务目标是沉淀长期通用流程，并且用户明确准许新增 `HOW-TO-*` 时，才使用 `HOW-TO-*`。

## 6. Language Rules

- 文档说明内容使用中文
- 代码定义相关名称使用英文
- 下列内容必须使用英文原文：
  - 模块名
  - 类名
  - 接口名
  - 服务名
  - `Controller` / `Service` / `DAO` / `Mapper`
  - `DTO` / `VO`
  - 字段名
  - 枚举值
  - 缓存键
  - 权限编码
- 不为了“纯中文”而翻译代码概念
- 不为了“纯英文”而把业务说明改成英文

## 7. Content Principles

- 文档必须清晰、明确、可执行
- 文档必须适合 AI 读取
- 文档必须适合直接指导实现
- 需求文档、数据库设计文档和专项设计文档必须直指目标状态
- 文档不得包含冗余说明
- 文档不得过度简化
- 文档不得保留模糊口径
- 文档不得保留历史包袱、迁移过渡口径、旧模型兼容说明或已完成执行过程
- 同一规则不得在多处重复且表述不一致
- 文档必须保持 Sandwich 项目独立口径
- 文档不得引用外部项目作为正式规则来源
- 治理文档只沉淀稳定规则，不记录完成清单
- 临时讨论结论进入 `TODO.md` 或 `RUNBOOK`；稳定后再收敛到治理文档，完成后从临时材料中清理
- `docs/50-prompts/` 只保存人工明确触发的生成提示词，不承载工程规则、业务需求或完成清单
- `docs/60-human/` 只保存人类阅读材料、项目叙事和非实现约束材料，不承载 AI 默认执行规则

## 8. Rule Expression Style

- 使用确定性表达
- 规约条目应该优先使用正向表达，直接说明代码、目录、依赖、接口或文档应该是什么
- 少用“不应该”“不应”等负向表达；负向表达只用于明确禁止项，且必须能被人工 review、testcase、ArchUnit 或 checkstyle 验证
- 不使用“推荐”“建议”“可考虑”“视情况”“后续再看”“如有需要”等不确定措辞表达规则
- 已确认的规则必须直接写成约束
- 需要固定的内容必须明确写成“固定”
- `Open Items` 为空时明确写 `无`

## 9. Structure Requirements

需求文档优先采用以下结构：

1. `Purpose`
2. `Scope`
3. `Bounded Context`
4. `Module Mapping`
5. `Core Business Objects`
6. `Global Constraints`
7. `Functional Requirements`
8. `Key Flows`
9. `Non-Functional Requirements`
10. `Open Items`

治理与 `HOW-TO` 文档按对应入口文档要求编写。

`RUNBOOK` 文档至少说明：

- `Purpose`
- `Scope`
- `Execution Order`
- `Verification`
- `Open Items`

治理文档至少说明：

- `Purpose`
- `Scope`
- 对应规则
- 与其他治理文档的边界
- `Open Items`

## 10. Cross-Document Linking Rules

- `AGENTS.md` 只写仓库级入口规则。
- `docs/AGENT.md` 负责 AI 文档加载路由。
- `00-governance/ARCHITECTURE.md` 是实现、修改、评审代码前的强制入口。
- 具体任务只继续读取当前文档明确引用的下一级文档。
- 不得把 `docs/` 当作默认全量上下文。
- `docs/50-prompts/` 和 `docs/60-human/` 固定不进入 AI 默认读取路径；只有用户明确要求使用其中某个文件时才读取。
- 新增稳定规则时，必须同步对应入口文档中的导航关系。
- 文档之间只链接直接下一级必读入口，不用索引式罗列无关文档。

## 11. TODO And Commit Rules

- `TODO.md` 是任务执行队列，不是完成清单。
- 完成记录保留在 commit / PR 中。
- 任务收口必须按 `TODO-RULES.md` 检查。
- 提交格式固定为 `Type(domain): 中文说明`。
- 已完成 `TODO.md` 项必须删除、拆分或收窄，并与对应文档、代码或测试改动放在同一个 commit。

## 12. Open Items

无
