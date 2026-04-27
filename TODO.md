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

- [ ] `front-security-migration-cleanup`：清理前台 Spring Security 迁移现场
  - 依赖前置：Shiro runtime 已删除
  - 范围对象：过期 Shiro 说明、TODO 已完成项、临时兼容类、空 package、无效配置、front-api Redis Maven 依赖
  - 处理动作：删除或收窄过期 runbook/TODO；删除不再使用的配置项和空目录；删除 `sandwish-front-api` 中不再需要的 `spring-boot-starter-data-redis`
  - 允许删除 Shiro：已完成后执行
  - 允许删除 Redis：是
  - 验收点：`rg -n "org.apache.shiro|shiro-spring-boot-web-starter|ShiroUtils|ShiroConfiguration|MemberAuthorizingRealm|RedisSessionDaoImpl|RedisCacheManager" sandwish-front-api -g '*.java' -g 'pom.xml' -g '*.yml'` 无输出；`rg -n "spring-boot-starter-data-redis|org.springframework.data.redis|RedisTemplate|StringRedisTemplate" sandwish-front-api -g '*.java' -g 'pom.xml'` 无输出；`git status --short` 干净
