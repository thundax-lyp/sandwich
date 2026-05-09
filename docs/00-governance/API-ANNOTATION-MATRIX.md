# API Annotation Matrix

## 1. Purpose

本文档定义 Sandwich 前后台 HTTP API 入口、API 请求模型和 API 响应模型的注解规范。

目标是让接口代码可被 AI 稳定识别、可被架构测试逐步门禁，并避免 Controller、Request、Response 的注解口径继续漂移。

## 2. Scope

当前范围：

- `sandwish-admin-api` 中 `com.github.thundax.modules..controller..` 的后台 REST API 入口
- `sandwish-front-api` 中 `com.github.thundax.modules..controller..` 的前台 REST API 入口
- `controller/request` 包中的 API 请求模型
- `controller/response` 包中的 API 响应模型
- `PageResponse<T>` 等通用分页响应模型

不在范围内：

- 不定义 Service、DAO、Mapper 和 `DO/DataObject` 注解规则
- 不定义数据库字段、索引和 MyBatis 映射规则
- 不引入 OpenAPI 3 注解体系

## 3. Selector

- `ADMIN_REST_CONTROLLER_SELECTOR`：`sandwish-admin-api` 中位于 `..modules..controller..` 且声明 `@RestController` 或 `@WrappedApiController` 的类
- `FRONT_REST_CONTROLLER_SELECTOR`：`sandwish-front-api` 中位于 `..modules..controller..` 且声明 `@RestController` 或 `@WrappedApiController` 的类
- `REQUEST_MODEL_SELECTOR`：类名以 `Request` 结尾，且位于 `..controller.request..`
- `RESPONSE_MODEL_SELECTOR`：类名以 `Response` 结尾，且位于 `..controller.response..`

## 4. Exception Buckets

公开入口例外必须使用显式注解表达。

例外口径固定写入对应规则：

- 认证公开入口：登录、登出、验证码、登录令牌刷新，必须声明 `@PublicApi`
- 文件流入口：头像、存储文件、验证码图片等直接写入 `HttpServletResponse` 的接口

`@PublicApi` 只表达接口对权限矩阵公开，不替代 Spring Security URL 放行配置，也不替代业务校验。

需要新增长期例外时，先更新本文档，再补对应 ArchUnit 或人工审阅规则。不得再使用 `@ApiOperation(notes = "ignore")` 表达权限例外。

## 5. Rule Format

每条规则固定字段：

1. `Rule ID`
2. `Scope`
3. `Constraint`
4. `Detection`
5. `Violation Message`

固定报错格式：

`[RuleID] <scope> violates <constraint>: <found>`

## 6. Hard Rules

当前 Swagger 基线是 Springfox 2.x / Swagger 2 注解体系，Controller 分组继续使用 `@Api(tags = "...")`。`@Tag` 属于 OpenAPI 3 注解体系，只有完成 Swagger 技术栈升级后才能统一切换。新增或修改 Controller 时，`@Api.tags` 应使用稳定业务分组名，例如 `系统/当前用户`，不得使用数字排序前缀。

