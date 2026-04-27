# Front Spring Security Runbook

## 1. Purpose

本文档定义 `sandwish-front-api` 从 Shiro 迁移到 Spring Security 的执行路径。

迁移目标：

- 前台安全链路使用 Spring Security。
- `sandwish-front-api` 依赖 `sandwish-common-security` 复用通用安全契约和权限匹配能力。
- 前台 session/cache 使用 `sandwish-common-cache` 提供的 JetCache 基线，不直接接触 Redis。
- 删除 Shiro 依赖、Shiro 配置、Shiro session/cache 和前台 Redis 直连依赖。

## 2. Scope

当前范围：

- `sandwish-front-api` 前台登录、登出、认证上下文和访问控制。
- 前台 Shiro session/cache 到 JetCache-backed session/cache 的迁移。
- `MemberAccessServiceImpl`、`LoginController`、`LogoutController`、`ShiroUtils` 等 Shiro 调用点替换。
- `sandwish-common-security` 依赖接入。

不在范围内：

- 不迁移后台 Spring Security 链路。
- 不改变会员业务表、会员查询和会员状态规则。
- 不把会员登录、第三方登录、验证码、cookie 或 session 业务语义放入 `sandwish-common-security`。
- 不让 `sandwish-common-cache` 承载 session 业务规则。

## 3. Boundary Decision

`sandwish-front-api` 允许依赖 `sandwish-common-security`。

固定边界：

- `sandwish-common-security` 只承载 Spring Security 通用契约、权限匹配和不依赖业务数据的安全对象。
- `sandwish-common-security` 不依赖 `sandwish-biz`、`sandwish-infra`、`sandwish-admin-api`、`sandwish-front-api`。
- 前台会员认证、第三方登录跳转、验证码、RSA 解密、cookie 和 session 适配固定归属 `sandwish-front-api`。
- 会员信息读取和状态校验通过 `sandwish-biz` 的 `MemberService` 完成。
- session/cache 持久化实现可以放在 `sandwish-front-api` 的安全适配层；若后续形成跨入口复用语义，接口放 `sandwish-biz`，实现放 `sandwish-infra`。
- 前台 session/cache 内部使用 JetCache，JetCache 基线来自 `sandwish-common-cache`。
- `sandwish-front-api` 不直接使用 Redis API、`RedisTemplate`、`StringRedisTemplate` 或 `spring-boot-starter-data-redis`。

## 4. Current Shiro Chain

当前前台 Shiro 链路：

`ShiroConfiguration -> ShiroFilterFactoryBean -> MemberAuthenticationFilter -> MemberAuthorizingRealm -> MemberService`

当前 Shiro session/cache 链路：

`ShiroConfiguration -> RedisSessionDaoImpl / RedisCacheManager -> RedisTemplate`

当前 Shiro 调用点：

- `LoginController`
- `LogoutController`
- `MemberAccessServiceImpl`
- `MemberAuthenticationFilter`
- `MemberAuthorizingRealm`
- `MemberUserFilter`
- `ShiroUtils`
- `MemberAuthenticationToken`
- `MemberPrincipal`
- `modules.member.security.shiro.*`
- `modules.sys.security.DefineModularRealmAuthenticator`

## 5. Target Spring Security Chain

目标前台链路：

`FrontSpringSecurityConfiguration -> MemberAuthenticationFilter -> MemberAuthenticationProvider -> MemberService`

目标上下文：

- 登录成功后，Spring Security `Authentication` 持有会员 principal。
- 当前会员读取固定通过 `SecurityContextHolder`。
- `MemberAccessServiceImpl` 不依赖 Shiro。
- 前台 session/cache 使用 JetCache-backed store 保存登录态和必要 session 属性。

## 6. Migration Order

### Step 1: Front Security Dependency Baseline

处理动作：

- `sandwish-front-api` 增加 `sandwish-common-security` 依赖。
- `sandwish-front-api` 增加 `spring-boot-starter-security` 依赖。
- 架构文档放开 `sandwish-front-api -> sandwish-common-security -> sandwish-common-core` 依赖方向。
- 保留 Shiro 依赖和配置，先不改变运行行为。

验收：

- `mvn -q -pl sandwish-front-api -am compile -DskipTests` 通过。

