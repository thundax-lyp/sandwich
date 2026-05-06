# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
