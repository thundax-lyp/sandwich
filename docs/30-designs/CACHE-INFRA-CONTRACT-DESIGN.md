# CACHE INFRA CONTRACT DESIGN

## 1. Purpose

本文档固定 CrudService 缓存职责迁入 infra 时的支撑契约。

本设计只定义命名、包路径、key、TTL、命中、回源、回填、失效和版本策略；不迁移任何业务链路。

## 2. Scope

当前范围：

- infra 缓存支撑对象命名
- infra 缓存支撑包路径
- DAO interface、infra DAO implementation、CacheSupport、Mapper 的职责边界
- key、TTL、命中、回源、回填、失效和版本策略
- `Dict` 首条迁移链路的落地约束

不在范围内：

- 不定义 JetCache 具体迁移步骤；当前 active runbook 为 `COMMON-CACHE-JETCACHE-RUNBOOK.md`
- 不把业务 key、TTL、版本和失效策略上提到 `sandwish-common-cache`
- 不删除 `CrudServiceImpl` 缓存方法
- 不迁移 `Dict`、`Storage`、`Office`、`Menu`、`Role`、`User` 任一业务链路
- 不改认证、会话、验证码、登录锁等非 CrudService Redis 用法

## 3. Fixed Shape

每条 CrudService 缓存链路迁移后的固定形态：

```text
Service
  -> DAO interface
      -> infra DAO implementation
          -> CacheSupport
          -> Mapper / Database
```

职责固定为：

- `Service` 只表达业务动作、事务编排和业务校验，不感知缓存。
- `DAO interface` 只表达业务数据访问契约，不出现 `Cache`、`Redis`、`JetCache`、`TTL`、`version` 等缓存实现词汇。
- `infra DAO implementation` 决定是否走缓存，负责把 DAO 查询语义编排为命中、回源、回填和失效。
- `CacheSupport` 负责 key、TTL、命中、回填、失效和版本维护。
- `Mapper` 只负责数据库访问，不感知缓存。

## 4. Package And Naming

业务缓存支撑对象固定放在 `sandwish-infra`。

包路径固定为：

```text
com.github.thundax.modules.<module>.persistence.cache
```

类命名固定为：

```text
<BusinessObject>CacheSupport
```

示例：

- `com.github.thundax.modules.sys.persistence.cache.DictCacheSupport`
- `com.github.thundax.modules.storage.persistence.cache.StorageCacheSupport`
- `com.github.thundax.modules.sys.persistence.cache.OfficeCacheSupport`
- `com.github.thundax.modules.sys.persistence.cache.MenuCacheSupport`
- `com.github.thundax.modules.sys.persistence.cache.RoleCacheSupport`
- `com.github.thundax.modules.sys.persistence.cache.UserCacheSupport`

当前阶段禁止新增通用 `CacheManager`、`CacheTemplate`、`CacheRepository`、`CacheFacade` 或跨业务 helper 层。

只有完成 `common-cache-boundary-decision` 并明确建立 `sandwish-common-cache` 后，才允许把无业务语义的 key、namespace、TTL 或 cache operation 抽象上提到 common。

## 5. DAO Interface Contract

DAO interface 固定保持业务数据访问语义。

允许：

- `get(Dict entity)`
- `getMany(List<String> idList)`
- `findList(Dict entity)`
- `findTypeList()`
- `findRoleMenu(Role role)`
- `findUserRole(User user)`

禁止：

- `getFromCache`
- `putCache`
- `removeCache`
- `removeAllCache`
- `getCacheVersion`
- `preloadCache`
- `getRedisCache`

如果某条链路需要外部获取版本变化，只能先在 infra 设计一个业务语义方法，再由对应 Holder 或 Service 调用业务语义方法；不得继续暴露 CrudService 缓存公共契约。

## 6. CacheSupport Contract

每个 `<BusinessObject>CacheSupport` 固定承担以下能力：

- 生成单对象 key
- 生成版本 key
- 读取单对象缓存
- 写入单对象缓存
- 删除单对象缓存
- 批量读取单对象缓存
- 批量回填单对象缓存
- 按业务需要执行全量失效
- 更新版本
- 读取当前版本

方法命名固定使用业务对象和数据动作，不使用 `Redis` 或 `JetCache`：

- `getById(String id)`
- `putById(<BusinessObject> entity)`
- `removeById(String id)`
- `removeAll()`
- `currentVersion()`
- `touchVersion()`

`Role`、`User` 等关联列表缓存可以增加明确业务语义方法：

- `getRoleUserIds(String roleId)`
- `putRoleUserIds(String roleId, List<String> userIds)`
- `removeRoleUserIds(String roleId)`
- `getUserRoleIds(String userId)`
- `putUserRoleIds(String userId, List<String> roleIds)`
- `removeUserRoleIds(String userId)`

`CacheSupport` 不调用 Service，不处理事务，不做权限判断，不调用 Controller 或 Holder。

## 7. Key Strategy

迁移期固定保留既有 key 语义，避免缓存迁移改变线上缓存读取行为。

既有 CrudService 链路 key：

| 链路 | 单对象 key | 版本 key |
| --- | --- | --- |
| `Dict` | `_INTERACTION_sys.dict.id_{id}` | `_INTERACTION_sys.dict.version` |
| `Storage` | `_INTERACTION_assist.storage.id_{id}` | `_INTERACTION_assist.storage.version` |
| `Office` | `_INTERACTION_SYS_OFFICE_id_{id}` | `_INTERACTION_SYS_OFFICE_version` |
| `Menu` | `_INTERACTION_sys.menu.id_{id}` | `_INTERACTION_sys.menu.version` |
| `Role` | `_INTERACTION_sys.role.id_{id}` | `_INTERACTION_sys.role.version` |
| `User` | `_INTERACTION_sys.userid_{id}` | `_INTERACTION_sys.userversion` |

