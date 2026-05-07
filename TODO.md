# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `snowflake-code-mybatis-typehandler`：改代码，迁移 MyBatis ID 类型处理
  - 范围文件：
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/mybatis/typehandler/EntityIdTypeHandler.java`
    - `sandwish-common/sandwish-common-mybatis/src/test/java/com/github/thundax/common/mybatis/typehandler/EntityIdTypeHandlerTest.java`
  - 处理动作：将 MyBatis `EntityId` 类型处理与雪花 `Long` 主键口径对齐。
  - 验收点：`mvn -pl sandwish-common-mybatis -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-sys-biz`：改代码，迁移系统管理域 biz ID 类型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/RoleDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/MenuDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DepartmentDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DictDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Department.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Dict.java`
  - 处理动作：将系统管理域 biz 层 ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-biz -am test` 通过。
  - 重要度：10/10

- [ ] `snowflake-code-sys-infra`：改代码，迁移系统管理域 infra ID 类型
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dataobject/UserDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dataobject/RoleDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dataobject/MenuDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dataobject/DepartmentDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dataobject/DictDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/RolePersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/MenuPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DepartmentPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DictPersistenceAssembler.java`
  - 处理动作：将系统管理域 infra 层 DO 和 assembler ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-infra -am test` 通过。
  - 重要度：10/10

- [ ] `snowflake-code-auth-biz`：改代码，迁移认证域 biz ID 类型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AuthSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/OAuthClient.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/OAuthAuthorization.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/OAuthAccessToken.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/OAuthRefreshToken.java`
  - 处理动作：将认证域 biz 层 ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-biz -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-auth-infra`：改代码，迁移认证域 infra ID 类型
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/AuthSessionDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/OAuthClientDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/OAuthAuthorizationDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/OAuthAccessTokenDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/dataobject/OAuthRefreshTokenDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthSessionPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/OAuthClientPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/OAuthAuthorizationPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/OAuthAccessTokenPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/OAuthRefreshTokenPersistenceAssembler.java`
  - 处理动作：将认证域 infra 层 DO 和 assembler ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-infra -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-storage-biz`：改代码，迁移存储域 biz ID 类型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StoredObject.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StoredObjectReference.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/MultipartUploadSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/MultipartUploadPart.java`
  - 处理动作：将存储域 biz 层 ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-biz -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-storage-infra`：改代码，迁移存储域 infra ID 类型
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/StoredObjectDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/StoredObjectReferenceDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/MultipartUploadSessionDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/MultipartUploadPartDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssembler.java`
  - 处理动作：将存储域 infra 层 DO 和 assembler ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-infra -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-member-assist`：改代码，迁移会员和辅助域 ID 类型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dataobject/MemberDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberPersistenceAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/AsyncTask.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dataobject/AsyncTaskDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java`
  - 处理动作：将会员和辅助域 ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-biz,sandwish-infra -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-api-boundary`：改代码，迁移 API 边界 ID 类型
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/**/*.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/**/*.java`
  - 处理动作：将前后台 API Request、Response 和 assembler 中的 ID 类型与雪花 `Long` 口径对齐。
  - 验收点：`mvn -pl sandwish-admin-api,sandwish-front-api -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-final-close`：全局雪花 ID 迁移收口
  - 范围文件：
    - `TODO.md`
    - `docs/30-designs/RUNBOOK-GLOBAL-SNOWFLAKE-ID-MIGRATION.md`
  - 处理动作：完成全局雪花 ID 迁移后删除 RUNBOOK，并删除、拆分或收窄已完成 TODO 项。
  - 验收点：`rg "ASSIGN_UUID|UuidIdGenerator|UuidHelper" ...` 和 `mvn install` 通过，工作区干净。
  - 重要度：10/10

## 待讨论项
