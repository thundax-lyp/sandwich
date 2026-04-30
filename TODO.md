# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 以下任务项来自 `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md`。
- 领域实体重塑拆解仍处于人工审阅阶段，审阅通过前不得执行。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `common-domain`：收敛领域实体身份根
  - 范围文件：
    - `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/domain/Entity.java`
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/BaseEntity.java`
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/DataEntity.java`
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/TreeEntity.java`
  - 处理动作：把实体身份与相等性收敛到 `Entity`，旧基类只保留过渡职责。
  - 验收点：`Entity` 承载 `EntityId`、getter/setter 和同类同 id 相等性；当前步骤可编译。
  - 重要度：10/10

- [ ] `biz-domain`：建立领域能力接口
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/common/domain/Auditable.java`
    - `sandwish-biz/src/main/java/com/github/thundax/common/domain/Prioritized.java`
  - 处理动作：新增 `Auditable`、`Prioritized`，只表达领域能力契约。
  - 验收点：两个类型均为 interface，不继承 `Entity`，不依赖 Jackson、MyBatis、Spring Web 或当前登录态。
  - 重要度：10/10

- [ ] `biz-architecture`：建立领域实体架构门禁
  - 范围文件：
    - `sandwish-biz/src/test/java/com/github/thundax/architecture/DomainEntityArchitectureTest.java`
  - 处理动作：新增架构测试，禁止旧实体基类和 Jackson 注解回流到业务实体。
  - 验收点：测试覆盖 `modules..entity..` 禁止 Jackson 注解依赖，禁止继承 `BaseEntity`、`DataEntity`、`AdminDataEntity`、`TreeEntity`、`AdminTreeEntity`。
  - 重要度：10/10

- [ ] `sys-user-role`：迁移用户、用户加密和角色实体
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUser.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUserEncrypt.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseRole.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UserEncrypt.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java`
  - 处理动作：下沉用户、用户加密、角色实体字段，移除旧基类和 Jackson 注解依赖。
  - 验收点：上述实体不再继承 `AdminDataEntity`，不再 import `com.fasterxml.jackson.annotation..`，审计和排序语义显式实现对应 interface。
  - 重要度：10/10

- [ ] `sys-user-role-assembler`：同步用户、用户加密和角色装配
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/UserInterfaceAssembler.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/RoleInterfaceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserEncryptPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/RolePersistenceAssembler.java`
  - 处理动作：显式转换用户、用户加密、角色字段，移除 `DataEntity` 通用拷贝依赖。
  - 验收点：API assembler 不再 import `DataEntity`；persistence assembler 显式完成 `Entity <-> DO` 字段转换。
  - 重要度：10/10

- [ ] `sys-basic`：迁移字典、日志和上传文件实体
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseDict.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseLog.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUploadFile.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Dict.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Log.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UploadFile.java`
  - 处理动作：下沉字典、日志、上传文件实体字段，移除旧基类和 Jackson 注解依赖。
  - 验收点：上述实体不再继承 `AdminDataEntity`，不再 import `com.fasterxml.jackson.annotation..`，`remarks` 不进入 `Auditable`。
  - 重要度：9/10

- [ ] `sys-basic-assembler`：同步字典、日志和上传文件装配
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/DictInterfaceAssembler.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/LogInterfaceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DictPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/LogPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UploadFilePersistenceAssembler.java`
  - 处理动作：显式转换字典、日志、上传文件字段，移除 `DataEntity` 通用拷贝依赖。
  - 验收点：API assembler 不再 import `DataEntity`；persistence assembler 显式完成 `Entity <-> DO` 字段转换。
  - 重要度：9/10

