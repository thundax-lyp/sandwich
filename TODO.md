# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `auth-session-runtime-tests`：补齐认证会话 Redis 运行态测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/security/AuthPermissionLifecycleTest.java
    sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImplTest.java
  - 处理动作：覆盖登录写运行态、请求 touch 运行态、登出落库收口和 TTL 刷新
  - 验收点：认证会话运行态和数据库审计态的生命周期都有测试断言
  - 重要度：9/10

- [ ] `auth-session-runtime-cleanup`：清理认证会话 Redis 迁移现场
  - 范围文件：TODO.md
    docs/30-designs/AUTH-IDENTITY-CREDENTIAL-RUNBOOK.md
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AuthServiceImpl.java
  - 处理动作：删除或收窄已完成 TODO，确认 Runbook 与最终实现一致
  - 验收点：TODO 不保留完成历史，Runbook 不残留数据库逐请求 touch 语义
  - 重要度：8/10

## 待讨论项
