# TODO List

## 1. 收敛领域实体身份根

目标：先把 `BaseEntity` 的身份能力移动到 domain 根，形成后续删除旧基类的稳定落点。

修改文件：

- `sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/domain/Entity.java`
- `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/BaseEntity.java`
- `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/DataEntity.java`
- `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/TreeEntity.java`

验收：

- `Entity` 只承载 `EntityId`、getter/setter、同类同 id 相等性。
- `BaseEntity` 不再承载不可迁移的新职责。
- 当前步骤保持可编译。

建议验证：

- `mvn -q -pl sandwish-common/sandwish-common-core,sandwish-common/sandwish-common-mybatis -am test`

建议提交：

- `Refactor(common): 收敛领域实体身份能力`

## 2. 建立领域能力契约与架构门禁

目标：先固定 `Auditable`、`Prioritized` 的 interface 边界，并用架构测试防止旧问题回流。

修改文件：

- `sandwish-biz/src/main/java/com/github/thundax/common/domain/Auditable.java`
- `sandwish-biz/src/main/java/com/github/thundax/common/domain/Prioritized.java`
- `sandwish-biz/src/test/java/com/github/thundax/architecture/DomainEntityArchitectureTest.java`

验收：

- `Auditable` 只能声明 `getCreateUserId`、`setCreateUserId`、`getUpdateUserId`、`setUpdateUserId`。
- `Prioritized` 只能声明 `getPriority`、`setPriority`。
- 两个 interface 不继承 `Entity`，不依赖 Jackson、MyBatis、Spring Web 或当前登录态。
- 架构测试禁止 `sandwish-biz` 的 `modules..entity..` 依赖 `com.fasterxml.jackson.annotation..`。
- 架构测试禁止新增业务实体继承 `BaseEntity`、`DataEntity`、`AdminDataEntity`、`TreeEntity`、`AdminTreeEntity`。

建议验证：

- `mvn -q -pl sandwish-biz -am test`

建议提交：

- `Test(architecture): 固定领域实体能力边界`

## 3. 迁移 sys 基础实体字段与 Jackson 注解

目标：按 domain 语义迁移系统域实体字段，移除 `sys` 实体对旧基类和 Jackson 注解的依赖。

修改文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUser.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUserEncrypt.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseRole.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseDict.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseLog.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseUploadFile.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseMenu.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/base/BaseOffice.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UserEncrypt.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Dict.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Log.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/UploadFile.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Office.java`

验收：

- 上述实体不再 import `com.fasterxml.jackson.annotation..`。
- 需要业务审计能力的实体显式实现 `Auditable`。
- 需要排序能力的实体显式实现 `Prioritized`。
- `remarks` 只作为具体业务备注保留，不进入 `Auditable`。
- `createDate`、`updateDate` 只在具备业务生命周期语义时保留；纯持久化审计不进入实体。
- `Menu`、`Office` 的树输出能力不再依赖 `AdminTreeEntity.toTreeData`。

建议验证：

- `mvn -q -pl sandwish-biz -am test`

建议提交：

- `Refactor(sys): 下沉系统实体领域字段`

## 4. 同步 sys API 与持久化装配

目标：让 sys 入口层和 infra 显式转换字段，不再依赖 `DataEntity` 通用拷贝。

修改文件：

- `sandwish-admin-api/src/main/java/com/github/thundax/common/web/BaseApiController.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/UserInterfaceAssembler.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/RoleInterfaceAssembler.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/OfficeInterfaceAssembler.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/MenuInterfaceAssembler.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/DictInterfaceAssembler.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/assembler/LogInterfaceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserPersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserEncryptPersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/RolePersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DictPersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/LogPersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UploadFilePersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/MenuPersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/OfficePersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/MenuDaoImpl.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/OfficeDaoImpl.java`

验收：

- sys assembler 不再 import `DataEntity`。
- `BaseApiController` 不再提供基于 `DataEntity` 的 `baseEntityToVo`、`baseVoToEntity`。
- sys persistence assembler 显式完成 `Entity <-> DO` 字段转换。
- `MenuDaoImpl`、`OfficeDaoImpl` 不再 import `TreeEntity`。

建议验证：

- `mvn -q -pl sandwish-admin-api,sandwish-infra -am test`

建议提交：

- `Refactor(sys): 显式转换系统实体字段`

## 5. 迁移 assist 实体、API 与持久化装配

目标：按 domain 语义迁移辅助域实体，移除旧基类和 Jackson 依赖。

修改文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/base/BaseAsyncTask.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/base/BaseSignature.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/AsyncTask.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/Signature.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/AsyncTaskInterfaceAssembler.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/SignatureInterfaceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/SignaturePersistenceAssembler.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/assist/service/impl/SignatureServiceImplTest.java`

验收：

- `AsyncTask`、`Signature` 及其 base 类不再继承 `DataEntity` 或 `AdminDataEntity`。
- `AsyncTask`、`Signature` 不再 import `com.fasterxml.jackson.annotation..`。
- assist assembler 不再 import `DataEntity`。
- persistence assembler 显式完成字段转换。

建议验证：