| Rule ID | Scope | Constraint | Detection | Violation Message |
| --- | --- | --- | --- | --- |
| `ANNO_REST_CLASS_BASE_REQUIRED` | `ADMIN_REST_CONTROLLER_SELECTOR` + `FRONT_REST_CONTROLLER_SELECTOR` | REST API 入口类必须声明 `@RestController` 或 `@WrappedApiController`，并声明类级 `@RequestMapping`；类级路径固定使用 `/api/{domain}/{resource}` | ArchUnit / review | `[ANNO_REST_CLASS_BASE_REQUIRED] <class> violates class base annotations required: <missingAnnotations>` |
| `ANNO_REST_CLASS_SWAGGER_REQUIRED` | `ADMIN_REST_CONTROLLER_SELECTOR` + `FRONT_REST_CONTROLLER_SELECTOR` | REST API 入口类必须声明 `@Api` | ArchUnit / review | `[ANNO_REST_CLASS_SWAGGER_REQUIRED] <class> violates Api annotation required: <foundAnnotations>` |
| `ANNO_REST_CLASS_API_TAG_BUSINESS_NAME` | `ADMIN_REST_CONTROLLER_SELECTOR` + `FRONT_REST_CONTROLLER_SELECTOR` | `@Api.tags` 必须使用稳定业务分组名，不得使用数字排序前缀 | ArchUnit / review | `[ANNO_REST_CLASS_API_TAG_BUSINESS_NAME] <class> violates Api tag business name required: <tag>` |
| `ANNO_REST_METHOD_MAPPING_REQUIRED` | REST API 入口类中的公开 HTTP 方法 | 必须且仅能有一个方法级 HTTP 映射注解：JSON 请求固定使用 `@PostMapping`，非 JSON 读取固定使用 `@GetMapping`；方法级不得使用 `@RequestMapping`、`@PutMapping`、`@DeleteMapping` 或 `@PatchMapping` | ArchUnit / review | `[ANNO_REST_METHOD_MAPPING_REQUIRED] <class#method> violates method mapping required: <foundMappings>` |
| `ANNO_REST_METHOD_SWAGGER_REQUIRED` | REST API 入口类中的公开 HTTP 方法 | 必须声明 `@ApiOperation`；认证公开入口和文件流入口也必须声明 | ArchUnit / review | `[ANNO_REST_METHOD_SWAGGER_REQUIRED] <class#method> violates ApiOperation required: <foundAnnotations>` |
| `ANNO_REST_METHOD_ACCESS_MARK_REQUIRED` | 声明 `@ApiOperation` 的 REST API 方法 | 必须由方法级或类级 `@HasPermission` / `@PublicApi` 表达访问口径；公开认证入口使用 `@PublicApi` | ArchUnit / review | `[ANNO_REST_METHOD_ACCESS_MARK_REQUIRED] <class#method> violates access annotation required: <foundAnnotations>` |
| `ANNO_REST_METHOD_JSON_POST_REQUIRED` | REST API 方法中使用 `@RequestBody` 或返回 JSON 包装的接口 | 固定使用 `@PostMapping` | ArchUnit / review | `[ANNO_REST_METHOD_JSON_POST_REQUIRED] <class#method> violates json post required: <foundMapping>` |
| `ANNO_REST_METHOD_GET_NON_JSON_REQUIRED` | REST API 方法中使用 `@GetMapping` 的接口 | 仅用于验证码、头像、文件内容等非 JSON 响应，方法返回类型固定为 `void` | ArchUnit / review | `[ANNO_REST_METHOD_GET_NON_JSON_REQUIRED] <class#method> violates get non-json required: <returnType>` |
| `ANNO_REQUEST_BODY_VALID_REQUIRED` | REST API 方法中使用 `@RequestBody` 的 `*Request` 参数 | 必须同时声明 `@Valid` | ArchUnit / review | `[ANNO_REQUEST_BODY_VALID_REQUIRED] <class#method> violates request body Valid required: <parameterType>` |
| `ANNO_REQUEST_MODEL_CLASS_REQUIRED` | `REQUEST_MODEL_SELECTOR` | 类级注解固定且仅允许 `@Getter`、`@Setter`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)` | ArchUnit | `[ANNO_REQUEST_MODEL_CLASS_REQUIRED] <class> violates request class annotations required: <foundAnnotations>` |
| `ANNO_RESPONSE_MODEL_CLASS_REQUIRED` | `RESPONSE_MODEL_SELECTOR` | 类级注解固定且仅允许 `@Getter`、`@Builder`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)` | ArchUnit | `[ANNO_RESPONSE_MODEL_CLASS_REQUIRED] <class> violates response class annotations required: <foundAnnotations>` |
| `ANNO_CONTROLLER_RESPONSE_ASSEMBLER_REQUIRED` | `ADMIN_REST_CONTROLLER_SELECTOR` + `FRONT_REST_CONTROLLER_SELECTOR` | Controller 固定通过对应 `*InterfaceAssembler` 或 `PageResponseHelper` 获取完整 `*Response` / `PageResponse`；Controller 不直接创建业务 `*Response` | ArchUnit / review | `[ANNO_CONTROLLER_RESPONSE_ASSEMBLER_REQUIRED] <class> violates response assembler required: <responseType>` |
| `ANNO_MODEL_FIELD_DESCRIPTION_REVIEW` | API Request / Response 字段 | 对外字段应声明 `@ApiModelProperty` 和稳定 JSON 字段名；当前作为人工审阅规则，不作为硬门禁 | review | `[ANNO_MODEL_FIELD_DESCRIPTION_REVIEW] <field> violates field description review: <foundAnnotations>` |

