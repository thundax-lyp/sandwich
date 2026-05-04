# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `auth-migration-cleanup`：清理认证模型迁移现场
  - 范围文件：TODO.md
    docs/30-designs/AUTH-IDENTITY-CREDENTIAL-RUNBOOK.md
    sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java
    sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UserEncrypt.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/LoginLockDao.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/LoginLockDaoImpl.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AuthServiceImpl.java
  - 处理动作：删除或收窄迁移完成后残留的旧密码、账号维度锁定和 TODO 任务项
  - 验收点：认证链路无旧凭据语义残留，已完成 TODO 被删除或收窄，Runbook 与最终实现一致
  - 重要度：8/10

## 待讨论项
