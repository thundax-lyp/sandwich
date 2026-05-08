# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `auth-principal-document-sync`：同步 Auth Principal 文档口径
  - 范围文件：
    - `docs/10-requirements/AUTH-REQUIREMENTS.md`
    - `docs/10-requirements/MEMBER-REQUIREMENTS.md`
    - `docs/10-requirements/SYSTEM-REQUIREMENTS.md`
    - `docs/20-database/AUTH-DATABASE-DESIGN.md`
    - `docs/20-database/MEMBER-DATABASE-DESIGN.md`
    - `docs/20-database/SYSTEM-DATABASE-DESIGN.md`
    - `docs/30-designs/DO-ANNOTATION-BASELINE.md`
  - 处理动作：将旧 Identity / Credential 文档描述收敛为 Principal Auth 模型口径
  - 验收点：文档中不再把 `UserIdentity` / `UserCredential` / `MemberIdentity` / `MemberCredential` 描述为目标模型
  - 重要度：8/10

- [ ] `auth-principal-verification`：执行迁移验收和残留引用扫描
  - 范围文件：
    - `docs/30-designs/RUNBOOK-AUTH-PRINCIPAL-MIGRATION.md`
    - `TODO.md`
  - 处理动作：按 RUNBOOK 执行残留引用扫描和 Maven 测试，收窄或删除已完成 TODO
  - 验收点：`rg "UserIdentity|UserCredential|MemberIdentity|MemberCredential" sandwish-biz sandwish-admin-api sandwish-front-api sandwish-infra docs` 仅剩明确允许的历史说明，相关 Maven 测试通过
  - 重要度：8/10

## 待讨论项
