# CACHE CURRENT USAGE INVENTORY

## 1. Purpose

本文档记录 `CrudServiceImpl` 缓存职责迁移前的当前使用现状。

本盘点只用于确定后续迁移顺序、迁移边界和不迁移范围；不改变任何业务代码。

## 2. Scope

当前范围：

- `CrudServiceImpl`
- 12 个直接继承 `CrudServiceImpl` 的业务 `Service`
- 6 条已启用 Redis 缓存的 CrudService 链路
- `*ServiceHolder` 中的缓存入口
- 非 CrudService 的 `RedisClient` 调用点标记

不在范围内：

- 不迁移任何缓存链路
- 不引入 JetCache
- 不删除 `CrudServiceImpl` 缓存方法
- 不改认证、会话、验证码、登录锁等非 CrudService Redis 用法

## 3. CrudService Cache Template

`CrudServiceImpl` 当前承载通用缓存模板职责：

- 启用开关：`isRedisCacheEnabled()`，默认 `false`
- 默认 TTL：`3600` 秒
- 默认对象 key：`cacheSection + "id_" + entity.id`
- 默认版本 key：`cacheSection + "version"`
- 单对象读取：`get(T)`、`get(String)`、`getMany(List<String>)` 先读 Redis，未命中后回源 DAO 并回填
- 单对象写后失效：`save`、`delete`、`updatePriority`、`updateDelFlag` 调用 `removeCache(entity)`
- 全量失效：`removeAllCache()` 使用 `deleteByPattern(cacheSection + "*")`，并更新版本号；`RedisClient.keys(pattern)` 内部还会追加 `*`
- 预加载：`preload(Collection<String>)` 按 id 检查未命中集合，最多每批 `500` 条调用 `dao.getMany`

缓存初始化时，`CrudServiceImpl.initService()` 会在启用缓存的 Service 启动后执行 `redisClient.delete(cacheSection)`；该调用只删除精确 key，不等同于全量 pattern 失效。

## 4. Direct CrudService Inheritors

| Service | 是否启用 CrudService Redis 缓存 | `cacheSection` | 当前缓存职责 |
| --- | --- | --- | --- |
| `DictServiceImpl` | 是 | `_INTERACTION_sys.dict.` | 单对象缓存、全量失效、版本号驱动 `DictServiceHolder` 本地列表缓存 |
| `StorageServiceImpl` | 是 | `_INTERACTION_assist.storage.` | 单对象缓存、启动全量失效 |
| `OfficeServiceImpl` | 是 | `_INTERACTION_SYS_OFFICE_` | 单对象缓存、保存/删除/移动树节点后全量失效 |
| `MenuServiceImpl` | 是 | `_INTERACTION_sys.menu.` | 版本号驱动 Service 内存全量菜单缓存、单对象缓存接口、保存/删除/移动树节点后全量失效 |
| `RoleServiceImpl` | 是 | `_INTERACTION_sys.role.` | 单对象缓存、角色用户 id 列表缓存、角色菜单 id 列表缓存、写后联动 User 全量失效 |
| `UserServiceImpl` | 是 | `_INTERACTION_sys.user` | 单对象缓存、用户角色 id 列表缓存、按登录名/SSO 查询后回填单对象缓存、删除后联动 Role 全量失效 |
| `MemberServiceImpl` | 否 | 默认类名段 | 不启用 CrudService Redis 缓存 |
| `SignatureServiceImpl` | 否 | 默认类名段 | 不启用 CrudService Redis 缓存 |
| `LogServiceImpl` | 否 | 默认类名段 | 不启用 CrudService Redis 缓存 |
| `UploadFileServiceImpl` | 否 | 默认类名段 | 不启用 CrudService Redis 缓存 |
| `DefaultUserEncryptServiceImpl` | 否 | 默认类名段 | 不启用 CrudService Redis 缓存 |
| `DatabaseUserEncryptServiceImpl` | 否 | 默认类名段 | 不启用 CrudService Redis 缓存 |

## 5. Enabled Chain Details

