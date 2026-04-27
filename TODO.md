# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前主线顺序

1. 文档治理骨架
   - 补齐架构、文档、命名、数据库和部署治理文档
2. 三层架构规则收敛
   - 固定 `Controller -> Service -> DAO/Mapper` 边界
3. 业务需求与数据库文档补齐
   - 按现有模块和业务域逐步建立 `10-requirements/` 与 `20-database/`
4. 质量与交付闭环
   - 按任务补测试、文档同步和小步提交

## P0 - Front Spring Security 迁移

执行原则：

- 按列表顺序执行，除非某一步编译失败要求拆分。
- `sandwish-front-api` 可以依赖 `sandwish-common-security`，但 `sandwish-common-security` 不承载会员登录、第三方登录、验证码、cookie 或 session 业务语义。
- 前台 session/cache 使用 `sandwish-common-cache` 的 JetCache 基线，不直接接触 Redis。
- 每一步完成后删除、拆分或收窄对应 TODO，并小步提交。

- [ ] `front-member-security-context-migration`：迁移前台会员上下文读取
  - 依赖前置：`front-spring-security` profile 下的登录认证已迁到 Spring Security provider/filter
  - 范围对象：`LoginController`、`LogoutController`、`MemberAccessServiceImpl`、`ShiroUtils` 调用点
  - 处理动作：用 Spring Security principal 和 `SecurityContextHolder` 替代 Shiro `Subject` / `Session` 读取；`testlogin` 改为 Spring Security 方式或删除
  - 允许删除 Shiro：否
  - 允许删除 Redis：否
  - 验收点：前台 Controller 和 `MemberAccessServiceImpl` 不再 import Shiro
- [ ] `front-session-cache-jetcache-migration`：迁移前台 session/cache 到 JetCache
  - 依赖前置：完成 `front-member-security-context-migration`
  - 范围对象：Shiro session/cache、前台 session attribute、登录态保存和过期语义
  - 处理动作：建立 JetCache-backed 前台 session/cache 语义边界；删除 `RedisTemplate` 直连；不让 `common-cache` 承载前台 session 业务规则
  - 允许删除 Shiro：完成替代链路后允许
  - 允许删除 Redis：完成替代链路后允许
  - 验收点：前台 session/cache 不再使用 `RedisTemplate`、`StringRedisTemplate` 或 `org.springframework.data.redis`
- [ ] `front-shiro-runtime-deletion`：删除 front-api Shiro 运行时
  - 依赖前置：完成 `front-session-cache-jetcache-migration`
  - 范围对象：`ShiroConfiguration`、Shiro session/cache 类、Shiro 工具类、Shiro 依赖和配置
  - 处理动作：删除 Shiro 配置、过滤器、Realm、session/cache、仅服务 Shiro 的异常/工具类；删除 `shiro-spring-boot-web-starter`
  - 允许删除 Shiro：是
  - 允许删除 Redis：否
  - 验收点：`sandwish-front-api` 无 `org.apache.shiro` import，pom 无 Shiro starter，`mvn -q -pl sandwish-front-api -am compile -DskipTests` 通过
- [ ] `front-security-migration-cleanup`：清理前台 Spring Security 迁移现场
  - 依赖前置：完成 `front-shiro-runtime-deletion`
  - 范围对象：过期 Shiro 说明、TODO 已完成项、临时兼容类、空 package、无效配置、front-api Redis Maven 依赖
  - 处理动作：删除或收窄过期 runbook/TODO；删除不再使用的配置项和空目录；删除 `sandwish-front-api` 中不再需要的 `spring-boot-starter-data-redis`
  - 允许删除 Shiro：已完成后执行
  - 允许删除 Redis：是
  - 验收点：`rg -n "org.apache.shiro|shiro-spring-boot-web-starter|ShiroUtils|ShiroConfiguration|MemberAuthorizingRealm|RedisSessionDaoImpl|RedisCacheManager" sandwish-front-api -g '*.java' -g 'pom.xml' -g '*.yml'` 无输出；`rg -n "spring-boot-starter-data-redis|org.springframework.data.redis|RedisTemplate|StringRedisTemplate" sandwish-front-api -g '*.java' -g 'pom.xml'` 无输出；`git status --short` 干净
