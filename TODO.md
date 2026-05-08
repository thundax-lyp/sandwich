# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `login-form-migration-verification`：执行登录表单迁移验收和 RUNBOOK 收口
  - 范围文件：
    - `docs/30-designs/RUNBOOK-LOGIN-FORM-BIZ-MIGRATION.md`
    - `TODO.md`
  - 处理动作：执行残留引用扫描和 Maven 测试，删除或收窄已完成 TODO，并清理 RUNBOOK
  - 验收点：入口模块生产代码无登录表单 DAO 直连，全量 Maven 测试通过，工作区干净
  - 重要度：8/10

## 待讨论项
