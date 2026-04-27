# Spring Security 权限整改 Runbook

## 1. Purpose

本文档定义后台权限体系从自研 `RequiresPermissions` AOP 迁移到 Spring Security 的执行路径。

迁移目标：

- 固定使用 Spring Security 承接认证上下文、过滤器链和方法级授权。
- 保留 Sandwich 现有权限来源与匹配语义。
- 将权限会话的 Redis 读写、TTL、touch 和失效收敛到 `PermissionService -> PermissionDao -> PermissionDaoImpl`。
- 删除未使用的角色注解能力，避免权限模型同时存在两套入口。

## 2. Scope

当前范围：

- 后台 API 权限体系。
- `RequiresPermissions` 到 Spring Security 注解的替换。
- `SubjectServiceImpl.createSubject()` 中权限装配语义的迁移。
- 权限会话 Redis 生命周期管理。
- `common-security` 模块引入与项目基线同步。

不在范围内：

- 前台 `sandwish-front-api` 的 Shiro 链路替换。
- 用户、角色、菜单数据库模型调整。
- 业务权限编码重命名。
- 登录密码、验证码、密钥交换逻辑重构。

## 3. Bounded Context

现有后台权限链路：

`AccessTokenFilter -> UserAccessHolder -> RequiresPermissions AOP -> SecurityUtils.getSubject() -> SubjectServiceImpl.createSubject() -> Subject.isPermitted()`

目标后台权限链路：

`Spring Security token filter -> Authentication -> PermissionService touch/load -> Spring method authorization -> PermissionMatcher`

权限来源固定保持：

`User -> Role -> Menu -> permissions`

默认权限固定保持：

- 普通登录用户灌入 `user`
- 管理员灌入 `admin`
- 超级管理员灌入 `super` 和 `admin`

权限匹配规则固定保持：

- `sys:role` 覆盖 `sys:role:*`
- 判断 `sys:role:view` 时，命中 `sys`、`sys:role`、`sys:role:view` 任一权限即通过
- 不使用真实 `*` 字符串作为权限数据要求

## 4. Module Mapping

### `sandwish-common-security`

职责：

- 引入 Spring Security。
- 定义 Spring Security 接入配置、公共认证模型和方法授权支撑。
- 定义 `PermissionMatcher` 等不依赖业务模块的通用权限匹配契约。
- 定义新的 Spring 方法安全注解或表达式入口。

边界：

- 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。
- 不访问 Redis。
- 不装载业务用户、角色、菜单。
- 不承载业务权限编码列表。

### `sandwish-biz`

职责：

- 定义 `PermissionService`。
- 定义 `PermissionDao` 接口。
- 承接权限装配业务语义。
- 对外提供权限创建、加载、touch、释放和全量失效能力。

边界：

- 不直接依赖 Redis 客户端。
- 不依赖 Spring Security 入口实现细节。
- 不暴露 HTTP request / response。

### `sandwish-infra`

职责：

- 实现 `PermissionDaoImpl`。
- 使用 Redis 管理权限集合、版本、TTL、touch 和 deleteAll。
- 保留 Redis key、TTL 和失效语义的可追踪边界。

边界：

- 不承载业务流程。
- 不暴露 Redis 客户端给 Controller 或 Service 编排层。

### `sandwish-admin-api`

职责：

- 使用 Spring Security filter 替换 `AccessTokenFilter` 的认证入口。
- 登录成功后调用 `PermissionService` 创建权限会话。
- 请求认证成功后恢复 `Authentication` 并 touch 权限会话。
- 将 Controller 权限注解从旧 `RequiresPermissions` 替换到新的 Spring 权限入口。

边界：

- Controller 不直接触碰 Redis。
- Controller 不直接调用权限 DAO。
- 不继续维护自研权限 AOP。

## 5. Migration Steps

### 5.1 建立 `common-security`

执行项：

- 新增 `sandwish-common/sandwish-common-security`。
- 引入 Spring Security 依赖，版本受当前 Spring Boot `2.0.5.RELEASE` 约束。
- 同步 root `pom.xml`、`sandwish-common/pom.xml`。
- 同步 `docs/00-governance/ARCHITECTURE.md` project baseline 与模块职责。
- 同步 `docs/AGENT.md` module router。

验收点：

- Maven reactor 能识别新模块。
- `sandwish-common-security` 不依赖业务模块。
- project baseline 中包含 `sandwish-common-security`。

### 5.2 抽取权限匹配规则

执行项：

- 将冒号分段前缀匹配规则沉淀为 `PermissionMatcher`。
- 保留 `sys:role` 覆盖 `sys:role:view` 的行为。
- 明确空权限、空用户权限集合时拒绝访问。