- [ ] `sys-tree`：迁移菜单和机构树实体
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseMenu.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseOffice.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Office.java`
  - 处理动作：下沉菜单、机构树字段，移除 `AdminTreeEntity` 和 Jackson 注解依赖。
  - 验收点：`Menu`、`Office` 不再继承 `AdminTreeEntity`，不再 import `com.fasterxml.jackson.annotation..`，树输出形态不在业务实体父类中。
  - 重要度：10/10

- [ ] `sys-tree-assembler`：同步菜单和机构装配
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/MenuInterfaceAssembler.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/OfficeInterfaceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/MenuPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/OfficePersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/MenuDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/OfficeDaoImpl.java`
  - 处理动作：显式转换菜单、机构字段，并移除 infra 对 `TreeEntity` 的引用。
  - 验收点：API assembler 不再 import `DataEntity`；`MenuDaoImpl`、`OfficeDaoImpl` 不再 import `TreeEntity`。
  - 重要度：10/10

- [ ] `admin-base-controller`：清理入口层旧通用拷贝方法
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/common/web/BaseApiController.java`
  - 处理动作：删除入口层基于 `DataEntity` 的通用字段拷贝方法。
  - 验收点：`BaseApiController` 不再 import `DataEntity`，不再提供基于 `DataEntity` 的 `baseEntityToVo`、`baseVoToEntity`。
  - 重要度：9/10

- [ ] `assist`：迁移辅助域实体与装配
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/base/BaseAsyncTask.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/base/BaseSignature.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/AsyncTask.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/Signature.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/AsyncTaskInterfaceAssembler.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/SignatureInterfaceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/SignaturePersistenceAssembler.java`
  - 处理动作：下沉辅助域实体字段，移除旧基类、Jackson 注解和 `DataEntity` 装配依赖。
  - 验收点：`AsyncTask`、`Signature` 不再继承 `DataEntity` 或 `AdminDataEntity`，不再 import `com.fasterxml.jackson.annotation..`；assist assembler 不再 import `DataEntity`。
  - 重要度：8/10

- [ ] `storage`：迁移存储域实体与装配
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/base/BaseStorage.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/base/BaseStorageBusiness.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/Storage.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StorageBusiness.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/StorageInterfaceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssembler.java`
  - 处理动作：下沉存储域实体字段，移除旧基类和 Jackson 注解依赖。
  - 验收点：`Storage`、`StorageBusiness` 不再继承 `DataEntity`，不再 import `com.fasterxml.jackson.annotation..`；API 输出由入口层模型承接 JSON 形态。
  - 重要度：8/10

- [ ] `member-auth`：迁移会员与认证实体边界
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/base/BaseMember.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AccessToken.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/LoginForm.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/assembler/MemberLoginInterfaceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthPersistenceAssembler.java`
  - 处理动作：下沉会员实体字段，清理认证对象中的 Controller 层 JSON 注解。
  - 验收点：`Member` 不再继承 `AdminDataEntity`；`Member`、`AccessToken`、`LoginForm` 不再 import `com.fasterxml.jackson.annotation..`；JSON 日期格式移动到 API 模型。
  - 重要度：8/10

- [ ] `legacy-entity-base`：删除旧实体继承链
  - 范围文件：
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/BaseEntity.java`
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/DataEntity.java`
    - `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/TreeEntity.java`
    - `sandwish-biz/src/main/java/com/github/thundax/common/persistence/AdminDataEntity.java`
    - `sandwish-biz/src/main/java/com/github/thundax/common/persistence/AdminTreeEntity.java`
  - 处理动作：删除已经没有生产引用的旧实体基类文件。
  - 验收点：旧基类文件已删除；生产代码不再引用 `BaseEntity`、`DataEntity`、`AdminDataEntity`、`TreeEntity`、`AdminTreeEntity`。
  - 重要度：10/10

- [ ] `migration-cleanup`：全量验证并清理迁移现场
  - 范围文件：
    - `docs/AGENT.md`
    - `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md`
    - `TODO.md`
  - 处理动作：完成所有迁移后，删除临时 RUNBOOK、撤销临时读取路由，并清理 `TODO.md`。
  - 验收点：`docs/AGENT.md` 不再引用 `HOW-TO-REMODEL-DOMAIN-ENTITY.md`；迁移 RUNBOOK 已删除；`TODO.md` 删除已完成任务或只保留剩余精确任务。
  - 重要度：10/10
