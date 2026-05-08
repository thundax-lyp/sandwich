# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `auth-principal-admin-write`：后台用户身份和密码维护改写 Principal
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/CurrentUserServiceImpl.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/CurrentUserController.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/CurrentUserServiceImplTest.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/sys/controller/CurrentUserControllerContractTest.java`
  - 处理动作：将后台用户登录名、密码设置、改密和重置密码的生产读写改为 `PrincipalIdentityService` / `PrincipalCredentialService`
  - 验收点：后台用户维护流程不再通过 `UserIdentityService` / `UserCredentialService` 完成身份和密码维护
  - 重要度：9/10

- [ ] `auth-principal-member-write`：会员注册和密码维护改写 Principal
  - 范围文件：
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberRegistrationServiceImpl.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberServiceImplTest.java`
    - `sandwish-front-api/src/test/java/com/github/thundax/modules/auth/assembler/MemberLoginInterfaceAssemblerTest.java`
  - 处理动作：将会员账号、手机、邮箱身份和会员密码维护改为 `PrincipalIdentityService` / `PrincipalCredentialService`
  - 验收点：会员注册、登录前置身份维护和密码维护不再依赖 `MemberIdentityService` / `MemberCredentialService`
  - 重要度：9/10

- [ ] `auth-principal-admin-read`：后台认证读取改读 Principal
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/result/AuthTokenQueryResult.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/AuthControllerContractTest.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/security/AuthPermissionLifecycleTest.java`
  - 处理动作：将后台密码、短信、wecom 和 github 登录统一改为 `PrincipalAuthService` 读取 Principal 身份和凭据
  - 验收点：`AdminAuthServiceImpl` 不再直接依赖 `UserIdentity` / `UserCredential` 模型或 DAO
  - 重要度：9/10

- [ ] `auth-principal-member-read`：会员认证读取改读 Principal
  - 范围文件：
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/auth/service/impl/PrincipalAuthServiceImplTest.java`
  - 处理动作：将会员密码和短信登录统一改为 `PrincipalAuthService` 读取 Principal 身份和凭据
  - 验收点：`MemberAuthServiceImpl` 不再依赖 `MemberIdentity` / `MemberCredential` 模型或 Service
  - 重要度：9/10

- [ ] `auth-principal-admin-display`：后台系统展示读取改读 Principal
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/LogController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java`
  - 处理动作：将后台日志、角色和用户展示所需登录名读取改为 Principal 身份读取
  - 验收点：后台展示 Controller 不再依赖 `UserIdentityService`
  - 重要度：7/10

- [ ] `auth-principal-session-entity`：认证会话实体改用 Principal identity 语义
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AuthSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/MemberAuthSession.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthSessionPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/MemberAuthSessionPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/MemberAuthSessionRuntimeDaoImpl.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionRuntimeDaoImplTest.java`
  - 处理动作：将认证会话 `identityId` / `identityType` 迁移为指向 `PrincipalIdentity.id` 和 `PrincipalIdentityType`
  - 验收点：新写入会话使用 Principal identity，读取边界兼容旧 session 字符串值
  - 重要度：9/10

- [ ] `auth-principal-admin-compat-shell-removal`：拆除后台旧身份凭据兼容壳
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserIdentityService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserCredentialService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserIdentityServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserIdentityServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceTest.java`
  - 处理动作：删除后台旧 Identity / Credential Service 兼容层及对应测试
  - 验收点：生产代码和测试代码中不存在 `UserIdentityService` / `UserCredentialService` 引用
  - 重要度：8/10

- [ ] `auth-principal-member-compat-shell-removal`：拆除会员旧身份凭据兼容壳
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberIdentityService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberCredentialService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberIdentityServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberCredentialServiceImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberIdentityServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberCredentialServiceImplTest.java`
  - 处理动作：删除会员旧 Identity / Credential Service 兼容层及对应测试
  - 验收点：生产代码和测试代码中不存在 `MemberIdentityService` / `MemberCredentialService` 引用
  - 重要度：8/10

