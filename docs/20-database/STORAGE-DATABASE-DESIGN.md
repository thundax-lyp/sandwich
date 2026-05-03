# STORAGE DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich `Storage` 模块的数据库表、字段映射、关系约束和持久化规则。

本文档以当前 `StorageDO`、`StorageBusinessDO`、`StorageDaoImpl` 和 `StoragePersistenceAssembler` 为准。当前仓库未提供独立建表 SQL，真实数据库 DDL 必须在上线前与本文档完成核对。

## 2. Scope

当前覆盖范围：

- `assist_storage`
- `assist_storage_business`
- `StorageDO`
- `StorageBusinessDO`
- `StorageMapper`
- `StorageBusinessMapper`
- `StorageDaoImpl`
- `StoragePersistenceAssembler`

当前不覆盖范围：

- 分片上传会话表
- 分片上传分片表
- 存储审计日志表
- 存储审计 outbox 表
- 对象存储供应商配置表
- CDN 分发配置表

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- `StorageDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `StorageBusinessDO.storageId` 映射数据库列 `file_id`，是共享主键，使用 `IdType.INPUT`。
- `assist_storage.del_flag` 是逻辑删除字段，`StorageDO` 不声明 `delFlag`。
- DAO list/page 查询必须追加 `del_flag = '0'` 条件。
- DAO insert 后必须写入 `del_flag = '0'`。
- 枚举字段使用 `varchar` 存储。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 存储资源主表固定为 `assist_storage`。
- 存储业务绑定表固定为 `assist_storage_business`。
- 文件资源主键列固定为 `id`。
- 业务绑定表存储资源主键列固定为 `file_id`。
- 文件扩展名列固定为 `extend_name`。
- MIME type 列固定为 `mime_type`。
- owner 字段固定为 `owner_id` 和 `owner_type`。
- 状态字段沿用历史列名 `enable_flag`。
- 可见性字段沿用历史列名 `public_flag`。
- 逻辑删除字段固定为 `del_flag`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `assist_storage` | `StorageDO` | `StorageMapper` | `Storage` |
| `assist_storage_business` | `StorageBusinessDO` | `StorageBusinessMapper` | `StorageBusiness` |

## 6. Table Design

### 6.1 assist_storage

`assist_storage` 保存文件资源元数据，不保存文件二进制内容。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 存储资源主键 |
| `name` | `name` | `name` | 是 | 文件名称，不含扩展名 |
| `extend_name` | `extendName` | `extendName` | 是 | 文件扩展名 |
| `mime_type` | `mimeType` | `mimeType` | 是 | MIME type |
| `owner_id` | `ownerId` | `ownerId` | 是 | 资源所有者 ID |
| `owner_type` | `ownerType` | `ownerType` | 是 | 资源所有者类型 |
| `enable_flag` | `enableFlag` | `status` | 是 | 资源状态 |
| `public_flag` | `publicFlag` | `visibility` | 是 | 资源可见性 |
| `priority` | `priority` | `priority` | 否 | 排序值 |
| `remarks` | `remarks` | `remarks` | 否 | 备注 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `del_flag` | 无 | 无 | 是 | 逻辑删除标记 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `enable_flag` 通过 `StorageStatus.value()` 写入。
- `public_flag` 通过 `StorageVisibility.value()` 写入。
- `owner_type` 通过 `StorageOwnerType.value()` 写入。
- `priority` 为空或小于 0 时，`StoragePersistenceAssembler` 固定转换为 `0`。
- `del_flag` 默认值固定为 `0`。

建议索引：

- 主键：`pk_assist_storage(id)`
- 普通索引：`idx_assist_storage_del_create(del_flag, create_date)`
- 普通索引：`idx_assist_storage_owner(owner_type, owner_id)`
- 普通索引：`idx_assist_storage_mime_type(mime_type)`
- 普通索引：`idx_assist_storage_status_visibility(enable_flag, public_flag)`

### 6.2 assist_storage_business

`assist_storage_business` 保存文件资源与业务对象之间的绑定关系。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `file_id` | `storageId` | `id` | 是 | 存储资源 ID |
| `business_id` | `businessId` | `businessId` | 是 | 业务对象 ID |
| `business_type` | `businessType` | `businessType` | 是 | 业务对象类型 |
| `business_params` | `businessParams` | `businessParams` | 否 | 业务扩展参数 |
| `public_flag` | `publicFlag` | `visibility` | 是 | 绑定关系可见性 |

字段规则：

- `file_id` 使用 `IdType.INPUT`，主键来源是 `Storage.id`。
- `file_id` 不生成新 UUID。
- `public_flag` 通过 `StorageVisibility.value()` 写入。
- 当前 `StorageBusinessDO` 不包含创建时间、更新时间和逻辑删除字段。

建议索引：

- 主键：`pk_assist_storage_business(file_id)`
- 普通索引：`idx_assist_storage_business_biz(business_type, business_id)`
- 普通索引：`idx_assist_storage_business_public(public_flag)`

## 7. Relationship Rules

- `assist_storage_business.file_id` 引用 `assist_storage.id`。
- 当前项目不强制数据库外键。
- 业务绑定关系一致性由 Service 编排和数据库约束共同保证。
- `StorageService.insertBusiness` 写入绑定关系前必须确保对应 `Storage` 已存在。
- `StorageService.removeBusiness` 固定按 `business_type` 和 `business_id` 清理绑定关系。
- 当前绑定表以 `file_id` 作为主键，同一个存储资源同一时间只能保留一条绑定关系。

## 8. Persistence Rules

- `StorageMapper` 固定继承 `BaseMapper<StorageDO>`。
- `StorageBusinessMapper` 固定继承 `BaseMapper<StorageBusinessDO>`。
- Mapper interface 不新增注解 SQL、Mapper XML 或 SQL Provider。
- `StorageDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询、更新和删除。
- `StoragePersistenceAssembler` 只负责 `Entity <-> DO` 转换。
- `StoragePersistenceAssembler` 不调用 Service、DAO 或 Mapper。
- `StorageDaoImpl.insert` 写入 `StorageDO` 后必须回填 `del_flag = '0'` 并清理缓存。
- `StorageDaoImpl.update` 必须清理对应资源缓存。
- `StorageDaoImpl.deleteById` 必须清理对应资源缓存。
- `StorageDaoImpl.updateStatus` 和 `StorageDaoImpl.updateVisibility` 必须清理对应资源缓存。

