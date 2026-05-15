# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


## 待审阅任务项

- [ ] `open-api-module-bootstrap`：创建 sandwish-open-api 入口模块空工程
  - 范围文件：
    - `pom.xml`
    - `sandwish-open-api/pom.xml`
    - `sandwish-open-api/src/main/java/com/github/thundax/OpenApiApplication.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/autoconfigure/SandwishConfiguration.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/autoconfigure/SandwishProperties.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java`
    - `sandwish-open-api/src/main/resources/config/application.yml`
    - `sandwish-open-api/src/main/resources/config/logback-spring.xml`
    - `sandwish-open-api/src/main/resources/i18n/messages.properties`
    - `sandwish-open-api/src/main/resources/i18n/messages_zh_CN.properties`
    - `sandwish-open-api/src/test/java/com/github/thundax/OpenApiApplicationTest.java`
  - 处理动作：新增可编译、可测试、context path 为 `/open-api` 的 Open API 入口模块。
  - 验收点：`mvn -pl sandwish-open-api -am test` 能执行到新模块测试。
  - 重要度：10/10

- [ ] `open-client-biz-model`：增加 OpenClient 业务模型和主体枚举
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/entity/OpenClient.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/entity/OpenClientPermission.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/entity/enums/OpenClientStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/entity/valueobject/OpenClientId.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/entity/valueobject/OpenClientIdCodec.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/entity/valueobject/OpenClientPermissionId.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/entity/valueobject/OpenClientPermissionIdCodec.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/dao/OpenClientDao.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/service/OpenClientService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/service/command/CreateOpenClientCommand.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/service/command/ResetOpenClientSecretCommand.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/service/command/UpdateOpenClientCommand.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/service/query/OpenClientQuery.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/service/dto/OpenClientDTO.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/open/service/impl/OpenClientServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalIdentityType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalCredentialType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/enums/AuditOperatorType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/AuditOperatorResolver.java`
  - 处理动作：建立 OpenClient 业务主体、权限模型、API KEY/SECRET 主体枚举和 Audit operator 映射。
  - 验收点：OpenClient Service 可创建、启停、重置 secret、维护权限，Audit 可解析 `OPEN_CLIENT`。
  - 重要度：10/10

- [ ] `open-client-database-infra`：增加 OpenClient 数据库设计、SQL 和持久化实现
  - 范围文件：
    - `docs/20-database/OPEN-API-DATABASE-DESIGN.md`
    - `docs/AGENT.md`
    - `db/AGENT.md`
    - `db/schema/open.sql`
    - `db/data/open.sql`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/open/persistence/dataobject/OpenClientDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/open/persistence/dataobject/OpenClientPermissionDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/open/persistence/mapper/OpenClientMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/open/persistence/mapper/OpenClientPermissionMapper.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/open/persistence/assembler/OpenClientPersistenceAssembler.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/open/persistence/dao/OpenClientDaoImpl.java`
  - 处理动作：落 `open_client`、`open_client_permission` 表设计、初始化 SQL 和 infra 持久化端口。
  - 验收点：`client_id + permission` 唯一约束存在，`ip_whitelist` 可保存 JSON array 字符串，infra 测试通过。
  - 重要度：10/10