- `mvn -q -pl sandwish-biz,sandwish-admin-api,sandwish-infra -am test`

建议提交：

- `Refactor(assist): 下沉辅助实体领域字段`

## 6. 迁移 storage 实体、API 与持久化装配

目标：按 domain 语义迁移存储域实体，移除旧基类和 Jackson 依赖。

修改文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/base/BaseStorage.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/base/BaseStorageBusiness.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/Storage.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StorageBusiness.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/StorageInterfaceAssembler.java`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/storage/vo/StorageVo.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/vo/StorageVo.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssembler.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java`

验收：

- `Storage`、`StorageBusiness` 及其 base 类不再继承 `DataEntity`。
- `Storage`、`StorageBusiness` 不再 import `com.fasterxml.jackson.annotation..`。
- API 输出继续由 `StorageVo` 或明确响应模型承接 JSON 形态。
- persistence assembler 显式完成字段转换。

建议验证：

- `mvn -q -pl sandwish-biz,sandwish-admin-api,sandwish-front-api,sandwish-infra -am test`

建议提交：

- `Refactor(storage): 下沉存储实体领域字段`

## 7. 迁移 member 与 auth 实体边界

目标：按 domain 语义迁移会员和认证对象，清理 Jackson 注解和旧基类依赖。

修改文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/base/BaseMember.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/AccessToken.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/LoginForm.java`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/member/assembler/MemberLoginInterfaceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberPersistenceAssembler.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/auth/persistence/assembler/AuthPersistenceAssembler.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberServiceImplTest.java`

验收：

- `Member` 及其 base 类不再继承 `AdminDataEntity`。
- `Member`、`AccessToken`、`LoginForm` 不再 import `com.fasterxml.jackson.annotation..`。
- 会员生日、证件有效期等业务日期字段仍保留在 `Member`，JSON 日期格式移动到 API 模型。
- persistence assembler 显式完成字段转换。

建议验证：

- `mvn -q -pl sandwish-biz,sandwish-front-api,sandwish-infra -am test`

建议提交：

- `Refactor(member): 下沉会员实体领域字段`

## 8. 清理旧实体基类文件

目标：在所有业务对象迁移完成后，删除旧继承链。

修改文件：

- `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/BaseEntity.java`
- `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/DataEntity.java`
- `sandwish-common/sandwish-common-mybatis/src/main/java/com/github/thundax/common/persistence/TreeEntity.java`
- `sandwish-biz/src/main/java/com/github/thundax/common/persistence/AdminDataEntity.java`
- `sandwish-biz/src/main/java/com/github/thundax/common/persistence/AdminTreeEntity.java`

验收：

- `rg "BaseEntity|DataEntity|AdminDataEntity|TreeEntity|AdminTreeEntity" sandwish-common sandwish-biz sandwish-admin-api sandwish-front-api sandwish-infra` 不再出现生产代码引用。
- 旧基类文件已删除。

建议验证：

- `mvn -q -pl sandwish-common/sandwish-common-mybatis,sandwish-biz,sandwish-admin-api,sandwish-front-api,sandwish-infra -am test`

建议提交：

- `Refactor(common): 删除旧实体继承链`

## 9. 清理 KOAL 插件传输对象归属

目标：把第三方接口传输对象从业务 entity 语义中剥离，保留 Jackson 注解在 adapter/DTO 语义下。

修改文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/plugins/koal/sign/BaseResponse.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/plugins/koal/sign/SignRequestParam.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/plugins/koal/sign/SignResponseParam.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/plugins/koal/sign/VerifySignRequestParam.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/plugins/koal/sign/VerifySignResponseParam.java`

验收：

- KOAL 参数对象不再被纳入业务 `Entity` 清理目标。
- 如果保留 Jackson 注解，文件命名、包名或文档注释明确其为第三方接口 DTO。
- 不影响签名插件调用链。

建议验证：

- `mvn -q -pl sandwish-biz -am test`

建议提交：

- `Refactor(assist): 明确KOAL传输对象边界`

## 10. 全量验证并清理迁移现场

目标：完成所有迁移后的最终收口，删除临时 RUNBOOK 和任务队列。

修改文件：

- `docs/AGENT.md`
- `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md`
- `TODO.md`

验收：

- `docs/AGENT.md` 删除指向 `HOW-TO-REMODEL-DOMAIN-ENTITY.md` 的临时读取路由。
- `docs/00-governance/how-to/HOW-TO-REMODEL-DOMAIN-ENTITY.md` 已删除。
- `TODO.md` 删除已完成任务；若仍有未完成范围，只保留剩余精确任务。
- `rg "com\\.fasterxml\\.jackson\\.annotation" sandwish-biz/src/main/java/com/github/thundax/modules/*/entity sandwish-biz/src/main/java/com/github/thundax/common/domain` 无业务实体命中。
- `rg "extends (BaseEntity|DataEntity|AdminDataEntity|TreeEntity|AdminTreeEntity)" sandwish-common sandwish-biz sandwish-admin-api sandwish-front-api sandwish-infra` 无命中。

建议验证：

- `mvn test`

建议提交：

- `Docs(governance): 清理实体重塑迁移手册`