## 9. Query Model Rules

`StorageQuery` 当前支持以下条件：

- `mimeType`
- `businessId`
- `businessType`
- `ownerId`
- `ownerType`
- `status`
- `visibility`
- `name`
- `remarks`

当前 DAO list/page 已落库支持以下条件：

- `mimeType`
- `ownerId`
- `ownerType`
- `status` 转换为 `enable_flag`
- `visibility` 转换为 `public_flag`
- `name` 模糊匹配
- `remarks` 模糊匹配
- `del_flag = '0'`

当前 DAO list/page 排序固定为：

1. `create_date` 降序。
2. `priority` 升序。

当前差异：

- `businessId` 和 `businessType` 已存在于 `StorageQuery`，但当前 list/page 查询未 join `assist_storage_business`。
- `listMimeTypes` 当前按 `mime_type` 分组排序，未追加 `del_flag = '0'` 条件。
- `listBusinessTypes` 当前按 `business_type` 分组排序，绑定表没有逻辑删除字段。

## 10. Open Items

- 补齐真实数据库 DDL，并与本文档字段、索引和约束逐项核对。
- 明确 `assist_storage_business` 是否允许一个文件绑定多个业务对象；若允许，必须调整主键或增加联合唯一约束。
- 明确 `assist_storage_business` 是否需要 `create_date`、`update_date` 和 `del_flag`。
- 明确 `StorageQuery.businessId` 和 `StorageQuery.businessType` 是否必须落到 list/page 查询。
- 明确 `listMimeTypes` 是否必须过滤 `del_flag = '0'`。
- 明确删除语义是 MyBatis-Plus 物理删除、逻辑删除插件，还是状态删除。