- [ ] `open-client-admin-api`：增加 OpenClient 后台管理最小入口
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/assembler/OpenClientInterfaceAssembler.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/OpenClientController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/request/OpenClientIdRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/request/OpenClientPageRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/request/OpenClientSaveRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/request/OpenClientSecretResetRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/request/OpenClientStatusRequest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/response/OpenClientResponse.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/open/controller/response/OpenClientSecretResponse.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/open/controller/OpenClientControllerContractTest.java`
    - `db/data/system.sql`
  - 处理动作：提供后台 OpenClient 创建、分页、详情、启停、重置 secret 和权限维护入口。
  - 验收点：创建和重置接口只在响应中返回一次 API SECRET 明文，查询接口不返回 API SECRET 明文。
  - 重要度：9/10

- [ ] `open-api-auth-foundation`：实现 Open API 签名认证和权限校验基础设施
  - 范围文件：
    - `sandwish-open-api/src/main/java/com/github/thundax/common/exception/OpenApiExceptionTranslator.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/common/exception/OpenApiResponseExceptions.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/OpenApiSecurityConfiguration.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/filter/OpenApiAuthenticationFilter.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/OpenApiHeaders.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/OpenApiCanonicalRequest.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/OpenApiSignatureVerifier.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/OpenApiNonceStore.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/OpenApiIpWhitelistMatcher.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/auth/security/OpenApiPermissionChecker.java`
    - `sandwish-open-api/src/main/resources/i18n/messages.properties`
    - `sandwish-open-api/src/main/resources/i18n/messages_zh_CN.properties`
    - `sandwish-open-api/src/test/java/com/github/thundax/modules/auth/security/OpenApiSignatureVerifierTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/modules/auth/security/OpenApiNonceStoreTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/modules/auth/security/OpenApiIpWhitelistMatcherTest.java`
  - 处理动作：实现 API KEY、timestamp、nonce、body hash、HMAC 签名、IP 白名单、权限校验和 OpenClient 上下文注入。
  - 验收点：任一认证失败不会进入 Controller，认证成功后 `SandwishContextHolder` 为 `OPEN_CLIENT`。
  - 重要度：10/10

- [ ] `open-api-submission-endpoints`：增加 Open API Submission 创建和图片上传接口
  - 范围文件：
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/storage/helper/StorageUploadStreamHelper.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/storage/controller/response/StorageUploadResponse.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/storage/assembler/StorageInterfaceAssembler.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/submission/assembler/SubmissionInterfaceAssembler.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/submission/controller/SubmissionController.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/submission/controller/request/SubmissionSaveRequest.java`
    - `sandwish-open-api/src/main/java/com/github/thundax/modules/submission/controller/response/SubmissionResponse.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/modules/submission/controller/SubmissionControllerContractTest.java`
  - 处理动作：开放 `POST /api/submission/submission/create` 和 `POST /api/submission/submission/image/upload`，上传 helper 使用 stream-based 接口。
  - 验收点：两个接口分别校验 `submission:submission:create` 和 `submission:submission:image:upload`，上传 helper 不依赖 `HttpServletRequest`，并复用 `SubmissionService` 与 Storage 能力。
  - 重要度：10/10

- [ ] `open-api-architecture-tests`：补齐 Open API 模块架构和契约测试
  - 范围文件：
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ApiSurfaceArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ExceptionLayeringArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/InterfaceAssemblerArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/RequestAnnotationArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ResponseAnnotationArchitectureTest.java`
    - `sandwish-open-api/src/test/java/com/github/thundax/architecture/ServletRegistrationArchitectureTest.java`
  - 处理动作：让新入口模块被 API surface、异常分层、Request/Response、Assembler 和 Servlet 注册门禁覆盖。
  - 验收点：`mvn -pl sandwish-open-api -am test` 包含架构测试且全部通过。
  - 重要度：9/10

- [ ] `open-api-runbook-cleanup`：完成 Open API RUNBOOK 现场清理
  - 范围文件：
    - `TODO.md`
    - `docs/30-designs/RUNBOOK-OPEN-API-MODULE.md`
    - `docs/10-requirements/OPEN-API-REQUIREMENTS.md`
    - `docs/30-designs/OPEN-API-AUTH-DESIGN.md`
    - `docs/30-designs/OPEN-API-ERROR-CODE-DESIGN.md`
    - `docs/20-database/OPEN-API-DATABASE-DESIGN.md`
  - 处理动作：执行完成后删除 RUNBOOK、删除或收窄已完成 TODO，并确认文档口径与代码一致。
  - 验收点：`TODO.md` 不保留已完成任务，RUNBOOK 被清理，`git status --short` 无无关修改。
  - 重要度：8/10

## 待讨论项
