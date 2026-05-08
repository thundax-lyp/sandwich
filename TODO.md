# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
