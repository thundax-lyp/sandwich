# DOCUMENT ROUTING MAP

## 1. Purpose

本文档用图表达 Sandwich 文档加载路径，帮助人类检查和优化文档路由。

本文档是 human 文档，不是 AI 默认加载入口。AI 执行任务时仍以 [`../../AGENTS.md`](../../AGENTS.md)、[`../AGENT.md`](../AGENT.md) 和具体文档内的直接链接为准。

本文档只用于回答这些问题：

- 某类任务从入口会读到哪些文档。
- 某条文档依赖链是否清晰。
- 某个文档是否缺少上游加载路径。
- `docs/AGENT.md` 是否存在漏路由、重复路由或过度路由。

## 2. Scope

当前覆盖关键文档加载路径：

- 仓库入口。
- `docs/AGENT.md` 任务路由。
- 治理文档入口。
- 需求文档、数据库设计文档和专项设计文档的典型路径。
- Open API 相关路径。
- TODO / RUNBOOK 协作路径。

当前不覆盖范围：

- 不作为全量文档索引。
- 不替代 `docs/AGENT.md`。
- 不要求 AI 在普通任务中读取本文档。
- 不记录每个文件的完整出入边。

## 3. Reading Rule

真实加载规则固定在两处：

1. `docs/AGENT.md`：任务类型到文档的路由规则。
2. 具体文档内部链接：当前文档到直接依赖文档的下一级链接。

本文档中的连线只表达“人类理解上的加载条件”。当本文档和真实加载规则不一致时，必须修改真实加载规则或本文档，使二者重新一致。

## 4. Entry Map

```mermaid
flowchart TD
  Root["./AGENTS.md"] -->|"所有任务先读仓库规则"| DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"实现 / 修改 / 评审代码"| Architecture["00-governance/ARCHITECTURE.md"]
  DocsAgent -->|"TODO 协作 / 任务收口 / 提交收口"| TodoRules["00-governance/TODO-RULES.md"]
  DocsAgent -->|"改文档"| DocumentRules["00-governance/DOCUMENT-RULES.md"]
  DocsAgent -->|"新增类 / 改类名 / 判断模块归属"| NamingRules["00-governance/NAMING-AND-PLACEMENT-RULES.md"]
  DocsAgent -->|"数据库 / DAO / Mapper / SQL"| DatabaseRules["00-governance/DATABASE-RULES.md"]
  DocsAgent -->|"API 注解 / 静态 API 文档 / 响应包装"| ApiMatrix["00-governance/API-ANNOTATION-MATRIX.md"]
  DocsAgent -->|"登录态 / 当前主体 / 线程上下文"| ContextRules["00-governance/CONTEXT-PROPAGATION-RULES.md"]
```

## 5. Business Route Map

```mermaid
flowchart TD
  DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"后台系统管理 / 用户 / 角色 / 菜单 / 部门 / 字典 / 系统日志"| SysReq["10-requirements/SYSTEM-REQUIREMENTS.md"]
  DocsAgent -->|"后台系统管理数据库"| SysDb["20-database/SYSTEM-DATABASE-DESIGN.md"]

  DocsAgent -->|"后台认证 / 前台会员认证 / token / OAuth2 / 认证会话"| AuthReq["10-requirements/AUTH-REQUIREMENTS.md"]
  DocsAgent -->|"认证数据库 / principal / credential / session / OAuth2 表"| AuthDb["20-database/AUTH-DATABASE-DESIGN.md"]

  DocsAgent -->|"存储对象 / 对象引用 / 分片上传 / OSS 适配"| StorageReq["10-requirements/STORAGE-REQUIREMENTS.md"]
  DocsAgent -->|"存储数据库"| StorageDb["20-database/STORAGE-DATABASE-DESIGN.md"]

  DocsAgent -->|"前台会员 / 会员主表 / 会员资料"| MemberReq["10-requirements/MEMBER-REQUIREMENTS.md"]
  DocsAgent -->|"前台会员数据库"| MemberDb["20-database/MEMBER-DATABASE-DESIGN.md"]

  DocsAgent -->|"提交内容 / submission / 占位业务域"| SubmissionReq["10-requirements/SUBMISSION-REQUIREMENTS.md"]
  DocsAgent -->|"提交内容数据库"| SubmissionDb["20-database/SUBMISSION-DATABASE-DESIGN.md"]

  DocsAgent -->|"数据审计 / 审计元数据 / 审计日志"| AuditReq["10-requirements/AUDIT-REQUIREMENTS.md"]
  DocsAgent -->|"数据审计数据库"| AuditDb["20-database/AUDIT-DATABASE-DESIGN.md"]
```