### 5.1 Dict

缓存 key：

- 单对象：`_INTERACTION_sys.dict.id_{dictId}`
- 版本：`_INTERACTION_sys.dict.version`

TTL：

- 单对象：`3600` 秒
- 版本更新：`3605` 秒
- 版本空值初始化：未显式设置 TTL

命中、回源、回填：

- `get` / `getMany` 走 `CrudServiceImpl` 默认单对象缓存。
- `DictServiceHolder.get(id)` 使用线程内本地 map 包装 `getService().get(id)`。
- `DictServiceHolder.getDictList(type)` 使用 `getCacheVersion()` 判断是否重建 `TYPE_DICT_MAP`，重建时调用 `findList(new Dict())`。

失效触发点：

- `DictServiceImpl.initialize()` 启动时调用 `removeAllCache()`。
- `DictServiceImpl.removeCache(Dict)` 覆写为 `super.removeAllCache()`，因此 `save`、`delete`、`updatePriority`、`updateDelFlag` 都会全量失效。
- `DictServiceHolder.removeAllCache()` 对外暴露全量失效入口。

迁移注意：

- `Dict` 是首条迁移候选。缓存语义集中，key 简单，写后全量失效已经是既有行为。
- 迁移时需要同时处理 `DictServiceHolder` 的本地 `TYPE_DICT_MAP` 版本判断，否则会保留 Service 对 `getCacheVersion()` 的依赖。

### 5.2 Storage

缓存 key：

- 单对象：`_INTERACTION_assist.storage.id_{storageId}`
- 版本：`_INTERACTION_assist.storage.version`

TTL：

- 单对象：`3600` 秒
- 版本更新：`3605` 秒
- 版本空值初始化：未显式设置 TTL

命中、回源、回填：

- `get` / `getMany` 走 `CrudServiceImpl` 默认单对象缓存。
- `StorageServiceHolder.get(id)` 使用线程内本地 map 包装 `getService().get(id)`。

失效触发点：

- `StorageServiceImpl.initialize()` 启动时调用 `removeAllCache()`。
- 继承的 `save` / `delete` / `updatePriority` / `updateDelFlag` 只删除单对象 key 并更新版本。
- `updateEnableFlag`、`updatePublicFlag`、`removeBusiness`、`insertBusiness` 当前不调用缓存失效。
- `StorageServiceHolder.removeAllCache()` 对外暴露全量失效入口。

迁移注意：

- `Storage` 不是首条迁移候选，因为文件业务绑定和启用/公开状态写入存在当前未显式失效点，迁移前需要先固定是否沿用该行为。

### 5.3 Office

缓存 key：

- 单对象：`_INTERACTION_SYS_OFFICE_id_{officeId}`
- 版本：`_INTERACTION_SYS_OFFICE_version`

TTL：

- 单对象：`3600` 秒
- 版本更新：`3605` 秒
- 版本空值初始化：未显式设置 TTL

命中、回源、回填：

- `get` / `getMany` 走 `CrudServiceImpl` 默认单对象缓存。
- `OfficeServiceHolder.get(id)` 使用线程内本地 map 包装 `getService().get(id)`。

失效触发点：

- `save` 后全量失效。
- `delete` 后全量失效。
- `moveTreeNode` 后全量失效。
- `OfficeServiceHolder.removeAllCache()` 对外暴露全量失效入口。

迁移注意：

- `Office` 是树结构，迁移时必须显式记录保存、删除和移动节点对整棵树缓存的失效策略。
- 不作为首条迁移链路。

### 5.4 Menu

缓存 key：

- 单对象：`_INTERACTION_sys.menu.id_{menuId}`
- 版本：`_INTERACTION_sys.menu.version`

TTL：

- 单对象：`3600` 秒
- 版本更新：`3605` 秒
- 版本空值初始化：未显式设置 TTL

命中、回源、回填：

