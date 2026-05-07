# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `assist-signature-biz`：删除 Signature 领域与 Service 能力
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/Signature.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/dao/SignatureDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/SignatureService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/SignService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/query/SignatureQuery.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/SignatureServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AbstractSignServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/DefaultSignServiceImpl.java`
  - 处理动作：删除 Signature 领域对象、查询对象、DAO 契约、Service 契约和默认签名实现。
  - 验收点：生产代码中不存在 `SignatureService`、`SignService`、`SignatureDao` 的引用。
  - 重要度：9/10

- [ ] `assist-signature-infra`：删除 Signature 持久化实现
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dataobject/SignatureDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/mapper/SignatureMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dao/SignatureDaoImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/SignaturePersistenceAssembler.java`
    - `sandwish-infra/src/test/java/com/github/thundax/architecture/DataObjectAnnotationArchitectureTest.java`
  - 处理动作：删除 Signature DO、Mapper、DAO 实现、持久化 assembler，并移除架构测试中的 SignatureDO 例外。
  - 验收点：infra 模块中不存在 `SignatureDO`、`SignatureMapper`、`SignatureDaoImpl` 的引用。
  - 重要度：9/10

- [ ] `assist-signature-api`：删除后台 Signature API 与默认配置
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/SignatureController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/SignatureInterfaceAssembler.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/SignatureDeleteRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/SignaturePageRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/SignatureVerifyRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/SignatureResponse.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/SignatureVerifyResponse.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/DefaultSignatureConfiguration.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/autoconfigure/DefaultSignatureConfiguration.java`
  - 处理动作：删除后台 Signature Controller、请求响应对象、接口 assembler 和前后台默认签名配置。
  - 验收点：admin-api 和 front-api 不再装配 `SignService`，后台不再暴露 Signature 管理接口。
  - 重要度：9/10

- [ ] `sys-signature-side-effect`：删除系统业务 Service 签名副作用
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/LogServiceImpl.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java`
  - 处理动作：删除构造器中的 `SignService` 依赖和新增、更新、删除流程中的签名调用。
  - 验收点：系统业务 Service 不再调用 `sign`、`deleteSign`，认证日志不再设置 `signable`。
  - 重要度：9/10

- [ ] `sys-signable-domain`：删除 Signable 领域接口和签名字段
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/common/domain/Signable.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Log.java`
  - 处理动作：删除 `Signable` 接口、领域对象上的 `Signable` 实现和只服务于签名设计的方法或字段。
  - 验收点：生产代码中不存在 `Signable`、`getSignName`、`getSignId`、`getSignBody`、`isSignable`、`setSignable`。
  - 重要度：8/10

- [ ] `signature-tests`：更新删除 Signature 后的测试
  - 范围文件：
    - `sandwish-biz/src/test/java/com/github/thundax/modules/assist/service/impl/SignatureServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/RoleServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/MenuServiceImplTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/LogServiceImplTest.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/assist/controller/SignatureControllerContractTest.java`
  - 处理动作：删除 Signature 专项测试，移除系统业务 Service 测试中的 `SignService` mock、记录器和断言。
  - 验收点：biz、infra、admin-api 相关测试不再依赖 Signature 设计。
  - 重要度：8/10

- [ ] `signature-doc-sync`：同步删除 Signature 设计的文档基线
  - 范围文件：
    - `docs/30-designs/DO-ANNOTATION-BASELINE.md`
    - `TODO.md`
  - 处理动作：删除 `SignatureDO` 注解基线记录，并在实现完成时删除、拆分或收窄本次 TODO。
  - 验收点：文档统计和待办项与删除后的代码状态一致。
  - 重要度：7/10

## 待讨论项
