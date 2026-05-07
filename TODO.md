# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `snowflake-sql-schema-auth`：改 SQL，迁移认证 schema ID 类型
  - 范围文件：
    - `db/schema/auth.sql`
  - 处理动作：将认证域 schema 中主键和关系字段调整为雪花 `bigint`。
  - 验收点：`db/schema/auth.sql` 中认证主表不再使用 `varchar(64)` 主键。
  - 重要度：9/10

- [ ] `snowflake-sql-schema-storage`：改 SQL，迁移存储 schema ID 类型
  - 范围文件：
    - `db/schema/storage.sql`
  - 处理动作：将存储域 schema 中主键和关系字段调整为雪花 `bigint`。
  - 验收点：`db/schema/storage.sql` 中存储主表不再使用 `varchar(64)` 主键。
  - 重要度：9/10

- [ ] `snowflake-sql-schema-member`：改 SQL，迁移会员 schema ID 类型
  - 范围文件：
    - `db/schema/member.sql`
  - 处理动作：将会员域 schema 中主键和关系字段调整为雪花 `bigint`。
  - 验收点：`db/schema/member.sql` 中会员主表不再使用 `varchar(64)` 主键。
  - 重要度：9/10

- [ ] `snowflake-sql-schema-audit`：改 SQL，复核 Audit schema ID 类型
  - 范围文件：
    - `db/schema/audit.sql`
  - 处理动作：复核 Audit schema 中 `id` / `meta_id` 为 `bigint`，`object_id` 为字符串。
  - 验收点：`db/schema/audit.sql` 与全局雪花 ID 规则一致。
  - 重要度：8/10

- [ ] `snowflake-sql-data-system`：改 SQL，迁移系统管理初始化 ID
  - 范围文件：
    - `db/data/system.sql`
  - 处理动作：将系统管理初始化数据中的固定 ID 调整为固定雪花 ID。
  - 验收点：`db/data/system.sql` 不再使用 `user-*`、`role-*`、`menu-*` 等字符串主键。
  - 重要度：10/10

- [ ] `snowflake-sql-data-auth`：改 SQL，迁移认证初始化 ID
  - 范围文件：
    - `db/data/auth.sql`
  - 处理动作：将认证初始化数据中的固定 ID 调整为固定雪花 ID。
  - 验收点：`db/data/auth.sql` 不再使用字符串数据库主键。
  - 重要度：9/10

- [ ] `snowflake-sql-data-storage`：改 SQL，迁移存储初始化 ID
  - 范围文件：
    - `db/data/storage.sql`
  - 处理动作：将存储初始化数据中的固定 ID 调整为固定雪花 ID。
  - 验收点：`db/data/storage.sql` 不再使用字符串数据库主键。
  - 重要度：8/10

- [ ] `snowflake-sql-data-member`：改 SQL，迁移会员初始化 ID
  - 范围文件：
    - `db/data/member.sql`
  - 处理动作：将会员初始化数据中的固定 ID 调整为固定雪花 ID。
  - 验收点：`db/data/member.sql` 不再使用字符串数据库主键。
  - 重要度：8/10

- [ ] `snowflake-sql-data-audit`：改 SQL，复核 Audit 初始化脚本
  - 范围文件：
    - `db/data/audit.sql`
  - 处理动作：复核 Audit 初始化脚本不需要固定主键种子。
  - 验收点：`db/data/audit.sql` 与 Audit 无固定种子规则一致。
  - 重要度：6/10

- [ ] `snowflake-db-agent`：改 SQL 入口，复核数据库脚本执行规则
  - 范围文件：
    - `db/AGENT.md`
  - 处理动作：复核数据库脚本执行规则与雪花 ID 初始化顺序一致。
  - 验收点：`db/AGENT.md` 不含 UUID 字符串主键初始化口径。
  - 重要度：7/10

- [ ] `snowflake-deploy-readme`：改部署说明，复核数据库初始化口径
  - 范围文件：
    - `deploy/sandwish-api/README.md`
  - 处理动作：复核部署说明中的数据库初始化顺序和固定种子说明。
  - 验收点：部署说明与雪花 ID SQL 基线一致。
  - 重要度：7/10

- [ ] `snowflake-code-common-id-model`：改代码，迁移 common ID 领域模型
  - 范围文件：
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/BaseId.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/BaseLongId.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/BaseStringId.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/EntityId.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/EntityIdCodec.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/EntityIdTest.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/EntityIdCodecTest.java`
  - 处理动作：将 `EntityId` 领域标识模型迁移到雪花 `Long` 口径。
  - 验收点：`EntityId` 与 `EntityIdCodec` 测试通过。
  - 重要度：10/10

- [ ] `snowflake-code-common-generator`：改代码，迁移 common ID 生成器
  - 范围文件：
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/IdGenerator.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/SnowflakeIdGenerator.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/UuidIdGenerator.java`
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/id/UuidHelper.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/SnowflakeIdGeneratorTest.java`
    - `sandwish-common/sandwish-common-core/src/test/java/com/github/thundax/common/id/UuidIdGeneratorTest.java`
  - 处理动作：将数据库主键生成默认能力收敛到雪花 ID。
  - 验收点：`SnowflakeIdGeneratorTest` 通过，UUID 不再作为数据库主键默认生成器。
  - 重要度：10/10

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