- `initialize()` 读取 `getCacheVersion()` 并加载 `dao.findList(new Menu())` 到 Service 内存 `cacheMenuList`。
- `findAllList()` 通过 `getCacheVersion()` 判断是否重建 `cacheMenuList`。
- `get(String)`、`get(Menu)`、`findList(Integer)`、`findChildList(String)` 主要从 `cacheMenuList` 读取。
- `UserServiceHolder.findMenuList(User)` 调用 `MenuServiceHolder.getService().preload(...)` 和 `getMany(...)`。

失效触发点：

- `save` 后全量失效。
- `delete` 后全量失效。
- `moveTreeNode` 后全量失效。
- `updateDisplayFlag` 调用 `removeCache(menu)`，删除单对象 key、更新版本，并通知 `MenuServiceImpl.CacheChangedListener`。
- `MenuServiceHolder.removeAllCache()` 对外暴露全量失效入口。

迁移注意：

- `Menu` 同时存在 Redis 版本号、Service 内存全量列表、Holder 预加载和权限菜单查询，不作为首条迁移链路。
- 后续迁移需要把版本判断、全量菜单加载和 `preload` 调用一起收束。

### 5.5 Role

缓存 key：

- 单对象：`_INTERACTION_sys.role.id_{roleId}`
- 版本：`_INTERACTION_sys.role.version`
- 角色用户 id 列表：`_INTERACTION_sys.role.users_{roleId}`
- 角色菜单 id 列表：`_INTERACTION_sys.role.menus_{roleId}`

TTL：

- 单对象：`3600` 秒
- 版本更新：`3605` 秒
- 版本空值初始化：未显式设置 TTL
- 角色用户 id 列表、角色菜单 id 列表：通过 `RedisClient.computeIfAbsent` 写入，当前调用点未显式传入 TTL

命中、回源、回填：

- `get` / `getMany` 走 `CrudServiceImpl` 默认单对象缓存。
- `findRoleUser(Role)` 使用线程内 map 和 Redis 缓存 roleId 到 userIdList，再返回 `new User(id)` 列表。
- `findRoleMenu(Role)` 使用线程内 map 和 Redis 缓存 roleId 到 menuIdList，再返回 `new Menu(id)` 列表。
- `RoleServiceHolder.get(id)` 使用线程内本地 map 包装 `getService().get(id)`。

失效触发点：

- `save`、`updateUserList`、`updateEnableFlag`、`delete` 通过 `removeCache(role)` 删除单对象、角色用户列表、角色菜单列表。
- `removeCache(role)` 还会调用 `UserServiceHolder.getService().removeAllCache()`，并通知 `RoleServiceImpl.CacheChangedListener`。
- `RoleServiceHolder.removeAllCache()` 对外暴露全量失效入口。

迁移注意：

- `Role` 存在角色关联用户、菜单缓存以及 User 联动全量失效，不作为首条迁移链路。
- 后续迁移必须先明确角色变更时对 User 单对象和用户角色列表的失效边界。

### 5.6 User

缓存 key：

- 单对象：`_INTERACTION_sys.userid_{userId}`
- 版本：`_INTERACTION_sys.userversion`
- 用户角色 id 列表：`_INTERACTION_sys.user.roles_{userId}`

TTL：

- 单对象：`3600` 秒
- 版本更新：`3605` 秒
- 版本空值初始化：未显式设置 TTL
- 用户角色 id 列表：通过 `RedisClient.computeIfAbsent` 写入，当前调用点未显式传入 TTL

命中、回源、回填：

- `get` / `getMany` 走 `CrudServiceImpl` 默认单对象缓存。
- `getByLoginName`、`getBySsoLoginName` 直接查 DAO，命中后 `putCache(user)` 回填单对象缓存，并触发 `userEncryptService.get(user.getId())`。
- `findUserRole(User)` 使用线程内 map 和 Redis 缓存 userId 到 roleIdList，再返回 `new Role(id)` 列表。
- `UserServiceHolder.get(id)` 使用线程内本地 map 包装 `getService().get(id)`。

失效触发点：

