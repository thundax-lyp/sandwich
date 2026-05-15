# RUNBOOK OPEN API MODULE

## 1. Purpose

本文档定义创建 `sandwish-open-api` 工程和首批开放接口能力的执行顺序。

本 RUNBOOK 是一次性执行手册，不是长期治理规则。执行完成并通过验证后，必须清理本文档和对应 `TODO.md` 任务项。

## 2. Scope

当前覆盖范围：

- 新增 `sandwish-open-api` Maven 入口模块。
- 新增 OpenClient 业务模型、持久化和后台管理最小能力。
- 复用 `auth_principal_identity` 和 `auth_principal_credential` 支撑 API KEY / API SECRET。
- 实现 Open API HMAC 签名认证、防重放、权限校验和上下文注入。
- 实现 Open API Submission 创建和图片上传接口。
- 补齐 Open API 错误码、i18n、接口契约测试和架构测试。

当前不覆盖范围：

- 第三方开发者门户。
- SDK、示例工程或 Postman 集合。
- OAuth2 client credentials。
- 非对称签名。
- Open API 独立调用日志表。
- 除 Submission 以外的业务模块开放。

## 3. Fixed Decisions

执行过程固定遵守：

- `sandwish-open-api` 与 `sandwish-admin-api`、`sandwish-front-api` 并列。
- `sandwish-open-api` 不依赖 `sandwish-admin-api` 或 `sandwish-front-api`。
- Open API 入口模块固定负责 header 读取、签名校验、nonce 防重放、权限校验、异常响应和上下文注入。
- `OpenClient` 主数据归属 `sandwish-biz/src/main/java/com/github/thundax/modules/open`。
- `open_client` 和 `open_client_permission` 持久化归属 `sandwish-infra/src/main/java/com/github/thundax/modules/open`。
- API KEY 固定使用 `auth_principal_identity`。
- API SECRET 固定使用 `auth_principal_credential`，可验证材料使用加密密文。
- `open_client.ipWhitelist` 固定使用 JSON array 字符串，空值或空数组表示不限 IP。
- OpenClient 权限固定使用 `open_client_permission`，不复用 `sys_menu` / `sys_role`。
- 首批 OpenClient 后台权限固定为 `open:client:view` 和 `open:client:edit`。
- Open API 首批业务权限固定为 `submission:submission:create` 和 `submission:submission:image:upload`。
- Submission 写操作审计继续走 `SubmissionService` 内置 Audit，Open API 只负责注入 `OPEN_CLIENT` 上下文。
- 首批不增加 Open API 独立调用日志表。

## 4. Execution Order

### 4.1 Bootstrap `sandwish-open-api`

目标：建立可编译、可启动、可测试的 Open API 入口模块空工程。

动作：

