# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `oauth-auth-session`：OAuth2 令牌交换接入 PrincipalAuthSession
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/AuthControllerContractTest.java`
  - 处理动作：authorization code exchange 创建 `PrincipalAuthSession`，OAuth refresh token 沿用旧 session。
  - 验收点：OAuth access token introspection 同时判断 token 和 `PrincipalAuthSession`。
  - 重要度：9/10

- [ ] `principal-login-event-model`：新增主体登录事件审计模型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalLoginEvent.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/valueobject/PrincipalLoginEventId.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/codec/PrincipalLoginEventIdCodec.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalLoginEventType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalAuthenticationMethod.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/PrincipalLoginEventDao.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/PrincipalLoginEventDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/PrincipalLoginEventMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/PrincipalLoginEventPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalLoginEventDaoImpl.java`
    - `db/schema/auth.sql`
    - `docs/20-database/AUTH-DATABASE-DESIGN.md`
  - 处理动作：新增 `PrincipalLoginEvent` DB 审计事实、枚举、DAO 和 schema。
  - 验收点：`auth_principal_login_event` 表结构和 DAO 映射完整，`Open Items` 中固定 reason 已进入枚举或常量。
  - 重要度：9/10

- [ ] `principal-login-event-write`：认证链路写入主体登录事件
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/controller/LoginController.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java`
  - 处理动作：登录成功、登录失败、logout、refresh、OAuth authorized 写入 `PrincipalLoginEvent`。
  - 验收点：Controller 直接传入 `ip` 和 `userAgent`，Service 不依赖 Servlet API，既有异常语义不变。
  - 重要度：9/10

- [ ] `permission-session-merge`：将 PermissionSession 合并进 PrincipalAuthSession.values
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PermissionSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/PermissionDao.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/PermissionService.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/PermissionServiceImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PermissionDaoImpl.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/security/filter/AccessTokenAuthenticationFilter.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/PrincipalAuthSession.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/PrincipalAuthSessionDaoImpl.java`
  - 处理动作：把 admin 权限集合写入 `PrincipalAuthSession.values["PERMISSIONS"]`，删除独立 `PermissionSession` 存活职责。
  - 验收点：权限读取来自 `PrincipalAuthSession.values`，`PermissionDao` 和 `PermissionDaoImpl` 不再存在。
  - 重要度：9/10

- [ ] `legacy-auth-session-delete`：删除旧 AuthSession 和 MemberAuthSession 链路
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AuthSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/MemberAuthSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/AuthSessionStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/MemberAuthSessionStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/AuthSessionDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/AuthSessionRuntimeDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/MemberAuthSessionDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/MemberAuthSessionRuntimeDao.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthSessionPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/MemberAuthSessionPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/AuthSessionDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/MemberAuthSessionDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/AuthSessionMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/MemberAuthSessionMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/MemberAuthSessionDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/MemberAuthSessionRuntimeDaoImpl.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImplTest.java`
    - `db/schema/auth.sql`
    - `docs/20-database/AUTH-DATABASE-DESIGN.md`
  - 处理动作：删除旧 admin/member 认证会话实体、DAO、DO、Mapper、实现、测试和表结构。
  - 验收点：代码、schema、数据库设计文档中不再存在旧认证会话链路。
  - 重要度：10/10

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
