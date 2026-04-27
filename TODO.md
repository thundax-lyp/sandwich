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

## P0 - Cache / Infra 边界演进

- [ ] `spring-security-permission-migration`：用 Spring Security 替换后台自研权限链路
  - 依赖前置：完成 `docs/30-designs/SPRING-SECURITY-PERMISSION-RUNBOOK.md` 的人工审阅
  - 范围对象：`common-security`、后台 token filter、权限注解、权限匹配规则、`PermissionService`、权限 Redis DAO/infra 实现、旧 `Subject` 权限链路
  - 处理动作：新增 `sandwish-common-security` 并同步 project baseline；保留 `role -> menu -> permissions`、默认 `user/admin/super` 和冒号前缀权限语义；登录成功写入权限会话；请求认证恢复权限并 touch TTL；逐步替换旧 `RequiresPermissions`
  - 允许引入 Spring Security：是
  - 允许迁移 `RequiresRoles`：否
  - 允许删除旧权限 AOP：完成新注解全量替换后允许
  - 验收点：后台 Controller 不再依赖自研权限 AOP，权限会话由 `PermissionService -> PermissionDao -> PermissionDaoImpl` 管理，controller/service 不直接触碰 Redis
- [ ] `redis-client-subject-cache-migration`：移除 SubjectService 对 RedisClient 的直接依赖
  - 依赖前置：后台密钥接口已完成 RedisClient 迁移
  - 范围对象：`SubjectServiceImpl`、subject/version 远端缓存 Store/DAO、infra 或安全适配实现
  - 处理动作：保留本地 subject map 和业务编排语义，将远端 subject/version 读写、TTL 和全量失效移出 admin-api service 实现；不新增只转发 Redis 操作的 Service
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：否
  - 验收点：`sandwish-admin-api` 不再直接 import `RedisClient`
- [ ] `redis-client-front-session-dict-migration`：移除前台工具对 RedisClient 的直接依赖
  - 依赖前置：完成 `redis-client-subject-cache-migration`
  - 范围对象：`SessionCacheServiceImpl`、Session 存储实现、`DictUtils`
  - 处理动作：Session 缓存进入明确的会话 Store/DAO 实现；`DictUtils` 复用现有 Dict DAO/cache 路径或删除无效旧缓存；不新增通用 CacheService
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：否
  - 验收点：`sandwish-front-api` 不再直接 import `RedisClient`
- [ ] `redis-client-sms-state-migration`：移除短信验证码 Servlet 对 RedisClient 的直接依赖
  - 依赖前置：完成 `redis-client-front-session-dict-migration`
  - 范围对象：`SmsValidateCodeServlet`、短信发送限流状态 Store/DAO、必要时补短信验证码 Service、infra 实现
  - 处理动作：将 `CACHE_MOBILE` 的 `exists/set/TTL` 收敛到业务语义 Store/DAO；若 Servlet 承担发送校验编排，则补 Service 承接编排；Servlet 不再通过 `SpringContextHolder` 获取 RedisClient
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：否
  - 验收点：`sandwish-biz` 不再直接 import `RedisClient`
- [ ] `redis-client-infra-dao-adapter-migration`：替换 infra DAO 对 common RedisClient 的依赖
  - 依赖前置：完成上层所有 RedisClient 直接依赖迁移
  - 范围对象：`AsyncTaskDaoImpl`、`AccessTokenDaoImpl`、`LoginFormDaoImpl`、`LoginLockDaoImpl`
  - 处理动作：保持既有业务 DAO 接口作为语义边界，用 infra-local 语义存储或窄 adapter 承接 Redis 操作；不把通用 RedisClient 原样搬到 infra；不新增只转发 Redis 操作的 Service
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：否
  - 验收点：infra DAO 不再 import `com.github.thundax.common.utils.redis.RedisClient`
- [ ] `redis-client-cache-support-migration`：替换 CacheSupport 对 common RedisClient 的依赖
  - 依赖前置：完成 `redis-client-infra-dao-adapter-migration`
  - 范围对象：`StorageCacheSupport`、`DictCacheSupport`、`MenuCacheSupport`、`OfficeCacheSupport`、`RoleCacheSupport`、`UserCacheSupport`
  - 处理动作：在 CacheSupport 内部替换 RedisClient，优先评估 JetCache 或 infra-local 窄 adapter；不为 CacheSupport 补 Service/DAO；不改变现有 key、TTL 和失效语义
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：CacheSupport 不再 import common RedisClient，JetCache 类型不外泄到 Service/Controller/Entity/Request/Response
- [ ] `redis-client-common-deletion`：删除 common-core RedisClient
  - 依赖前置：完成所有 RedisClient 调用点迁移
  - 范围对象：`sandwish-common-core` RedisClient 类、相关文档和依赖描述
  - 处理动作：删除 `com.github.thundax.common.utils.redis.RedisClient`；同步移除或收窄 common-core 中 Redis client 职责表述
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：是
  - 验收点：全仓无 `com.github.thundax.common.utils.redis.RedisClient` import，common 不再暴露 Redis 客户端封装
