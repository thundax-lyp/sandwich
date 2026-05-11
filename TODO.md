# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


## 待审阅任务项

- [ ] `biz-exception-boundary`：增加 Service 异常边界注解和 AOP
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PrincipalAuthService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PrincipalCredentialService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PrincipalIdentityService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PreAuthSessionService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PrincipalAuthServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PrincipalCredentialServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PrincipalIdentityServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/service/AuditService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/service/impl/AuditServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/MultipartUploadService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/StorageService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/MultipartUploadServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/CurrentUserService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DepartmentService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/LogService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/MenuService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/CurrentUserServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/LogServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceDaoBoundaryArchitectureTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceMethodModelArchitectureTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceNamingArchitectureTest.java`
  - 处理动作：新增 `@BizExceptionBoundary` 和 AOP，放行 `DomainException`/`BizException`，捕获其他异常转换为带 cause 的 `BizException`。
  - 验收点：非 getter 的 Service implementation public 方法具备边界注解，AOP 转换行为有测试覆盖。
  - 重要度：10/10

- [ ] `biz-domain-exception-migration`：迁移领域枚举和值对象异常
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/OAuthClientStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalAuthenticationMethod.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalCredentialStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalCredentialType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalIdentityStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalIdentityType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalLoginEventType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalTokenStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/entity/enums/PrincipalType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/enums/AuditAction.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/entity/enums/AuditOperatorType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberGender.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/enums/MemberStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/MultipartUploadStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StorageOwnerType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StorageType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StoredObjectReferenceStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StoredObjectStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/LogType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/MenuVisibility.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/RolePrivilege.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/RoleStatus.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/UserPrivilege.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/enums/UserStatus.java`
  - 处理动作：将领域枚举和值对象的不变量失败从 `BizException` 迁移为 `DomainException`。
  - 验收点：领域对象、值对象和领域枚举不再抛泛业务异常，领域失败携带 String code/messageKey/defaultMessage。
  - 重要度：8/10

- [ ] `biz-service-api-exception-migration`：迁移 Biz Service 的 ApiException 调用点
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PrincipalAuthService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PrincipalAuthServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/CurrentUserService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/CurrentUserServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/StorageService.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java`
  - 处理动作：删除 Service 接口和实现中的 `throws ApiException`，将业务失败改为 `BizException` 或 `DomainException`。
  - 验收点：`sandwish-biz` 生产代码无 `throws ApiException`、`new ApiException`、`extends ApiException` 残留。
  - 重要度：10/10

- [ ] `api-error-translator`：建立前后台异常转换层
  - 范围文件：
    - `sandwish-common/sandwish-common-web/src/main/java/com/github/thundax/common/web/exception/GlobalExceptionHandler.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/exception/BannedAccountException.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/exception/InvalidUsernamePasswordException.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/exception/AdminExceptionTranslator.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/exception/FrontExceptionTranslator.java`
  - 处理动作：按入口建立 `AdminExceptionTranslator` 和 `FrontExceptionTranslator`，将 Biz/Domain/framework 异常转换为 `SandwishException`。
  - 验收点：translator 输出 code 均存在于对应 error code 文档，`GlobalExceptionHandler` 只负责响应组装。
  - 重要度：10/10

- [ ] `admin-api-exception-migration`：迁移后台 API 异常响应
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/CaptchaController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/AdminAuthService.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/security/filter/ResponseBodyWrapper.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/MenuController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DictController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DepartmentController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/StorageController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/MultipartUploadController.java`
  - 处理动作：删除后台 Controller 和后台认证 Service 中的 checked `ApiException`，统一通过 translator/SandwishException 输出 String code。
  - 验收点：后台 API contract tests 覆盖 String code，后台生产代码无 `ApiException` 使用点。
  - 重要度：10/10

- [ ] `front-api-exception-migration`：迁移前台 API 异常响应
  - 范围文件：
    - `sandwish-front-api/src/main/java/com/github/thundax/common/web/ReturnObject.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/controller/LoginController.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/controller/RegisterController.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/MemberAuthService.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/MemberRegistrationService.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberRegistrationServiceImpl.java`
  - 处理动作：删除前台 API 的 checked `ApiException` 和 int code 返回，统一通过前台 translator 输出 String code。
  - 验收点：前台 API tests 覆盖 String code，前台生产代码无 `ApiException` 使用点。
  - 重要度：10/10

- [ ] `infra-exception-boundary`：盘点并清理 Infra 业务异常依赖
  - 范围文件：
    - 搜索模式：`sandwish-infra/src/main/java/com/github/thundax/modules/**/persistence/**/*.java`
    - 搜索模式：`sandwish-infra/src/main/java/com/github/thundax/modules/**/store/**/*.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/sys/persistence/assembler/UserPersistenceAssemblerTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/sys/persistence/assembler/RolePersistenceAssemblerTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/sys/persistence/assembler/MenuPersistenceAssemblerTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/sys/persistence/assembler/LogPersistenceAssemblerTest.java`
  - 处理动作：移除 Infra 生产和测试对 `BizException`、`DomainException`、`SandwishException` 的依赖，只保留技术异常。
  - 验收点：`sandwish-infra` 无业务异常和 API 响应异常依赖，无 `InfraException`。
  - 重要度：8/10

- [ ] `exception-architecture-gates`：增加异常分层架构门禁
  - 范围文件：
    - `sandwish-biz/src/test/java/com/github/thundax/architecture/ExceptionLayeringArchitectureTest.java`
    - `sandwish-biz/src/test/java/com/github/thundax/architecture/BizExceptionBoundaryArchitectureTest.java`
    - `sandwish-admin-api/src/test/java/com/github/thundax/architecture/ExceptionLayeringArchitectureTest.java`
    - `sandwish-front-api/src/test/java/com/github/thundax/architecture/ExceptionLayeringArchitectureTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/architecture/ExceptionLayeringArchitectureTest.java`
    - `sandwish-common/sandwish-common-web/src/test/java/com/github/thundax/common/web/response/ApiResponseTest.java`
    - `sandwish-common/sandwish-common-web/src/test/java/com/github/thundax/common/web/exception/GlobalExceptionHandlerTest.java`
  - 处理动作：增加 ArchUnit 或契约测试，约束异常归属、`@BizExceptionBoundary` 标注范围、ApiException 禁用和 API code 格式。
  - 验收点：违反异常分层、非 getter Service 缺少边界注解、ApiException 残留或 code 格式错误时测试失败。
  - 重要度：10/10

- [ ] `exception-governance-cleanup`：沉淀治理规则并清理 RUNBOOK
  - 范围文件：
    - `docs/00-governance/ARCHITECTURE.md`
    - `docs/00-governance/API-ANNOTATION-MATRIX.md`
    - `docs/00-governance/TODO-RULES.md`
    - `docs/AGENT.md`
    - `docs/30-designs/RUNBOOK-EXCEPTION-LAYERING.md`
    - `TODO.md`
  - 处理动作：将稳定异常分层和 API error code 规则沉淀到治理文档，更新 AI 路由，删除 RUNBOOK 并删除或收窄已完成 TODO。
  - 验收点：长期规则不依赖 RUNBOOK，`RUNBOOK-EXCEPTION-LAYERING.md` 已清理，`TODO.md` 只保留未完成任务。
  - 重要度：9/10

## 待讨论项
