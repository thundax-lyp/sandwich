# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `auth-identity-biz`：新增登录标识业务对象
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/UserIdentity.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/UserIdentityType.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/UserIdentityStatus.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/UserIdentityDao.java
  - 处理动作：新增 `UserIdentity` 实体、枚举和 DAO 契约
  - 验收点：业务层可按登录标识类型和值定位唯一用户身份
  - 重要度：9/10

- [ ] `auth-credential-biz`：新增认证凭据业务对象
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/UserCredential.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/UserCredentialType.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/UserCredentialStatus.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/UserCredentialDao.java
  - 处理动作：新增 `UserCredential` 实体、枚举和 DAO 契约
  - 验收点：业务层可表达密码凭据、失败次数、锁定、过期和强制改密状态
  - 重要度：9/10

- [ ] `auth-session-biz`：新增认证会话业务对象
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AuthSession.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/AuthSessionStatus.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/AuthSessionDao.java
  - 处理动作：新增 `AuthSession` 实体、状态枚举和 DAO 契约
  - 验收点：业务层可表达会话签发、活跃、登出、失效和过期状态
  - 重要度：8/10

- [ ] `auth-identity-infra`：实现登录标识持久化
  - 范围文件：sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/UserIdentityDO.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/UserIdentityMapper.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/UserIdentityPersistenceAssembler.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/UserIdentityDaoImpl.java
  - 处理动作：新增 `UserIdentity` DO、Mapper、assembler 和 DAO 实现
  - 验收点：infra 可按 `identityType + identityValue` 唯一查询并维护身份状态
  - 重要度：9/10

- [ ] `auth-credential-infra`：实现认证凭据持久化
  - 范围文件：sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/UserCredentialDO.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/UserCredentialMapper.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/UserCredentialPersistenceAssembler.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/UserCredentialDaoImpl.java
  - 处理动作：新增 `UserCredential` DO、Mapper、assembler 和 DAO 实现
  - 验收点：infra 可按 `identityId + credentialType` 查询并写回凭据失败和锁定状态
  - 重要度：9/10

- [ ] `auth-session-infra`：实现认证会话持久化
  - 范围文件：sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/AuthSessionDO.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/mapper/AuthSessionMapper.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthSessionPersistenceAssembler.java
    sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dao/AuthSessionDaoImpl.java
  - 处理动作：新增 `AuthSession` DO、Mapper、assembler 和 DAO 实现
  - 验收点：infra 可按 token 或 sessionId 查询并更新会话活跃、登出和失效状态
  - 重要度：8/10

- [ ] `sys-user-save-identity`：用户保存链路同步登录身份
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java
    sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserEncryptService.java
    sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DatabaseUserEncryptServiceImpl.java
  - 处理动作：用户创建、修改登录名和重置密码时同步维护默认身份和密码凭据
  - 验收点：新增或修改后台用户后可通过 `UserIdentity + UserCredential` 完成认证前置数据装载
  - 重要度：9/10

- [ ] `admin-auth-login-credential`：后台登录切换到身份凭据认证
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AuthServiceImpl.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/dao/LoginLockDao.java
  - 处理动作：登录校验改为先解析 `UserIdentity`，再校验 `UserCredential`
  - 验收点：登录成功、密码失败、凭据锁定和身份禁用路径均由新模型驱动
  - 重要度：10/10

- [ ] `admin-auth-session-lifecycle`：后台登录登出写入认证会话
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AuthServiceImpl.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/security/filter/AccessTokenAuthenticationFilter.java
    sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PermissionService.java
  - 处理动作：登录成功创建 `AuthSession`，请求活跃 touch，会话登出或失效时更新状态
  - 验收点：token、权限会话和认证会话生命周期一致且可审计
  - 重要度：8/10

- [ ] `auth-migration-tests`：补齐认证模型迁移测试
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/auth/service/impl/AuthCredentialServiceTest.java
    sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/assembler/UserIdentityPersistenceAssemblerTest.java
    sandwish-infra/src/test/java/com/github/thundax/modules/auth/persistence/assembler/UserCredentialPersistenceAssemblerTest.java
    sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/security/AuthPermissionLifecycleTest.java
  - 处理动作：补齐身份解析、凭据状态流转、持久化转换和后台登录生命周期测试
  - 验收点：新增模型的核心状态流转和现有登录权限生命周期都有测试覆盖
  - 重要度：9/10

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