验收点：

- 权限匹配规则可单测。
- `PermissionMatcher` 不依赖 `Subject`。
- 不引入真实 `*` 字符串匹配要求。

### 5.3 建立权限会话服务

执行项：

- 在 `sandwish-biz` 定义 `PermissionService`。
- 在 `sandwish-biz` 定义 `PermissionDao` 接口。
- 在 `sandwish-infra` 实现 Redis `PermissionDaoImpl`。
- 将 `SubjectServiceImpl.createSubject()` 中的权限装配语义迁移到权限服务。
- 将权限集合、版本、TTL、touch、release、deleteAll 通过 DAO 实现。

验收点：

- 权限来源仍为 `role -> menu -> permissions`。
- `user/admin/super` 默认权限行为不变。
- Redis TTL 和 touch 不暴露到 Controller。
- 菜单或角色变更仍能触发权限会话失效。

### 5.4 登录链路写入权限会话

执行项：

- 后台登录成功并创建 token 后，调用 `PermissionService` 创建权限会话。
- 权限会话与 token 或 userId 的映射规则固定。
- 登出和 token 删除时释放权限会话。

验收点：

- 登录后请求可从权限会话恢复权限集合。
- 登出后权限会话失效。
- token 失效后不能继续 touch 权限会话。

### 5.5 Spring Security 接管请求认证

执行项：

- 用 Spring Security token filter 承接 `AccessTokenFilter` 认证职责。
- token 校验成功后创建 `Authentication`。
- `Authentication` 中固定携带 userId 和权限集合，或携带可延迟加载权限的主体对象。
- 每次有效请求 touch 权限会话 TTL。

验收点：

- 未登录请求返回现有未授权响应语义。
- 已登录请求可继续通过 `UserAccessHolder` 或兼容适配获取当前用户。
- 有效请求会刷新权限会话 TTL。

### 5.6 替换方法级权限注解

执行项：

- 定义基于 Spring Security 的权限注解或表达式入口。
- 将 Controller 上的旧 `@RequiresPermissions` 逐步替换为新入口。
- 将现有两处 `Logical.OR` 用法收敛为 `@RequiresPermissions("sys:role")` 等价语义后再迁移。
- 不迁移 `RequiresRoles`。

验收点：

- 所有后台 Controller 权限入口统一使用 Spring Security。
- `RequiresRoles` 无业务使用且不进入新体系。
- `Logical.OR` 不作为新注解能力保留。

### 5.7 删除旧权限链路

执行项：

- 删除旧 `AuthorizationAttributeSourceAdvisor`。
- 删除旧 `AopAllianceAnnotationsAuthorizingMethodInterceptor`。
- 删除旧 `PermissionAnnotationMethodInterceptor`。
- 删除旧 `RoleAnnotationMethodInterceptor`。
- 删除旧 `RequiresPermissions`、`RequiresRoles`、`Logical`。
- 删除或收窄 `Subject`、`SecurityUtils`、`SubjectServiceImpl`。

验收点：

- 全仓无旧权限注解 import。
- 后台不再注册自研权限 AOP。
- 权限判断全部由 Spring Security 权限入口触发。

## 6. Compatibility Rules

- 迁移期间允许双轨运行，但同一个 Controller 方法不得同时保留旧权限注解和新权限注解。
- 每个迁移提交必须能明确说明迁移了哪一段链路。
- 行为迁移必须补测试或运行对应模块测试。
- 删除旧能力前必须先完成全仓引用检查。
- Spring Security 版本不得突破 Spring Boot 固定约束。

## 7. Test Strategy

必须覆盖：

- `sys:role` 命中 `sys:role:view`。
- `sys:role:view` 不命中 `sys:role` 反向要求。
- 普通用户默认拥有 `user`。
- admin 用户默认拥有 `admin` 和 `user`。
- super 用户默认拥有 `super`、`admin` 和 `user`。
- token 有效请求触发 touch。
- token 失效请求不得 touch。
- 角色或菜单变更触发权限会话失效。

测试隔离固定遵循 `docs/00-governance/ARCHITECTURE.md` 的 DAO / Mapper 规则。登录与权限配置 testcase 使用测试专用 InMemory DAO/Impl 承接状态，不依赖 Redis、外部数据库或生产缓存。

最小命令：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/bin:$PATH" mvn -pl sandwish-admin-api -am test
```

新增 `common-security` 后追加：

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home PATH="/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/bin:$PATH" mvn -pl sandwish-common/sandwish-common-security test
```

## 8. Open Items

无
