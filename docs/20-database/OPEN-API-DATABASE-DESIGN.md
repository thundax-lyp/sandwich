# OPEN API DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich 开放接口 OpenClient 主体和 OpenClient 权限的数据库表、字段映射、关系约束和持久化规则。

本文档以 `OPEN-API-REQUIREMENTS.md` 和 `OPEN-API-AUTH-DESIGN.md` 为基础。建表 SQL 见 [`../../db/schema/open.sql`](../../db/schema/open.sql)，初始化脚本见 [`../../db/data/open.sql`](../../db/data/open.sql)。

## 2. Scope

当前覆盖范围：

- `open_client`
- `open_client_permission`
- `OpenClientDO`
- `OpenClientPermissionDO`
- `OpenClientMapper`
- `OpenClientPermissionMapper`
- `OpenClientDaoImpl`
- `OpenClientPersistenceAssembler`

当前不覆盖范围：

- API KEY 和 API SECRET 主数据，固定归属 `auth_principal_identity` 和 `auth_principal_credential`。
- Open API 独立调用日志表。
- nonce 运行态存储。
- 后台 OpenClient 菜单和权限初始化数据。
- 生产数据变更脚本。

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- Open API 主体表使用 `open_` 业务域前缀。
- 独立数据库表主键数据库类型固定为 `bigint`，Java 类型固定为 `Long`。
- 独立数据库表主键由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- 枚举字段使用 `varchar` 存储。
- `ip_whitelist` 使用 JSON array 字符串保存。
- 空 `ip_whitelist` 表示不限制来源 IP。
- API SECRET 明文不得落库。
- API KEY 和 API SECRET 不保存到 `open_client` 主表。
- 业务对象变更审计固定归属 Audit 模块，不在 `open_` 表中保存通用审计字段。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- OpenClient 主表固定为 `open_client`。
- OpenClient 权限表固定为 `open_client_permission`。
- 主键字段固定为 `id`。
- OpenClient 权限表的主体字段固定为 `client_id`。
- 权限字段固定为 `permission`。
- IP 白名单字段固定为 `ip_whitelist`。
- 过期时间字段固定为 `expired_at`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `open_client` | `OpenClientDO` | `OpenClientMapper` | `OpenClient` |
| `open_client_permission` | `OpenClientPermissionDO` | `OpenClientPermissionMapper` | `OpenClientPermission` |

## 6. Table Design

### 6.1 open_client

`open_client` 保存开放接口第三方主体业务资料。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | OpenClient 主键 |
| `name` | `name` | `name` | 是 | 第三方主体名称 |
| `status` | `status` | `status` | 是 | OpenClient 状态 |
| `ip_whitelist` | `ipWhitelist` | `ipWhitelist` | 否 | IP 白名单 JSON array 字符串 |
| `expired_at` | `expiredAt` | `expiredAt` | 否 | 过期时间 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `name` 固定使用 `varchar(128)`。
- `status` 通过 `OpenClientStatus.value()` 写入。
- `status` 固定使用状态值：`ENABLED`、`DISABLED`。
- `ip_whitelist` 固定使用 JSON array 字符串保存；空值表示不限制来源 IP。
- `expired_at` 为空表示不过期。
- 本表不保存 API KEY、API SECRET 或通用审计字段。

索引：

- 主键：`pk_open_client(id)`
- 普通索引：`idx_open_client_status(status, id)`
- 普通索引：`idx_open_client_expired(expired_at, id)`

### 6.2 open_client_permission

`open_client_permission` 保存 OpenClient 到业务权限码的直接映射。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 权限映射主键 |
| `client_id` | `clientId` | `clientId` | 是 | OpenClient 主键 |
| `permission` | `permission` | `permission` | 是 | 业务权限码 |

字段规则：

- `id` 由 DAO implementation 通过 `SnowflakeIdGenerator` 生成。
- `client_id` 引用 `open_client.id`。
- `permission` 使用业务模块权限码，不使用 `open:` 前缀。
- 本表不引入 role、menu、权限树或通用审计字段。

索引：

- 主键：`pk_open_client_permission(id)`
- 联合唯一索引：`uk_open_client_permission_client_permission(client_id, permission)`
- 普通索引：`idx_open_client_permission_permission(permission, client_id)`

## 7. Relationship Rules

- 当前 OpenClient 表不强制数据库外键。
- `open_client_permission.client_id` 通过 Service 和数据库唯一约束关联 `open_client.id`。
- 一个 OpenClient 可以关联多条 OpenClientPermission。
- `client_id + permission` 必须唯一。
- 禁用 OpenClient 通过 `open_client.status` 表达，不设计逻辑删除字段。
- API KEY 通过 `auth_principal_identity.principal_type=OPEN_CLIENT` 和 `principal_id=open_client.id` 关联。
- API SECRET 通过 `auth_principal_credential.principal_type=OPEN_CLIENT` 和 `principal_id=open_client.id` 关联。

## 8. Persistence Rules

- `OpenClientDO` 固定映射 `open_client`。
- `OpenClientPermissionDO` 固定映射 `open_client_permission`。
- `OpenClientMapper` 固定保持 `public interface OpenClientMapper extends BaseMapper<OpenClientDO> {}`。
- `OpenClientPermissionMapper` 固定保持 `public interface OpenClientPermissionMapper extends BaseMapper<OpenClientPermissionDO> {}`。
- `OpenClientDaoImpl` 固定负责 `OpenClient <-> OpenClientDO`、`OpenClientPermission <-> OpenClientPermissionDO` 转换、查询条件拆解和分页。
- `OpenClientPersistenceAssembler` 只负责 Entity 与 `DO/DataObject` 字段转换。
- Service 不感知 `OpenClientDO` 或 `OpenClientPermissionDO`。
- Controller 不直接依赖 Mapper、`DO/DataObject` 或 `PersistenceAssembler`。

## 9. Query Model Rules

- OpenClient 分页固定支持按 `name` 模糊过滤和 `status` 精确过滤。
- OpenClient 分页默认按 `id desc` 排序。
- OpenClient 权限固定按 `client_id` 查询。
- OpenClient 权限列表默认按 `permission asc, id asc` 排序。
- OpenClient 权限维护固定先删除当前 `client_id` 权限，再批量写入去重后的权限集合。
- `pageNo` / `pageSize` 由 Service 校验，DAO implementation 只按已校验参数执行分页。

## 10. Open Items

无
