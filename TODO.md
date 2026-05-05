# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `common-web`：增加可标记启用的统一响应包装 Advice
  - 范围文件：
    - `sandwish-common/sandwish-common-web/src/main/java/com/github/thundax/common/web/annotation/WrappedApiController.java`
    - `sandwish-common/sandwish-common-web/src/main/java/com/github/thundax/common/web/advice/ApiResponseBodyAdvice.java`
    - `sandwish-common/sandwish-common-web/src/test/java/com/github/thundax/common/web/advice/ApiResponseBodyAdviceTest.java`
    - `sandwish-common/sandwish-common-web/pom.xml`
  - 处理动作：新增只对标记 Controller 生效的 `ResponseBodyAdvice`，统一包装普通 API 返回值。
  - 验收点：标记 Controller 的普通对象会包装为 `ApiResponse`，`ApiResponse`、`String` 和未标记 Controller 会跳过包装，并有单元测试覆盖。
  - 重要度：10/10

- [ ] `common-security`：增加声明式权限注解
  - 范围文件：
    - `sandwish-common/sandwish-common-security/src/main/java/com/github/thundax/common/security/annotation/HasPermission.java`
    - `sandwish-common/sandwish-common-security/src/test/java/com/github/thundax/common/security/annotation/HasPermissionTest.java`
  - 处理动作：新增用于 Controller 或方法声明权限要求的 `@HasPermission` 注解。
  - 验收点：注解能标记类型和方法，并能在运行期读取权限值。
  - 重要度：8/10

- [ ] `common-security`：增加 Spring Security 当前用户解析器
  - 范围文件：
    - `sandwish-common/sandwish-common-security/src/main/java/com/github/thundax/common/security/user/CurrentUserResolver.java`
    - `sandwish-common/sandwish-common-security/src/main/java/com/github/thundax/common/security/user/SecurityContextCurrentUserResolver.java`
    - `sandwish-common/sandwish-common-security/src/main/java/com/github/thundax/common/security/user/SpringSecurityCurrentUserProvider.java`
    - `sandwish-common/sandwish-common-security/src/test/java/com/github/thundax/common/security/user/SecurityContextCurrentUserResolverTest.java`
    - `sandwish-common/sandwish-common-security/src/test/java/com/github/thundax/common/security/user/SpringSecurityCurrentUserProviderTest.java`
  - 处理动作：将 Spring Security `Authentication` 转换为 common `CurrentUser`，并通过 Provider 暴露当前用户。
  - 验收点：认证用户、匿名用户和空上下文都能得到稳定 `CurrentUser`，权限从 `GrantedAuthority` 转换，并有单元测试覆盖。
  - 重要度：9/10

## 待审阅任务项

## 待讨论项