## 7. Minimal Matrix

| Interface Type | Required | Forbidden |
| --- | --- | --- |
| Admin REST Controller | `@RestController` 或 `@WrappedApiController` + `@RequestMapping @Api`；类级路径固定使用 `/api/{domain}/{resource}`；方法级 JSON API 固定使用 `@PostMapping`；非 JSON 读取固定使用 `@GetMapping` 且返回 `void`；方法级声明 `@ApiOperation`；`@RequestBody *Request` 参数声明 `@Valid`；业务 `*Response` 由对应 `*InterfaceAssembler` 创建 | 直接依赖 DAO / Mapper / `DO/DataObject` / `PersistenceAssembler`；方法级 `@RequestMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping`；直接 `new *Response()` |
| Front REST Controller | `@RestController` 或 `@WrappedApiController` + `@RequestMapping @Api`；类级路径固定使用 `/api/{domain}/{resource}`；方法级 JSON API 固定使用 `@PostMapping`；非 JSON 读取固定使用 `@GetMapping` 且返回 `void`；方法级声明 `@ApiOperation`；`@RequestBody *Request` 参数声明 `@Valid`；公开入口声明 `@PublicApi`；业务 `*Response` 由对应 `*InterfaceAssembler` 创建 | 直接依赖 DAO / Mapper / `DO/DataObject` / `PersistenceAssembler`；方法级 `@RequestMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping`；直接 `new *Response()` |
| Request Model | `@Getter @Setter @ApiModel @JsonInclude(JsonInclude.Include.NON_NULL) @JsonIgnoreProperties(ignoreUnknown = true)` | 业务流程、Service/DAO 依赖、`DO/DataObject` 字段 |
| Response Model | `@Getter @Builder @ApiModel @JsonInclude(JsonInclude.Include.NON_NULL) @JsonIgnoreProperties(ignoreUnknown = true)` | 业务流程、Service/DAO 依赖、`DO/DataObject` 字段 |

混合 REST Controller 可以保留 `@RestController`，并在需要统一响应包装的 JSON 方法上声明 `@WrappedApiResponse`。直接写入 `HttpServletResponse` 的图片、文件或头像方法不得声明 `@WrappedApiResponse`。

`@ApiOperation.notes` 是可选业务补充说明，不承载访问控制规则。API 访问口径固定以 `@HasPermission` 或 `@PublicApi` 为准。

## 8. CI Gate

- 已有 ArchUnit 门禁继续覆盖 Request / Response 类级注解。
- REST Controller 直接创建业务 `*Response` 的行为纳入门禁；业务响应创建固定收敛到对应 `*InterfaceAssembler`，分页响应创建固定使用 `PageResponseHelper`。
- 已有 RestController 架构测试继续约束 REST Controller 不回流手写 `Validator`。
- 前后台 REST Controller 已纳入类级 `/api/{domain}/{resource}`、`@Api`、`@ApiOperation`、HTTP mapping 唯一性、JSON POST、GET 非 JSON、访问口径标记和 `@RequestBody *Request` 参数 `@Valid` 门禁。
- 新增或修改 Controller、Request、Response 时，必须按本文档人工审阅。

## 9. Open Items

无
