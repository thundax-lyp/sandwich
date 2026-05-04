# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `auth-cleanup`：清理 OAuth/Auth 迁移现场
  - 范围文件：TODO.md
    docs/30-designs/AUTH-OAUTH-MIGRATION-RUNBOOK.md
    sandwish-biz/src/main/java/com/github/thundax/modules/auth
    sandwish-infra/src/main/java/com/github/thundax/modules/auth
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth
  - 处理动作：删除已完成 TODO、清理失效 RUNBOOK、空目录和迁移临时口径
  - 验收点：TODO 只保留未完成项，代码和文档无失效 OAuth/Auth 迁移说明
  - 重要度：10/10

## 待审阅任务项

## 待讨论项
