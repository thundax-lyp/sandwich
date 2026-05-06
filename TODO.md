# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `admin-api-assist-response-wrapper`：迁移辅助签名接口响应包装
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/SignatureController.java
    sandwish-admin-api/src/test/java/com/github/thundax/modules/assist/controller/SignatureControllerContractTest.java
  - 处理动作：将纯 JSON Controller 迁移到 `@WrappedApiController` 并补真实响应结构测试
  - 验收点：`SignatureController` 成功 JSON 响应由 `ApiResponseBodyAdvice` 包装，`PageResponse` 不被二次包装
  - 重要度：7/10

- [ ] `admin-api-auth-response-wrapper`：迁移后台认证 JSON 接口响应包装
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java
    sandwish-admin-api/src/test/java/com/github/thundax/modules/auth/controller/AuthControllerContractTest.java
    sandwish-admin-web/src/api/authApi.ts
    sandwish-admin-web/src/App.test.tsx
  - 处理动作：将 `AuthController` 迁移到 `@WrappedApiController` 并确认前端继续读取 `{code,message,data}` 响应
  - 验收点：登录、登出、token JSON 接口保持统一响应结构，前端登录和登出测试通过
  - 重要度：8/10

- [ ] `admin-api-response-wrapper-filter`：移除后台响应包装过滤器
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/filter/ResponseWrapperFilter.java
    sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java
    sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/SandwishProperties.java
    sandwish-admin-api/src/main/resources/config/application.yml
  - 处理动作：删除 `ResponseWrapperFilter` 及其配置入口，保留 `@WrappedApiController` 响应包装路径
  - 验收点：后台 JSON 响应不依赖 servlet filter 字节流包装，图片和文件流响应不被包装
  - 重要度：8/10

## 待审阅任务项

## 待讨论项