- [ ] `auth-principal-admin-model-removal`：删除后台旧身份凭据模型和持久化实现
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserIdentityDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserCredentialDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UserIdentity.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UserCredential.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/UserIdentityType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/UserIdentityStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/UserCredentialType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/UserCredentialStatus.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserIdentityPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserCredentialPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/UserIdentityDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/UserCredentialDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dataobject/UserIdentityDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dataobject/UserCredentialDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/UserIdentityMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/UserCredentialMapper.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/sys/persistence/assembler/UserIdentityPersistenceAssemblerTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/sys/persistence/assembler/UserCredentialPersistenceAssemblerTest.java`
  - 处理动作：删除后台旧 Identity / Credential Entity、Enum、DAO、DO、Mapper、assembler 和测试
  - 验收点：后台旧身份凭据模型在 biz 和 infra 中不存在
  - 重要度：8/10

- [ ] `auth-principal-member-model-removal`：删除会员旧身份凭据模型和持久化实现
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberIdentityDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberCredentialDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/MemberIdentity.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/MemberCredential.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberIdentityType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberIdentityStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberCredentialType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberCredentialStatus.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberIdentityPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberCredentialPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberIdentityDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberCredentialDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dataobject/MemberIdentityDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dataobject/MemberCredentialDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/mapper/MemberIdentityMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/mapper/MemberCredentialMapper.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/member/entity/MemberCredentialTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/member/persistence/assembler/MemberIdentityPersistenceAssemblerTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/member/persistence/assembler/MemberCredentialPersistenceAssemblerTest.java`
  - 处理动作：删除会员旧 Identity / Credential Entity、Enum、DAO、DO、Mapper、assembler 和测试
  - 验收点：会员旧身份凭据模型在 biz 和 infra 中不存在
  - 重要度：8/10

- [ ] `auth-principal-sql-cleanup`：清理仓库 SQL 中的旧身份凭据表定义和初始化
  - 范围文件：
    - `db/schema/system.sql`
    - `db/schema/member.sql`
    - `db/data/system.sql`
    - `db/data/member.sql`
  - 处理动作：删除仓库 SQL 脚本中的 `sys_user_identity` / `sys_user_credential` / `member_identity` / `member_credential` 定义和初始化内容
  - 验收点：仓库 SQL 脚本不再创建或初始化旧 Identity / Credential 表
  - 重要度：8/10

- [ ] `auth-principal-document-sync`：同步 Auth Principal 文档口径
  - 范围文件：
    - `docs/10-requirements/AUTH-REQUIREMENTS.md`
    - `docs/10-requirements/MEMBER-REQUIREMENTS.md`
    - `docs/10-requirements/SYSTEM-REQUIREMENTS.md`
    - `docs/20-database/AUTH-DATABASE-DESIGN.md`
    - `docs/20-database/MEMBER-DATABASE-DESIGN.md`
    - `docs/20-database/SYSTEM-DATABASE-DESIGN.md`
    - `docs/30-designs/DO-ANNOTATION-BASELINE.md`
  - 处理动作：将旧 Identity / Credential 文档描述收敛为 Principal Auth 模型口径
  - 验收点：文档中不再把 `UserIdentity` / `UserCredential` / `MemberIdentity` / `MemberCredential` 描述为目标模型
  - 重要度：8/10

- [ ] `auth-principal-verification`：执行迁移验收和残留引用扫描
  - 范围文件：
    - `docs/30-designs/RUNBOOK-AUTH-PRINCIPAL-MIGRATION.md`
    - `TODO.md`
  - 处理动作：按 RUNBOOK 执行残留引用扫描和 Maven 测试，收窄或删除已完成 TODO
  - 验收点：`rg "UserIdentity|UserCredential|MemberIdentity|MemberCredential" sandwish-biz sandwish-admin-api sandwish-front-api sandwish-infra docs` 仅剩明确允许的历史说明，相关 Maven 测试通过
  - 重要度：8/10

## 待讨论项