- `save`、`updatePassword`、`updateLoginInfo`、`updateEnableFlag`、`delete` 通过 `removeCache(user)` 删除单对象和用户角色列表。
- `delete` 额外调用 `RoleServiceHolder.getService().removeAllCache()`。
- `UserServiceHolder.removeAllCache()` 对外暴露全量失效入口。

迁移注意：

- `User` 涉及登录名、SSO、密码、启用状态、用户角色和 Role 联动，不作为首条迁移链路。
- 当前 `cacheSection` 没有结尾分隔符，单对象 key 为 `_INTERACTION_sys.userid_{userId}`，迁移时不得顺手改 key 语义。

## 6. ServiceHolder Cache Entrypoints

当前 Holder 缓存入口分为三类：

- 线程内本地单对象缓存：`DictServiceHolder`、`StorageServiceHolder`、`OfficeServiceHolder`、`MenuServiceHolder`、`RoleServiceHolder`、`UserServiceHolder`、`MemberServiceHolder`
- CrudService 全量失效入口：`DictServiceHolder.removeAllCache()`、`StorageServiceHolder.removeAllCache()`、`OfficeServiceHolder.removeAllCache()`、`MenuServiceHolder.removeAllCache()`、`RoleServiceHolder.removeAllCache()`、`UserServiceHolder.removeAllCache()`、`MemberServiceHolder.removeAllCache()`
- 版本或预加载入口：`DictServiceHolder.getDictList(type)` 调用 `getCacheVersion()`；`UserServiceHolder.findMenuList(User)` 调用 `MenuService.preload(...)`

`MemberServiceHolder.removeAllCache()` 当前调用未启用 Redis 缓存的 `MemberService`，属于公共契约暴露造成的无效缓存入口。

## 7. Non CrudService Redis Usage

以下 `RedisClient` 用法不属于本轮 CrudService 缓存迁移范围：

| 位置 | 用途 | 范围判断 |
| --- | --- | --- |
| `AccessTokenDaoImpl` | access token 与 userId 双向索引，TTL 来自 `AuthProperties.loginExpiredSeconds` 加安全秒数 | 认证在线态，不迁移 |
| `LoginFormDaoImpl` | 登录表单、refresh token、验证码字段，TTL 来自 `AuthProperties.loginExpiredSeconds` 和 `REFRESH_REMAIN_SECONDS` | 登录会话，不迁移 |
| `LoginLockDaoImpl` | 登录失败次数、锁定用户标记，TTL 由调用方传入 | 登录锁，不迁移 |
| `AsyncTaskDaoImpl` | 异步任务结果，TTL 来自 `AsyncTask.expiredSeconds` | Redis 持久化 DAO，不迁移 |
| `SmsValidateCodeServlet` | 手机号短信发送频控，TTL 为 `1` 秒 | 验证码/频控，不迁移 |

这些用法已经位于 Redis 语义明确的入口或 infra DAO 中，后续 CrudService 缓存迁移不得顺手替换。

## 8. First Migration Chain

首条迁移链路固定选择 `Dict`。

原因：

- `DictServiceImpl` 显式开启 CrudService Redis 缓存。
- 主要缓存是按 id 读取的单对象。
- 写后失效已经固定为全量失效，迁移时无需重新设计复杂联动。
- 不涉及登录态、认证、会话、验证码或树移动。
- 相比 `Storage`，没有文件业务绑定和启用/公开状态写入的失效歧义。
- 相比 `Office`、`Menu`、`Role`、`User`，没有树结构或跨服务权限联动。

`Dict` 迁移前置条件：

- 先完成 `cache-infra-contract-design`，固定 infra 缓存支撑对象命名、包路径、key、TTL、命中、回源、回填、失效和版本策略。
- 保持既有 key 语义和查询语义不变。
- 迁移后 `DictServiceImpl` 不再覆写 `isRedisCacheEnabled()`，不再调用 `removeAllCache()` 或 `getCacheVersion()`。
- `DictServiceHolder` 不再通过 `CrudService` 公共契约读取缓存版本。

## 9. Open Items

无
