# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `auth-runbook-verification`：执行认证会话改造总验证
  - 范围文件：
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/security/AuthPermissionLifecycleTest.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/AuthControllerContractTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/dao/PrincipalAuthSessionDaoImplTest.java`
    - `TODO.md`
  - 处理动作：执行 RUNBOOK 固定验证命令和残留扫描，收窄或删除已完成 TODO。
  - 验收点：compile/test/diff check 通过，残留扫描只剩有意保留项，`TODO.md` 不保留完成项。
  - 重要度：10/10

- [ ] `auth-runbook-cleanup`：清理 PrincipalAuthSession RUNBOOK 现场
  - 范围文件：
    - `docs/30-designs/RUNBOOK-PRINCIPAL-AUTH-SESSION-REFORM.md`
    - `docs/20-database/AUTH-DATABASE-DESIGN.md`
    - `TODO.md`
  - 处理动作：将稳定口径收敛到数据库设计文档，删除或收窄一次性 RUNBOOK，并清理对应 TODO。
  - 验收点：RUNBOOK 不作为长期遗留文档存在，稳定结构进入正式文档，工作区没有无关残留。
  - 重要度：10/10

## 待讨论项
