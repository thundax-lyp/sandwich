# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 以下任务项来自 `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md`。
- 领域实体重塑拆解仍处于人工审阅阶段，审阅通过前不得执行。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `migration-cleanup`：全量验证并清理迁移现场
  - 范围文件：
    - `docs/AGENT.md`
    - `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md`
    - `TODO.md`
  - 处理动作：完成所有迁移后，删除临时 RUNBOOK、撤销临时读取路由，并清理 `TODO.md`。
  - 验收点：`docs/AGENT.md` 不再引用 `HOW-TO-REMODEL-DOMAIN-ENTITY.md`；迁移 RUNBOOK 已删除；`TODO.md` 删除已完成任务或只保留剩余精确任务。
  - 重要度：10/10
