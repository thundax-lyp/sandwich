# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


## 待审阅任务项

- [ ] `exception-governance-cleanup`：沉淀治理规则并清理 RUNBOOK
  - 范围文件：
    - `docs/00-governance/ARCHITECTURE.md`
    - `docs/00-governance/API-ANNOTATION-MATRIX.md`
    - `docs/00-governance/TODO-RULES.md`
    - `docs/AGENT.md`
    - `docs/30-designs/RUNBOOK-EXCEPTION-LAYERING.md`
    - `TODO.md`
  - 处理动作：将稳定异常分层和 API error code 规则沉淀到治理文档，更新 AI 路由，删除 RUNBOOK 并删除或收窄已完成 TODO。
  - 验收点：长期规则不依赖 RUNBOOK，`RUNBOOK-EXCEPTION-LAYERING.md` 已清理，`TODO.md` 只保留未完成任务。
  - 重要度：9/10

## 待讨论项
