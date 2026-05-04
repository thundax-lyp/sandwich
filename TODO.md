# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `auth-session-runtime-model`：新增认证会话运行态端口
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/AuthSessionRuntimeDao.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AuthSession.java
  - 处理动作：定义按 token 读写、touch、delete 和 TTL 刷新的运行态会话契约
  - 验收点：业务层不直接依赖 Redis API，运行态会话字段可覆盖 token 请求链路
  - 重要度：10/10

- [ ] `auth-session-runtime-infra`：实现 Redis 认证会话运行态存储
  - 范围文件：sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImpl.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthSessionPersistenceAssembler.java
  - 处理动作：用缓存存储活跃 `AuthSession` 快照并按 token 有效期设置 TTL
  - 验收点：登录后可从 Redis 运行态读取会话，请求 touch 可刷新运行态过期时间
  - 重要度：9/10

- [ ] `auth-session-lifecycle-redis`：切换认证会话生命周期为 Redis 优先
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AuthServiceImpl.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/AuthService.java
  - 处理动作：登录写 Redis 和数据库，活跃请求只 touch Redis，登出用 Redis 最后访问态收口数据库
  - 验收点：正常请求不更新数据库 lastAccessTime，登出后数据库状态仍为 `LOGGED_OUT`
  - 重要度：10/10

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