- 在根 `pom.xml` 增加 `sandwish-open-api` module。
- 新增 `sandwish-open-api/pom.xml`，依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-common-web`、`sandwish-common-security`、`sandwish-common-swagger`、`sandwish-common-cache` 和 `sandwish-common-test`。
- 新增 `OpenApiApplication`。
- 新增 `application.yml`，默认端口固定为 `20010`，context path 固定为 `/open-api`，`spring.application.name` 固定为 `sandwish-open-api`。
- 新增 `logback-spring.xml`、i18n messages 和最小自动配置。
- 增加占位测试，避免新模块无匹配测试反复失败。

验收：

```text
mvn -pl sandwish-open-api -am test
```

### 4.2 Build OpenClient Biz Model

目标：在业务层建立 OpenClient 主体、权限、状态和服务契约。

动作：

- 新增 `OpenClient`、`OpenClientPermission`、`OpenClientStatus`。
- 新增 `OpenClientId`、`OpenClientIdCodec`、`OpenClientPermissionId`、`OpenClientPermissionIdCodec`。
- 新增 OpenClient DAO interface。
- 新增 OpenClient Service、Command、Query 和 DTO。
- 扩展 auth 主体枚举：`PrincipalType.OPEN_CLIENT`。
- 扩展 auth 标识和凭据枚举：`PrincipalIdentityType.API_KEY`、`PrincipalCredentialType.API_SECRET`。
- 扩展 Audit operator 映射：`SandwishSubjectType.OPEN_CLIENT -> AuditOperatorType.OPEN_CLIENT`。

验收：

```text
mvn -pl sandwish-biz -am test
```

### 4.3 Add Database And Infra Persistence

目标：落 `open_client` / `open_client_permission` 数据库设计、SQL 和持久化实现。

动作：

- 新增 `docs/20-database/OPEN-API-DATABASE-DESIGN.md`。
- 新增 `db/schema/open.sql`。
- 新增 `db/data/open.sql`。
- 在 `db/AGENT.md` 增加 open-api 数据库路由。
- 在 `docs/AGENT.md` 增加 open-api 数据库文档路由。
- 新增 OpenClient DO、Mapper、PersistenceAssembler 和 DAO implementation。
- 保证 `open_client_permission.client_id + permission` 唯一。
- 保证 `open_client.ip_whitelist` 可保存 JSON array 字符串。

验收：

```text
mvn -pl sandwish-infra -am test
```

### 4.4 Add Admin OpenClient Management

目标：后台具备创建、查看、启停、重置 secret 和维护权限的最小入口。

动作：

- 在 `sandwish-admin-api` 新增 `modules/open` Controller、Request、Response 和 `InterfaceAssembler`。
- 后台 OpenClient 创建成功时返回 API KEY 和 API SECRET 明文一次。
- 重置 API SECRET 时返回新 API SECRET 明文一次。
- 查询详情和分页不得返回 API SECRET 明文。
- 后台权限使用 `open:client:view` 和 `open:client:edit`。
- 在 `db/data/system.sql` 增加 OpenClient 后台菜单和权限。
- 增加后台 Controller contract test。

验收：

```text
mvn -pl sandwish-admin-api -am test
```

### 4.5 Add Open API Auth Foundation

目标：完成 Open API 独立认证、授权、错误响应和上下文注入。

动作：

- 在 `sandwish-open-api` 新增 Open API 专属 `ExceptionTranslator` 和响应异常工厂。
- 在 i18n messages 中补齐 `OPEN-API-ERROR-CODE-DESIGN.md` 内的 message key。
- 新增 API KEY header 读取、timestamp 校验、nonce 校验、body hash 校验和 HMAC-SHA256 签名校验。
- nonce 固定使用 Redis key `open-api:nonce:{apiKey}:{nonce}`，TTL 等于或略大于 5 minutes。
- 新增 IP 白名单校验，支持单 IP 和 CIDR。
- 新增 OpenClient permission checker。
- 认证成功后注入 `SandwishContextHolder`，主体类型固定为 `OPEN_CLIENT`。
- 任一认证失败不得进入业务 Controller。

验收：

```text
mvn -pl sandwish-open-api -am test
```

### 4.6 Add Open API Submission Endpoints

目标：开放第三方 Submission 创建和图片上传。

动作：

- 在 `sandwish-open-api` 新增 `modules/submission` Controller、Request、Response 和 `InterfaceAssembler`。
- 创建接口固定为 `POST /api/submission/submission/create`，权限为 `submission:submission:create`。
- 图片上传接口固定为 `POST /api/submission/submission/image/upload`，权限为 `submission:submission:image:upload`。
- Open API Submission Controller 固定调用 `SubmissionService`，不得直接访问 DAO、Mapper、Storage store 或底层对象存储。
- Open API 图片上传入口只负责接收 multipart，并把文件名、content type、size 和 `InputStream` 转交给上传 helper。
- 上传 helper 固定设计为 stream-based 接口，不依赖 `HttpServletRequest` 或 `MultipartHttpServletRequest`。
- 上传 helper 不得依赖 `sandwish-admin-api`。
- 图片上传核心固定复用 `StorageService` 和 `StoredObjectStore`，上传对象 `ownerType=SUBMISSION`。
- 创建 Submission 时通过 `imageObjectIds` 建立业务引用，Audit 操作者由 OpenClient 上下文提供。

验收：

```text
mvn -pl sandwish-open-api -am test
```

### 4.7 Add Architecture And Contract Tests

目标：让新入口模块被现有工程门禁覆盖。

动作：

- 为 `sandwish-open-api` 补齐 API surface、request/response 注解、InterfaceAssembler、ExceptionLayering、ServletRegistration 和 Service API 边界测试。
- 补齐 Open API auth filter / signature verifier / nonce / permission checker 单元测试。
- 补齐 Submission 创建和图片上传 Controller contract test。
- 补齐 OpenClient Service 和 infra DAO 关键测试。

验收：

```text
mvn -pl sandwish-open-api -am test
mvn test
```

### 4.8 Final Cleanup

目标：完成执行现场收口。

动作：

- 删除或收窄已完成 `TODO.md` 任务。
- 删除本文档。
- 确认 `docs/10-requirements/OPEN-API-REQUIREMENTS.md`、`docs/30-designs/OPEN-API-AUTH-DESIGN.md`、`docs/30-designs/OPEN-API-ERROR-CODE-DESIGN.md` 和数据库文档与代码一致。
- 确认 `docs/60-human/DOCUMENT-ROUTING-MAP.md` 不需要同步。
- 确认工作区没有无关修改。

验收：

```text
git status --short
```

## 5. Verification

阶段性验证命令：

```text
mvn -pl sandwish-open-api -am test
mvn -pl sandwish-biz -am test
mvn -pl sandwish-infra -am test
mvn -pl sandwish-admin-api -am test
```

最终验证命令：

```text
mvn test
```

如果完整 `mvn test` 受本地环境限制无法执行，必须至少完成受影响模块测试，并在最终说明中写明未执行完整测试的原因。

## 6. Open Items

无
