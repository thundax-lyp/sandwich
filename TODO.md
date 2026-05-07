# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `signature-cleanup`：清理 Signature 删除后的现场
  - 范围文件：
    - `docs/30-designs/RUNBOOK-REMOVE-SIGNATURE-DESIGN.md`
    - `TODO.md`
  - 处理动作：按 RUNBOOK 执行残留引用扫描、相关模块测试、全量 `mvn install`、工作区状态检查，并删除本次 RUNBOOK。
  - 验收点：`rg` 不再发现未审阅的 Signature 设计残留，相关测试和 `mvn install` 通过，`RUNBOOK-REMOVE-SIGNATURE-DESIGN.md` 已删除，`git status --short` 干净。
  - 重要度：9/10

## 待讨论项
