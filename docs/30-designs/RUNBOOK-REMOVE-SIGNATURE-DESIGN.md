# RUNBOOK 删除 Signature 设计

## 1. Purpose

本文档定义删除 `Signature` 设计的执行步骤、边界和验收口径。

目标是移除业务实体签名留痕能力，清理 `Signature` 领域模型、`SignService` 编排、持久化对象、后台接口、自动配置和对应测试，避免业务 Service 继续维护无实际价值的签名副作用。

## 2. Scope

当前范围：

- `sandwish-biz` 中 `Signature`、`SignService`、`Signable` 及系统业务 Service 对签名服务的依赖
- `sandwish-infra` 中 `SignatureDO`、`SignatureMapper`、`SignatureDaoImpl` 和持久化装配
- `sandwish-admin-api` 中后台 `SignatureController`、请求响应对象、接口 assembler 和签名默认配置
- `sandwish-front-api` 中签名默认配置
- 受影响的 biz、infra、admin-api 测试
- `docs/30-designs/DO-ANNOTATION-BASELINE.md` 中 `SignatureDO` 基线记录
- `TODO.md` 中删除任务的待审阅执行入口

不在范围内：

- 不删除存储模块的 `business_type` 字段和对象引用能力
- 不删除 AspectJ `MethodSignature` 等 Java 框架概念
- 不删除加密、认证或第三方协议中真实需要的 `sign` 字段、方法或包名
- 不新增替代签名设计
- 不新增业务审计或变更留痕设计

## 3. Execution Order

### 3.1 删除 Signature 自身能力

固定先删除 `Signature` 独立能力，避免其他模块继续依赖已废弃接口。

涉及文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/Signature.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/dao/SignatureDao.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/SignatureService.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/SignService.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/query/SignatureQuery.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/SignatureServiceImpl.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AbstractSignServiceImpl.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/DefaultSignServiceImpl.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dataobject/SignatureDO.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/mapper/SignatureMapper.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dao/SignatureDaoImpl.java`
- `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/SignaturePersistenceAssembler.java`

验收口径：

- 项目内不存在 `SignatureService`、`SignService`、`SignatureDao`、`SignatureDO`、`SignatureMapper` 的生产代码引用。
- `assist` 模块中不再提供 `Signature` 的领域、DAO、Service 和 persistence 实现。

### 3.2 删除后台 Signature API

后台 API 固定直接删除，不保留空 Controller、空响应对象或兼容接口。

涉及文件：

- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/SignatureController.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/SignatureInterfaceAssembler.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/SignatureDeleteRequest.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/SignaturePageRequest.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/SignatureVerifyRequest.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/SignatureResponse.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/SignatureVerifyResponse.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/DefaultSignatureConfiguration.java`
- `sandwish-front-api/src/main/java/com/github/thundax/autoconfigure/DefaultSignatureConfiguration.java`

验收口径：

- 后台不再暴露 `/sys/signature/**` 或同等签名校验接口。
- admin-api 和 front-api 不再装配默认 `SignService`。

### 3.3 清理系统业务 Service 的签名副作用

系统业务 Service 固定删除签名依赖、构造器参数和 `sign` / `deleteSign` 调用。

涉及文件：

- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImpl.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/LogServiceImpl.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java`

验收口径：

- 上述 Service 构造器不再接收 `SignService`。
- 新增、更新、删除用户、角色、菜单、认证凭据、系统日志时不再产生签名记录。
- `AuthController` 不再设置日志签名标记。

### 3.4 清理 Signable 领域接口

`Signable` 固定随 `Signature` 设计一起删除。领域对象不保留只服务于签名设计的方法。

涉及文件：

- `sandwish-biz/src/main/java/com/github/thundax/common/domain/Signable.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java`
- `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Log.java`

验收口径：

- 生产代码中不存在 `Signable` 引用。
- `User`、`Role`、`Menu`、`Log` 不再暴露 `getSignName`、`getSignId`、`getSignBody`。
- `Log` 不再包含 `signable` 字段。

### 3.5 更新测试

测试固定跟随行为变化删除签名断言和签名测试夹具。

涉及文件：

- `sandwish-biz/src/test/java/com/github/thundax/modules/assist/service/impl/SignatureServiceImplTest.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserServiceImplTest.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserCredentialServiceImplTest.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/RoleServiceImplTest.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/MenuServiceImplTest.java`
- `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/LogServiceImplTest.java`
- `sandwish-admin-api/src/test/java/com/github/thundax/modules/assist/controller/SignatureControllerContractTest.java`
- `sandwish-infra/src/test/java/com/github/thundax/architecture/DataObjectAnnotationArchitectureTest.java`

验收口径：

- 删除 `SignatureServiceImplTest` 和 `SignatureControllerContractTest`。
- 系统业务 Service 测试不再 mock 或记录 `SignService`。
- `DataObjectAnnotationArchitectureTest` 不再把 `SignatureDO` 纳入例外清单。

### 3.6 同步文档和任务

文档固定只同步被删除能力的基线，不扩展新设计。

涉及文件：

- `docs/30-designs/DO-ANNOTATION-BASELINE.md`
- `TODO.md`

验收口径：

- `DO-ANNOTATION-BASELINE.md` 删除 `SignatureDO` 记录并修正统计数。
- 完成代码删除时，对应 `TODO.md` 项同步删除、拆分或收窄。

## 4. Verification

执行后固定运行：

```bash
rg "SignatureService|SignService|Signable|SignatureDao|SignatureDO|SignatureMapper|getSignName|getSignId|getSignBody|setSignable|isSignable" sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api docs TODO.md
mvn -pl sandwish-biz -am test
mvn -pl sandwish-infra -am test
mvn -pl sandwish-admin-api -am test
mvn install
git status --short
```

`rg` 结果只允许出现已审阅且与本设计无关的命中。测试失败时，先修复与删除签名设计直接相关的编译或断言问题。

## 5. Open Items

无
