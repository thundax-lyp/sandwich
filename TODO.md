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

- [ ] `cache-current-usage-inventory`：盘点 CrudService 缓存现状
  - 参考手册：`docs/30-designs/CACHE-INFRA-BOUNDARY-RUNBOOK.md`
  - 范围对象：`CrudServiceImpl`、12 个直接继承 `CrudServiceImpl` 的 Service、6 条已启用 Redis 缓存的链路、`*ServiceHolder` 缓存方法、非 CrudService 的 `RedisClient` 调用点
  - 已知启用缓存链路：`Dict`、`Storage`、`Office`、`Menu`、`Role`、`User`
  - 已知未启用缓存链路：`Member`、`Signature`、`Log`、`UploadFile`、`DefaultUserEncrypt`、`DatabaseUserEncrypt`
  - 已知公共缓存方法调用：`removeAllCache`、`preload`、`getCacheVersion`
  - 处理动作：只输出详细现状、调用方、缓存 key/TTL/失效触发点和首条迁移链路建议；不改代码
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：后续迁移链路顺序和不迁移范围明确；非 CrudService 的认证、会话、验证码、登录锁等 Redis 用法被标记为不在本轮范围
- [ ] `cache-infra-contract-design`：固定 infra 缓存支撑契约
  - 参考手册：`docs/30-designs/CACHE-INFRA-BOUNDARY-RUNBOOK.md`
  - 范围对象：缓存支撑对象命名、包路径、key、TTL、命中、回源、回填、失效和版本策略
  - 处理动作：只设计 infra 缓存支撑形态和迁移接口；不迁移业务链路
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：Service、DAO interface、infra DAO implementation、CacheSupport、Mapper 的职责边界明确
- [ ] `common-cache-boundary-decision`：判断是否建立 common-cache 模块
  - 参考手册：`docs/30-designs/COMMON-CACHE-RUNBOOK.md`
  - 依赖前置：完成 `cache-current-usage-inventory`、`cache-infra-contract-design`
  - 范围对象：`sandwish-common-cache` 候选模块、common 缓存抽象、infra 业务缓存支撑、JetCache 候选适配边界
  - 处理动作：按 RUNBOOK 判断是否建立 `sandwish-common-cache`；若建立则拆分后续 common-cache 子任务，若不建立则收窄为 infra 内部缓存支撑任务；本项完成时删除 `COMMON-CACHE-RUNBOOK.md`
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：common-cache 是否建模块已有结论；TODO 已新增或收窄后续执行项；`docs/30-designs/COMMON-CACHE-RUNBOOK.md` 已在同一提交中删除
- [ ] `cache-dict-chain-migration`：迁移 Dict 缓存链路到 infra
  - 依赖前置：完成 `cache-current-usage-inventory`、`cache-infra-contract-design`、`common-cache-boundary-decision`
  - 范围对象：`DictServiceImpl`、`DictDao`、`DictDaoImpl`、`DictMapper`、`DictServiceHolder` 中和 Dict 缓存直接相关的调用
  - 处理动作：将 Dict 的缓存命中、回源、回填、失效和版本维护迁入 infra；Service 不再直接调用缓存相关方法
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：Dict 对外查询语义不变；`DictServiceImpl` 不再覆写 `isRedisCacheEnabled`，不再调用 `removeAllCache` 或 `getCacheVersion`
- [ ] `cache-storage-chain-migration`：迁移 Storage 缓存链路到 infra
  - 依赖前置：完成 `cache-dict-chain-migration`
  - 范围对象：`StorageServiceImpl`、`StorageDao`、`StorageDaoImpl`、`StorageMapper`、`StorageServiceHolder`
  - 处理动作：将 Storage 单对象缓存和写后失效迁入 infra；不改变文件存储、预览 URL 或业务绑定逻辑
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：Storage Service 不再直接调用 `removeAllCache`；非 CrudService 的文件处理逻辑不被改动
- [ ] `cache-office-chain-migration`：迁移 Office 缓存链路到 infra
  - 依赖前置：完成 `cache-infra-contract-design`
  - 范围对象：`OfficeServiceImpl`、`OfficeDao`、`OfficeDaoImpl`、`OfficeMapper`、`OfficeServiceHolder`
  - 处理动作：将 Office 缓存迁入 infra；树结构写操作后的失效策略必须显式记录
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：Office 树查询和移动语义不变；Service 不再直接操作缓存
- [ ] `cache-menu-chain-migration`：迁移 Menu 缓存链路到 infra
  - 依赖前置：完成 `cache-office-chain-migration`
  - 范围对象：`MenuServiceImpl`、`MenuDao`、`MenuDaoImpl`、`MenuMapper`、`MenuServiceHolder`、`UserServiceHolder` 中的 `preload` 调用
  - 处理动作：将 Menu 缓存、预加载和版本判断迁入 infra；不改变权限菜单查询语义
  - 允许引入 JetCache：否
  - 允许删除 `CrudServiceImpl` 缓存方法：否
  - 验收点：`MenuServiceImpl` 不再调用 `preload`、`removeAllCache`、`getCacheVersion`
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
