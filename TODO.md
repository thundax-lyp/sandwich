# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `Audit 3.8 final-cleanup`：最终完整验证并清理 RUNBOOK 现场
  - 范围文件：
    - `TODO.md`
    - `docs/30-designs/RUNBOOK-AUDIT-SERVICE-REFORM.md`
  - 处理动作：先执行完整 `mvn clean install`，再按完成情况删除或收窄 TODO 项，删除本 RUNBOOK，并执行残留扫描。
  - 验收点：`mvn clean install` 成功；残留扫描只命中 Audit 自身、`sys_log` 或 Open Items 明确字段；`TODO.md` 不保留已完成项；`RUNBOOK-AUDIT-SERVICE-REFORM.md` 已删除。
  - 重要度：10/10

## 待讨论项
