# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `snowflake-final-close`：全局雪花 ID 迁移收口
  - 范围文件：
    - `TODO.md`
    - `docs/30-designs/RUNBOOK-GLOBAL-SNOWFLAKE-ID-MIGRATION.md`
  - 处理动作：完成全局雪花 ID 迁移后删除 RUNBOOK，并删除、拆分或收窄已完成 TODO 项。
  - 验收点：`rg "ASSIGN_UUID|UuidIdGenerator|UuidHelper" ...` 和 `mvn install` 通过，工作区干净。
  - 重要度：10/10

## 待讨论项
