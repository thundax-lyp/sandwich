# HOW TO MIGRATE API RESPONSE WRAPPER

## 1. Purpose

本文档定义后台 API 从 `ResponseWrapperFilter` 迁移到 `@WrappedApiController` 与 `ApiResponseBodyAdvice` 的最小闭环步骤。

目标是让 API 响应包装从字节流层隐式改写，迁移为 Spring MVC 类型层的显式入口约定。

## 2. Scope

当前范围：

- `sandwish-admin-api` 的 JSON REST Controller。
- `sandwish-common-web` 中已有的 `@WrappedApiController`、`ApiResponseBodyAdvice` 和 `ApiResponse`。
- `sandwish-admin-api` 中历史 `ResponseWrapperFilter` 的撤销路径。
- 前端调用方对 `{code,message,data}` 响应结构的确认。

不在范围内：

- 验证码图片、文件下载、头像、存储对象内容等直接写入 `HttpServletResponse` 的流式入口。
- `PageResponse<T>` 的结构重做。
- 业务异常模型重做。
- 前台 `sandwish-front-api` 的响应包装策略调整。

## 3. Bounded Context

当前后台响应包装存在两套机制：

- `ResponseWrapperFilter`：在 Servlet Filter 字节流层包装 `/api/*` JSON 响应。
- `ApiResponseBodyAdvice`：在 Spring MVC 返回值处理层包装 `@WrappedApiController` 返回值。

迁移目标固定为：JSON Controller 入口通过 `@WrappedApiController` 显式进入 `ApiResponseBodyAdvice`，Controller 可以继续返回业务 Response DTO；异常继续由 `GlobalExceptionHandler` 返回 `ApiResponse.failure(...)`。

`ResponseWrapperFilter` 固定作为待撤销历史机制，不再扩大使用范围。

## 4. When To Use

- 迁移一个后台 JSON Controller 到显式统一响应包装。
- 判断一个接口是否可以从 `ResponseWrapperFilter` 迁出。
- 删除 `ResponseWrapperFilter` 前做最后收口检查。
- 修复前端对 API 响应层级的误判。

## 5. Do Not Use For

- 新增文件流接口。
- 修改验证码图片接口。
- 修改静态资源映射。
- 改造数据库、DAO、Service 或业务流程。

## 6. Pre-Checks

迁移前固定检查：

1. Controller 是否只返回 JSON 响应。
2. Controller 是否直接写入 `HttpServletResponse`。
3. Controller 返回值是否已经是 `ApiResponse` 或 `PageResponse`。
4. 前端或测试是否断言响应 JSON 顶层结构。
5. 该 Controller 是否存在公开入口，公开入口是否已经声明 `@PublicApi`。

直接写入 `HttpServletResponse` 的方法固定不纳入本轮迁移。

## 7. Steps

### 7.1 单 Controller 迁移

1. 选择一个低风险 JSON Controller。
2. 将类级 `@RestController` 替换为 `@WrappedApiController`。
3. 保留类级 `@RequestMapping`、`@Api`、`@HasPermission` 或 `@PublicApi`。
4. 保持方法返回 DTO、`Boolean` 或 `PageResponse<T>` 的现有签名。
5. 不在每个方法中手写 `ApiResponse.success(...)`，除非该方法需要表达业务失败响应。
6. 增加或更新 Controller contract test，断言真实 HTTP 响应为 `{code,message,data}`。
7. 确认 `PageResponse<T>` 不被二次包装。

### 7.2 认证接口迁移

1. 迁移 `AuthController` 中 JSON 登录、登出、token 和 OAuth2 接口。
2. 保留 `@PublicApi`。
3. 保留 `/api/auth/captcha` 图片接口在 `CaptchaController` 中的原始响应方式。
4. 更新前端登录调用测试，确认前端继续读取 `data.token`。
5. 确认 `/api/auth/captcha` 返回 `image/jpeg`，不进入 JSON 包装。

### 7.3 撤销 Filter

1. 确认后台 JSON Controller 已完成 `@WrappedApiController` 迁移。
2. 删除 `ResponseWrapperFilter`。
3. 删除 `SandwishProperties.ResponseWrapperFilterProperties`。
4. 删除 `WebMvcConfiguration#responseWrapperFilter` 注册。
5. 删除 `sandwish.response-wrapper-filter` 配置项。
6. 更新测试，确保没有 `/api/*` 字节流包装依赖。

## 8. Files To Touch

通常涉及：

- `sandwish-admin-api/src/main/java/com/github/thundax/modules/**/controller/*Controller.java`
- `sandwish-admin-api/src/test/java/com/github/thundax/modules/**/controller/*ContractTest.java`
- `sandwish-admin-web/src/api/**`
- `sandwish-admin-web/src/**/*.test.tsx`
- `sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java`
- `sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/SandwishProperties.java`
- `sandwish-admin-api/src/main/resources/config/application.yml`

通常不触碰：

- Service、DAO、Mapper 和数据库脚本。
- `sandwish-front-api`。
- 文件流 Controller 的响应体写入逻辑。

## 9. Common Mistakes

- 同时保留 `ResponseWrapperFilter` 和 `@WrappedApiController`，导致同一响应存在双重包装风险。
- 把 `PageResponse<T>` 包进 `ApiResponse.data`，改变分页调用方协议。
- 把验证码图片、文件下载、存储内容接口标为 `@WrappedApiController` 后影响二进制响应。
- 在每个 Controller 方法中机械返回 `ApiResponse.success(...)`，让入口层噪音过大。
- 只改后端，不更新前端对 `data` 层级的测试。

## 10. Verification

最小验证：

- 迁移单个 Controller 时运行对应 Controller contract test。
- 迁移认证接口时运行后台 auth 相关测试和前端登录测试。
- 删除 `ResponseWrapperFilter` 时运行：
  - `mvn -q -pl sandwish-admin-api -am test`
  - `npm run test`，工作目录为 `sandwish-admin-web`

人工检查：

- JSON 成功响应顶层固定包含 `code`、`message`、`data`。
- JSON 失败响应由 `GlobalExceptionHandler` 返回 `ApiResponse.failure(...)`。
- 图片和文件流响应不包含 `code`、`message`、`data`。

## 11. Commit Guidance

提交粒度固定小步：

- 一个 Controller 迁移和对应测试放在同一个 commit。
- 认证接口迁移和前端登录调用测试放在同一个 commit。
- 删除 `ResponseWrapperFilter`、配置属性和最终测试收口放在同一个 commit。

提交信息示例：

- `Refactor(api): 迁移辅助接口响应包装`
- `Refactor(auth): 迁移认证接口响应包装`
- `Refactor(admin): 移除响应包装过滤器`

## 12. Open Items

无