关联列表 key：

| 链路 | key |
| --- | --- |
| `Role` 用户列表 | `_INTERACTION_sys.role.users_{roleId}` |
| `Role` 菜单列表 | `_INTERACTION_sys.role.menus_{roleId}` |
| `User` 角色列表 | `_INTERACTION_sys.user.roles_{userId}` |

新 key 只能由对应 `<BusinessObject>CacheSupport` 生成，不得散落在 Service、Holder、DAO implementation 或 Mapper 中。

## 8. TTL Strategy

迁移期固定保留既有 TTL 语义：

- CrudService 单对象缓存 TTL：`3600` 秒
- CrudService 版本更新 TTL：`3605` 秒
- CrudService 版本空值初始化：沿用当前无显式 TTL 行为
- `Role` 关联用户/菜单列表：沿用当前无显式 TTL 行为
- `User` 关联角色列表：沿用当前无显式 TTL 行为

TTL 常量归属对应 `<BusinessObject>CacheSupport`。

禁止在 Service、Holder、DAO interface、Mapper 中定义缓存 TTL。

## 9. Hit Miss Backfill

单对象读取固定流程：

1. `Service` 调用 DAO interface 的业务查询方法。
2. infra DAO implementation 调用 `CacheSupport.getById(id)`。
3. 命中时直接返回缓存对象。
4. 未命中时调用 Mapper / Database 回源。
5. 回源结果非空时调用 `CacheSupport.putById(entity)`。
6. infra DAO implementation 返回业务 `Entity`。

批量读取固定流程：

1. infra DAO implementation 先按 id 列表读取缓存。
2. 已命中对象进入结果集合。
3. 未命中 id 列表调用 Mapper / Database 回源。
4. 回源结果逐条回填缓存。
5. 返回结果必须保持当前业务语义，不因缓存命中顺序引入新的排序承诺。

缓存值迁移期固定优先沿用当前 CrudService 缓存值形态，即业务 `Entity` JSON。

如果后续改为缓存 `DO/DataObject`，必须作为单独任务显式说明兼容、清理和回滚策略。

## 10. Invalidation

单对象失效固定由 infra DAO implementation 在写操作完成后调用 `CacheSupport.removeById(id)`。

全量失效固定由 infra DAO implementation 或对应 `CacheSupport` 根据业务写入语义调用 `CacheSupport.removeAll()`。

版本维护固定在 `CacheSupport` 内部完成：

- `removeById(id)` 后调用 `touchVersion()`
- `removeAll()` 后调用 `touchVersion()`
- `touchVersion()` 写入新的版本值

迁移期允许 `removeAll()` 沿用既有 namespace pattern 删除策略。

长期目标禁止新增全局 pattern delete 作为新的缓存设计；后续链路能明确受影响 key 时，优先删除明确 key。

## 11. Version Strategy

版本号只用于本地缓存或全量缓存列表判断。

固定规则：

- 版本 key 由对应 `<BusinessObject>CacheSupport` 维护。
- 版本读取方法只允许 infra 或当前仍待迁移的 Holder 适配层调用。
- 完成链路迁移后，Service 不再调用 `getCacheVersion()`。
- 完成 Holder 收窄后，业务代码不再通过 `CrudService` 公共契约读取版本。

`Dict` 首条迁移必须把 `DictServiceHolder.getDictList(type)` 对 `getCacheVersion()` 的依赖改为不依赖 CrudService 公共契约。

## 12. Dict First Chain Contract

`Dict` 迁移时固定采用以下边界：

- `DictServiceImpl` 只调用 `DictDao`。
- `DictServiceImpl` 不覆写 `isRedisCacheEnabled()`。
- `DictServiceImpl.initialize()` 不调用 `removeAllCache()`。
- `DictServiceImpl.removeCache(Dict)` 不再存在。
- `DictDao` 不新增缓存语义方法。
- `DictDaoImpl` 注入 `DictCacheSupport`。
- `DictDaoImpl.get(Dict)` 编排单对象缓存命中、回源和回填。
- `DictDaoImpl.getMany(List<String>)` 编排批量单对象缓存命中、回源和回填。
- `DictDaoImpl` 在 `insert`、`update`、`updatePriority`、`updateDelFlag`、`delete` 后触发全量失效。
- `DictCacheSupport` 维护 `_INTERACTION_sys.dict.` namespace、单对象 key、版本 key、TTL 和全量失效。
- `DictServiceHolder` 的本地 `TYPE_DICT_MAP` 不再通过 `CrudService.getCacheVersion()` 判断版本。

## 13. Common Cache Decision Inputs

本设计为 `common-cache-boundary-decision` 提供以下判断输入：

- 缓存基础设施固定由 `sandwish-common-cache` 提供 JetCache 基线，业务缓存语义仍归属 infra DAO/cache support。
- 首条 `Dict` 迁移只需要一个业务专用 `DictCacheSupport`。
- `Storage`、`Office`、`Menu`、`Role`、`User` 后续可能复用 key、TTL 和缓存操作抽象，但复用价值必须在至少一条链路迁移后再判断。
- 当前阶段缓存 key、失效时机和版本策略均带业务语义，固定留在 infra。

## 14. Verification

本设计项是文档任务，收口检查固定为：

- `TODO.md` 删除 `cache-infra-contract-design`
- 未修改业务代码
- 未新增 Maven 模块
- 未引入 JetCache
- `git diff --check` 通过

## 15. Open Items

无
