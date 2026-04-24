# DOCUMENT REQUIREMENTS

## 1. Purpose

本文档定义项目内需求文档和 AI 输入文档的统一写作要求。  
目标是让文档适合 AI 读取、适合工程实现、适合持续维护。

## 2. File Naming

- 文档文件名使用大写英文
- 文件名可使用 `-` 连接单词
- 文件名不得使用中文
- 文件名不得使用空格
- `HOW-TO` 文档固定放在 `docs/00-governance/how-to/`
- `HOW-TO` 文档命名固定为 `HOW-TO-XXX.md`

## 3. Language Rules

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

## 4. Content Principles

- 文档必须清晰、明确、可执行
- 文档必须适合 AI 读取
- 文档必须适合直接指导实现
- 文档不得包含冗余说明
- 文档不得过度简化
- 文档不得保留模糊口径
- 同一规则不得在多处重复且表述不一致
- 文档治理可以学习 Bacon，架构形态不照搬 Bacon

## 5. Requirement Style

- 使用确定性表达
- 不使用“建议”“可考虑”“视情况”“后续再看”“如有需要”等不确定措辞
- 已确认的规则必须直接写成约束
- 需要固定的内容必须明确写成“固定”
- `Open Items` 为空时明确写 `无`

## 6. Structure Requirements

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

## 7. Cross-Document Linking Rules

- `AGENTS.md` 只写仓库级入口规则。
- `docs/AGENT.md` 负责 AI 文档加载路由。
- `00-governance/ARCHITECTURE.md` 是实现、修改、评审代码前的强制入口。
- 具体任务只继续读取当前文档明确引用的下一级文档。
- 不得把 `docs/` 当作默认全量上下文。
- 新增稳定规则时，必须同步对应入口文档中的导航关系。

## 8. TODO And Commit Rules

- `TODO.md` 是任务执行队列，不是完成历史。
- 完成历史保留在 commit / PR 中。
- 任务收口必须按 `how-to/HOW-TO-CLOSE-A-TASK-WITH-TODO-TESTS-AND-COMMIT.md` 检查。
- 提交格式固定为 `Type(domain): 中文说明`。

## 9. Open Items

无
