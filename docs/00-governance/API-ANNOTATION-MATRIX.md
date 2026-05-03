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
- 不要求历史服务端页面入口继续扩展
- 不引入 OpenAPI 3 注解体系

## 3. Selector

- `ADMIN_REST_CONTROLLER_SELECTOR`：`sandwish-admin-api` 中位于 `..modules..controller..` 且声明 `@RestController` 的类
- `FRONT_REST_CONTROLLER_SELECTOR`：`sandwish-front-api` 中位于 `..modules..controller..` 且声明 `@RestController` 的类
- `LEGACY_PAGE_CONTROLLER_SELECTOR`：声明 `@Controller` 但不声明 `@RestController` 的历史页面入口
- `REQUEST_MODEL_SELECTOR`：类名以 `Request` 结尾，且位于 `..controller.request..`
- `RESPONSE_MODEL_SELECTOR`：类名以 `Response` 结尾，且位于 `..controller.response..`

## 4. Exception Buckets

当前不新增异常注解。

例外口径固定写入对应规则：

- 认证公开入口：登录、登出、验证码、登录令牌刷新
- 文件流入口：头像、存储文件、验证码图片等直接写入 `HttpServletResponse` 的接口
- 历史页面入口：只允许维护，不允许新增业务能力

需要新增长期例外时，先更新本文档，再补对应 ArchUnit 或人工审阅规则。

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

| Rule ID | Scope | Constraint | Detection | Violation Message |
| --- | --- | --- | --- | --- |
| `ANNO_REST_CLASS_BASE_REQUIRED` | `ADMIN_REST_CONTROLLER_SELECTOR` + `FRONT_REST_CONTROLLER_SELECTOR` | REST API 入口类必须声明 `@RestController` 和类级 `@RequestMapping` | ArchUnit / review | `[ANNO_REST_CLASS_BASE_REQUIRED] <class> violates class base annotations required: <missingAnnotations>` |
| `ANNO_REST_CLASS_SWAGGER_REQUIRED` | `ADMIN_REST_CONTROLLER_SELECTOR` | 后台 REST API 入口类必须声明 `@Api` | review，存量收敛后升级 ArchUnit | `[ANNO_REST_CLASS_SWAGGER_REQUIRED] <class> violates Api annotation required: <foundAnnotations>` |
| `ANNO_REST_METHOD_MAPPING_REQUIRED` | REST API 入口类中的公开 HTTP 方法 | 必须且仅能有一个 HTTP 映射注解：`@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping` | ArchUnit / review | `[ANNO_REST_METHOD_MAPPING_REQUIRED] <class#method> violates method mapping required: <foundMappings>` |
| `ANNO_REST_METHOD_SWAGGER_REQUIRED` | 后台 REST API 入口类中的公开 HTTP 方法 | 必须声明 `@ApiOperation`；认证公开入口和文件流入口也必须声明，`notes` 可使用 `ignore` 或 `user` 表达权限例外 | review，存量收敛后升级 ArchUnit | `[ANNO_REST_METHOD_SWAGGER_REQUIRED] <class#method> violates ApiOperation required: <foundAnnotations>` |
| `ANNO_REQUEST_BODY_VALID_REQUIRED` | REST API 方法中使用 `@RequestBody` 的 `*Request` 参数 | 必须同时声明 `@Valid` | ArchUnit / review | `[ANNO_REQUEST_BODY_VALID_REQUIRED] <class#method> violates request body Valid required: <parameterType>` |
| `ANNO_REQUEST_MODEL_CLASS_REQUIRED` | `REQUEST_MODEL_SELECTOR` | 类级注解固定且仅允许 `@Getter`、`@Setter`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)` | ArchUnit | `[ANNO_REQUEST_MODEL_CLASS_REQUIRED] <class> violates request class annotations required: <foundAnnotations>` |
| `ANNO_RESPONSE_MODEL_CLASS_REQUIRED` | `RESPONSE_MODEL_SELECTOR` | 类级注解固定且仅允许 `@Getter`、`@Setter`、`@ApiModel`、`@JsonInclude(JsonInclude.Include.NON_NULL)`、`@JsonIgnoreProperties(ignoreUnknown = true)` | ArchUnit | `[ANNO_RESPONSE_MODEL_CLASS_REQUIRED] <class> violates response class annotations required: <foundAnnotations>` |
| `ANNO_MODEL_FIELD_DESCRIPTION_REVIEW` | API Request / Response 字段 | 对外字段应声明 `@ApiModelProperty` 和稳定 JSON 字段名；当前作为人工审阅规则，不作为硬门禁 | review | `[ANNO_MODEL_FIELD_DESCRIPTION_REVIEW] <field> violates field description review: <foundAnnotations>` |
| `ANNO_LEGACY_PAGE_NO_NEW_BUSINESS` | `LEGACY_PAGE_CONTROLLER_SELECTOR` | 历史页面入口只允许维护既有静态支撑能力，不得新增核心业务规则 | review | `[ANNO_LEGACY_PAGE_NO_NEW_BUSINESS] <class> violates legacy page boundary: <foundUsage>` |

## 7. Minimal Matrix

| Interface Type | Required | Forbidden |
| --- | --- | --- |
| Admin REST Controller | `@RestController @RequestMapping @Api`；方法级 HTTP Mapping + `@ApiOperation`；`@RequestBody *Request` 参数声明 `@Valid` | 直接依赖 DAO / Mapper / `DO/DataObject` / `PersistenceAssembler` |
| Front REST Controller | `@RestController @RequestMapping`；方法级 HTTP Mapping；`@RequestBody *Request` 参数声明 `@Valid` | 直接依赖 DAO / Mapper / `DO/DataObject` / `PersistenceAssembler` |
| Request Model | `@Getter @Setter @ApiModel @JsonInclude(JsonInclude.Include.NON_NULL) @JsonIgnoreProperties(ignoreUnknown = true)` | 业务流程、Service/DAO 依赖、`DO/DataObject` 字段 |
| Response Model | `@Getter @Setter @ApiModel @JsonInclude(JsonInclude.Include.NON_NULL) @JsonIgnoreProperties(ignoreUnknown = true)` | 业务流程、Service/DAO 依赖、`DO/DataObject` 字段 |
| Legacy Page Controller | 仅维护既有页面或静态支撑入口 | 新增核心业务规则、新增服务端页面能力 |

## 8. CI Gate

- 已有 ArchUnit 门禁继续覆盖 Request / Response 类级注解。
- 已有 RestController 架构测试继续约束 REST Controller 不回流手写 `Validator`。
- 新增或修改 Controller、Request、Response 时，必须按本文档人工审阅。
- 存量接口收敛后，再把 `@Api`、`@ApiOperation`、HTTP mapping 唯一性和 `@Valid` 规则逐步升级为 ArchUnit 门禁。

## 9. Open Items

- 是否把前台 REST Controller 也固定要求 `@Api` 和 `@ApiOperation`。
- 是否为认证公开入口新增专用例外注解，替代 `notes = "ignore"` 的历史口径。