### Step 2: Spring Security Filter Baseline

处理动作：

- 新增 `FrontSpringSecurityConfiguration`。
- `FrontSpringSecurityConfiguration` 固定挂在 `front-spring-security` profile 下，迁移期默认不接管 Shiro 流量。
- 建立与 Shiro chain definition 等价的路径规则。
- 固定匿名路径：`/static/**`、`/servlet/**`、`/auth/register`。
- 固定登录路径：`/auth/login`。
- 固定登出路径：`/auth/logout`。
- 固定会员受保护路径：`/member/**`。
- 新链路只建立基线，不删除 Shiro。

验收：

- Spring Security 配置可编译。
- 迁移期不存在两个安全过滤器同时处理同一路径的不可解释冲突。

### Step 3: Member Authentication Provider

处理动作：

- 新增 Spring Security `AuthenticationProvider` 或等价认证服务。
- 将 `MemberAuthorizingRealm` 中会员装载、启用状态校验和默认密码兼容逻辑迁入 Spring Security 认证链路。
- 保留 RSA 密码解密和第三方登录跳转语义。
- 登录成功后创建 Spring Security `Authentication`。

验收：

- 登录认证不依赖 `AuthorizingRealm`。
- 会员禁用、账号不存在、密码错误仍能返回等价失败语义。

### Step 4: Member Security Context Migration

处理动作：

- 用 Spring Security principal 替代 `MemberPrincipal`。
- `MemberAccessServiceImpl` 改为读取 `SecurityContextHolder`。
- `LoginController` 改为通过 Spring Security 判断当前登录状态。
- `LogoutController` 改为通过 Spring Security logout 或 session invalidation 完成登出。
- `testlogin` 改为 Spring Security 方式或删除。

验收：

- `LoginController`、`LogoutController`、`MemberAccessServiceImpl` 不再 import Shiro。
- `ShiroUtils` 不再被前台业务入口调用。

### Step 5: JetCache-backed Front Session/Cache

处理动作：

- 建立前台 session/cache 语义边界。
- session/cache 内部使用 `@CreateCache`。
- 迁移 Shiro `RedisSessionDaoImpl` 和 `RedisCacheManager` 承载的登录态、session attribute 和过期语义。
- 不新增 Redis 直连，不新增通用 Redis 客户端，不让 `common-cache` 承载前台 session 业务规则。

验收：

- 前台 session/cache 不再使用 `RedisTemplate`、`StringRedisTemplate` 或 `org.springframework.data.redis`。
- JetCache 类型只出现在前台安全适配实现内部。

### Step 6: Remove Shiro Runtime

处理动作：

- 删除 `ShiroConfiguration`。
- 删除 Shiro session/cache 类。
- 删除 `MemberAuthorizingRealm`、`MemberAuthenticationToken`、`ShiroUtils` 和仅服务 Shiro 的异常/过滤器。
- 删除 `shiro-spring-boot-web-starter` 依赖。
- 删除不再使用的 `shiro.*` 配置项。

验收：

- `sandwish-front-api` 无 `org.apache.shiro` import。
- `sandwish-front-api/pom.xml` 无 Shiro starter。
- `mvn -q -pl sandwish-front-api -am compile -DskipTests` 通过。

### Step 7: Cleanup Migration Scene

处理动作：

- 删除或收窄过期 Shiro runbook、TODO 和临时兼容类。
- 删除空 package、无效 `.gitkeep` 和不再使用的配置项。
- 删除 `sandwish-front-api` 中不再需要的 Redis Maven 依赖。
- 保留本文档作为前台 Spring Security 最终运行说明。

验收：

```bash
rg -n "org.apache.shiro|shiro-spring-boot-web-starter|ShiroUtils|ShiroConfiguration|MemberAuthorizingRealm|RedisSessionDaoImpl|RedisCacheManager" sandwish-front-api -g '*.java' -g 'pom.xml' -g '*.yml'
rg -n "spring-boot-starter-data-redis|org.springframework.data.redis|RedisTemplate|StringRedisTemplate" sandwish-front-api -g '*.java' -g 'pom.xml'
mvn -q -pl sandwish-front-api -am compile -DskipTests
```

以上检查固定无输出，编译固定通过。

## 7. Open Items

无
