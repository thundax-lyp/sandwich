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

## P0 - Common Cache / RedisClient JetCache 迁移

执行原则：

- 按列表顺序执行，除非某一步编译失败要求拆分。
- JetCache 只能出现在 `sandwish-common-cache`、`sandwish-infra` 的实现细节，不能外泄到 Controller、Service、Entity、Request、Response。
- 迁移期允许 `RedisClient` 暂存；新增或改造代码不得新增 `RedisClient` 调用点。

- [ ] `common-cache-application-wiring`：后台/前台应用接入 common-cache
  - 依赖前置：`sandwish-common-cache` 基线模块已存在
  - 范围对象：`sandwish-admin-api`、`sandwish-front-api` Maven 依赖和运行配置
  - 处理动作：应用依赖 `sandwish-common-cache`；补齐 JetCache local/remote 配置；迁移期允许 RedisClient 继续存在
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：`mvn -q -pl sandwish-admin-api,sandwish-front-api -am compile -DskipTests` 通过
- [ ] `redis-client-sys-cache-support-jetcache-migration`：用 JetCache 替换系统类 CacheSupport 对 RedisClient 的依赖
  - 依赖前置：完成 `common-cache-application-wiring`
  - 范围对象：`DictCacheSupport`、`MenuCacheSupport`、`OfficeCacheSupport`、`RoleCacheSupport`、`UserCacheSupport`
  - 处理动作：在 CacheSupport 内部使用 `@CreateCache`；保留现有 key、TTL、版本和失效语义；JetCache 类型不外泄到 Service/Controller/Entity/Request/Response
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：系统类 CacheSupport 不再 import common RedisClient
- [ ] `redis-client-storage-cache-support-jetcache-migration`：用 JetCache 替换存储 CacheSupport 对 RedisClient 的依赖
  - 依赖前置：完成 `common-cache-application-wiring`
  - 范围对象：`StorageCacheSupport`
  - 处理动作：在 Storage cache support 内部使用 `@CreateCache`；保留现有 key、TTL、版本和失效语义
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：`StorageCacheSupport` 不再 import common RedisClient
- [ ] `redis-client-auth-dao-jetcache-migration`：用 JetCache 替换 auth DAO 对 RedisClient 的依赖
  - 依赖前置：完成 `common-cache-application-wiring`
  - 范围对象：`AccessTokenDaoImpl`、`LoginFormDaoImpl`、`LoginLockDaoImpl`、`PermissionDaoImpl`
  - 处理动作：保持现有业务 DAO 接口作为语义边界；JetCache 只留在 infra 实现；不新增泛化 CacheService/RedisService
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：auth DAO 不再 import common RedisClient
- [ ] `redis-client-assist-dao-jetcache-migration`：用 JetCache 替换 assist DAO 对 RedisClient 的依赖
  - 依赖前置：完成 `common-cache-application-wiring`
  - 范围对象：`AsyncTaskDaoImpl`、`KeypairPrivateKeyDaoImpl`
  - 处理动作：保持现有业务 DAO 接口作为语义边界；JetCache 只留在 infra 实现；不新增泛化 CacheService/RedisService
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：assist DAO 不再 import common RedisClient
- [ ] `redis-client-front-session-dict-jetcache-migration`：移除 front-api 对 RedisClient 的直接依赖
  - 依赖前置：完成 `common-cache-application-wiring`
  - 范围对象：`SessionCacheServiceImpl`、`DictUtils`
  - 处理动作：前台会话/字典缓存迁到业务 Store/DAO 或已有缓存路径；入口模块不直接注入 JetCache
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：`sandwish-front-api` 不再直接 import `RedisClient`
- [ ] `redis-client-biz-sms-state-jetcache-migration`：移除短信验证码 Servlet 对 RedisClient 的直接依赖
  - 依赖前置：完成 `common-cache-application-wiring`
  - 范围对象：`SmsValidateCodeServlet`
  - 处理动作：短信发送限流状态迁到业务 Store/DAO 或已有缓存路径；Servlet 不通过 `SpringContextHolder` 获取缓存能力；入口层不直接注入 JetCache
  - 允许引入 JetCache：是
  - 允许删除 `RedisClient`：否
  - 验收点：`sandwish-biz` 不再直接 import `RedisClient`
- [ ] `redis-client-common-deletion`：删除 common-core RedisClient
  - 依赖前置：完成所有 RedisClient 调用点迁移
  - 范围对象：`sandwish-common-core` RedisClient 类、相关 Maven 依赖、相关文档
  - 处理动作：删除 `com.github.thundax.common.utils.redis.RedisClient`；同步移除 common-core 中 Redis client 职责
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：是
  - 验收点：全仓无 `com.github.thundax.common.utils.redis.RedisClient` import，common-core 不再暴露 Redis 客户端封装
- [ ] `common-cache-migration-cleanup`：收尾清理 common-cache 迁移现场
  - 依赖前置：完成 `redis-client-common-deletion`
  - 范围对象：过期 RedisClient runbook、缓存边界文档、TODO 已完成项、Maven 依赖、应用配置、无效 package 和 `.gitkeep`
  - 处理动作：删除或收窄旧 RedisClient/infra adapter 文档；移除不再需要的 `spring-boot-starter-data-redis` 依赖；删除完成的 TODO 项或改写为下一阶段明确任务；清理空目录占位文件；保留 `COMMON-CACHE-JETCACHE-RUNBOOK.md` 作为最终运行说明
  - 允许引入 JetCache：否
  - 允许删除 `RedisClient`：已完成后执行
  - 验收点：`rg -n "RedisClient|common.utils.redis" docs TODO.md sandwish-common sandwish-biz sandwish-infra sandwish-admin-api sandwish-front-api -g '*.java' -g '*.md' -g 'pom.xml'` 仅保留历史说明或无输出；`git status --short` 干净