## 6. Open API Route Map

```mermaid
flowchart TD
  Root["./AGENTS.md"] -->|"所有任务入口"| DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"open-api / OpenClient / API KEY/SECRET / 第三方签名认证"| OpenReq["10-requirements/OPEN-API-REQUIREMENTS.md"]
  DocsAgent -->|"open-api / OpenClient / API KEY/SECRET / 第三方签名认证"| OpenAuth["30-designs/OPEN-API-AUTH-DESIGN.md"]

  OpenReq -->|"签名认证细节"| OpenAuth
  OpenReq -->|"错误码设计"| OpenErr["30-designs/OPEN-API-ERROR-CODE-DESIGN.md"]

  DocsAgent -->|"ExceptionTranslator / API error code / 异常响应"| ApiMatrix["00-governance/API-ANNOTATION-MATRIX.md"]
  ApiMatrix -->|"按入口选择 open-api error code"| OpenErr

  OpenReq -->|"初始开放业务：Submission 创建和图片上传"| SubmissionReq["10-requirements/SUBMISSION-REQUIREMENTS.md"]
  OpenReq -->|"图片上传和对象引用规则"| StorageReq["10-requirements/STORAGE-REQUIREMENTS.md"]
  OpenReq -->|"业务写操作操作者追踪"| AuditReq["10-requirements/AUDIT-REQUIREMENTS.md"]
```

## 7. Error Code Route Map

```mermaid
flowchart TD
  DocsAgent["docs/AGENT.md"] -->|"异常分层 / ExceptionTranslator / API error code"| Architecture["00-governance/ARCHITECTURE.md"]
  Architecture -->|"API 响应和异常入口规则"| ApiMatrix["00-governance/API-ANNOTATION-MATRIX.md"]

  ApiMatrix -->|"admin-api 错误码"| AdminErr["30-designs/ADMIN-API-ERROR-CODE-DESIGN.md"]
  ApiMatrix -->|"front-api 错误码"| FrontErr["30-designs/FRONT-API-ERROR-CODE-DESIGN.md"]
  ApiMatrix -->|"open-api 错误码"| OpenErr["30-designs/OPEN-API-ERROR-CODE-DESIGN.md"]
```

## 8. TODO And RUNBOOK Route Map

```mermaid
flowchart TD
  DocsAgent["docs/AGENT.md"]

  DocsAgent -->|"TODO 协作 / 任务拆解 / 任务列表重写"| TodoRules["00-governance/TODO-RULES.md"]
  TodoRules -->|"TODO 协作执行方法"| TodoHowTo["00-governance/how-to/HOW-TO-RUN-TODO-COLLABORATION.md"]

  DocsAgent -->|"任务收口 / 测试检查 / 文档同步 / 小步提交"| CloseHowTo["00-governance/how-to/HOW-TO-CLOSE-A-TASK-WITH-TODO-TESTS-AND-COMMIT.md"]

  DocsAgent -->|"一次性复杂任务 / 跨模块迁移 / 删除 / 重构"| DocumentRules["00-governance/DOCUMENT-RULES.md"]
  DocumentRules -->|"产物位置和边界"| Runbook["30-designs/RUNBOOK-*.md"]
  Runbook -->|"待审阅任务项"| Todo["TODO.md"]
```

## 9. Maintenance Checklist

新增或调整稳定文档路由时，人类维护者检查以下问题：

- 这个文档是否需要进入 `docs/AGENT.md` 的任务路由。
- 这个文档是否只需要被上游文档直接链接。
- 这条路由的加载条件是否足够明确。
- 是否存在另一个文档已经表达同一规则。
- 是否会让 AI 在普通任务中多读无关文档。
- 本文档的图是否需要同步调整。

## 10. Open Items

无
