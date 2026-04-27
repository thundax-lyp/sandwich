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

- [ ] `cache-role-chain-migration`：迁移 Role 缓存链路到 infra
  - 依赖前置：完成 `cache-menu-chain-migration`
  - 范围对象：`RoleServiceImpl`、`RoleDao`、`RoleDaoImpl`、`RoleMapper`、`RoleServiceHolder`
  - 处理动作：将 Role 单对象缓存及角色关联用户、菜单缓存迁入 infra；不改变角色授权关系写入语义
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：Role Service 不再直接读写 `redisClient`，不再调用 `removeAllCache`
- [ ] `cache-user-chain-migration`：迁移 User 缓存链路到 infra
  - 依赖前置：完成 `cache-role-chain-migration`
  - 范围对象：`UserServiceImpl`、`UserDao`、`UserDaoImpl`、`UserMapper`、`UserServiceHolder`
  - 处理动作：将 User 单对象缓存及用户角色缓存迁入 infra；不改变登录名、SSO、密码和启用状态相关查询写入语义
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：User Service 不再直接读写 `redisClient`，不再调用 `removeAllCache`
- [ ] `cache-holder-contract-cleanup`：收窄 ServiceHolder 缓存入口
  - 依赖前置：完成所有已启用缓存链路迁移
  - 范围对象：`DictServiceHolder`、`StorageServiceHolder`、`OfficeServiceHolder`、`MenuServiceHolder`、`RoleServiceHolder`、`UserServiceHolder`、`MemberServiceHolder`
  - 处理动作：删除或收窄迁移后不再需要的 `removeAllCache`、`preload`、`getCacheVersion` 间接入口；不改变 Holder 获取 Service 的职责
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：业务代码不再通过 Holder 间接调用 CrudService 缓存方法
- [ ] `cache-jetcache-introduction`：评估并引入 JetCache 作为 infra 实现细节
  - 依赖前置：至少完成一条缓存链路迁入 infra，并确认 infra 缓存契约稳定
  - 范围对象：最窄可行依赖模块、配置项、infra CacheSupport 实现
  - 处理动作：评估 Spring Boot 兼容版本；只在 infra/cache support 内引入 JetCache；不迁移额外业务链路
  - 允许引入 JetCache：是
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：JetCache 类型不出现在 Service、Controller、Entity、Request、Response 中
- [ ] `crud-service-cache-method-removal`：删除 CrudService 缓存公共契约
  - 依赖前置：完成所有 CrudService 缓存链路迁移和 Holder 缓存入口收窄
  - 范围对象：`CrudService`、`CrudServiceImpl` 中的缓存字段、构造器参数和方法
  - 处理动作：删除 `removeAllCache`、`preload`、`getCacheVersion`、`isRedisCacheEnabled`、Redis cache get/put/remove/version 相关实现；同步调整构造器依赖
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：是
  - 验收点：`CrudServiceImpl` 不再依赖 `RedisClient`，Service 公共契约不再暴露缓存能力
