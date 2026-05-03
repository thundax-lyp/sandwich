# STORAGE DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich `Storage` 模块的数据库表、字段映射、关系约束和持久化规则。

本文档以当前 `StorageDO`、`StorageBusinessDO`、`StorageDaoImpl` 和 `StoragePersistenceAssembler` 为基础，并定义已确认要引入的分片上传与底层存储后端字段。当前仓库未提供独立建表 SQL，真实数据库 DDL 必须在上线前与本文档完成核对。

## 2. Scope

当前覆盖范围：

- `assist_storage`
- `assist_storage_business`
- `assist_storage_multipart_upload`
- `assist_storage_multipart_upload_part`
- `StorageDO`
- `StorageBusinessDO`
- `MultipartUploadSessionDO`
- `MultipartUploadPartDO`
- `StorageMapper`
- `StorageBusinessMapper`
- `MultipartUploadSessionMapper`
- `MultipartUploadPartMapper`
- `StorageDaoImpl`
- `StoragePersistenceAssembler`

当前不覆盖范围：

- 存储审计日志表
- 存储审计 outbox 表
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
- `storage_type` 固定使用 `LOCAL_FILE` 或 `OSS`。
- `upload_status` 固定使用 `INITIATED`、`UPLOADING`、`COMPLETED`、`ABORTED`。
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
- 底层存储后端类型字段固定为 `storage_type`。
- 存储桶或本地逻辑目录字段固定为 `bucket_name`。
- 底层对象键字段固定为 `object_key`。
- 访问端点字段固定为 `access_endpoint`。
- 分片上传会话表固定为 `assist_storage_multipart_upload`。
- 分片上传分片表固定为 `assist_storage_multipart_upload_part`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `assist_storage` | `StorageDO` | `StorageMapper` | `Storage` |
| `assist_storage_business` | `StorageBusinessDO` | `StorageBusinessMapper` | `StorageBusiness` |
| `assist_storage_multipart_upload` | `MultipartUploadSessionDO` | `MultipartUploadSessionMapper` | `MultipartUploadSession` |
| `assist_storage_multipart_upload_part` | `MultipartUploadPartDO` | `MultipartUploadPartMapper` | `MultipartUploadPart` |

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
| `storage_type` | `storageType` | `storageType` | 是 | 底层存储后端类型 |
| `bucket_name` | `bucketName` | `bucketName` | 否 | 存储桶或本地逻辑目录 |
| `object_key` | `objectKey` | `objectKey` | 是 | 底层对象键 |
| `size` | `size` | `size` | 是 | 文件大小，字节 |
| `access_endpoint` | `accessEndpoint` | `accessEndpoint` | 是 | 文件访问端点 |
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
- `storage_type` 通过 `StorageBackendType.value()` 写入。
- `object_key` 在当前存储后端内必须唯一。
- `access_endpoint` 是访问端点快照，不保存临时签名 URL。
- `priority` 为空或小于 0 时，`StoragePersistenceAssembler` 固定转换为 `0`。
- `del_flag` 默认值固定为 `0`。

建议索引：

- 主键：`pk_assist_storage(id)`
- 普通索引：`idx_assist_storage_del_create(del_flag, create_date)`
- 普通索引：`idx_assist_storage_owner(owner_type, owner_id)`
- 唯一索引：`uk_assist_storage_object_key(storage_type, bucket_name, object_key)`
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

### 6.3 assist_storage_multipart_upload

`assist_storage_multipart_upload` 保存分片上传会话。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 会话数据库主键 |
| `upload_id` | `uploadId` | `uploadId` | 是 | 分片上传会话业务键 |
| `owner_id` | `ownerId` | `ownerId` | 是 | 上传发起人 ID |
| `owner_type` | `ownerType` | `ownerType` | 是 | 上传发起人类型 |
| `business_type` | `businessType` | `businessType` | 否 | 业务对象类型 |
| `original_filename` | `originalFilename` | `originalFilename` | 是 | 原始文件名 |
| `mime_type` | `mimeType` | `mimeType` | 是 | MIME type |
| `storage_type` | `storageType` | `storageType` | 是 | 底层存储后端类型 |
| `bucket_name` | `bucketName` | `bucketName` | 否 | 存储桶或本地逻辑目录 |
| `object_key` | `objectKey` | `objectKey` | 是 | 最终对象键 |
| `provider_upload_id` | `providerUploadId` | `providerUploadId` | 否 | 底层存储供应商分片会话标识 |
| `total_size` | `totalSize` | `totalSize` | 是 | 文件总大小，字节 |
| `part_size` | `partSize` | `partSize` | 是 | 固定分片大小，字节 |
| `uploaded_part_count` | `uploadedPartCount` | `uploadedPartCount` | 是 | 已上传分片数 |
| `upload_status` | `uploadStatus` | `uploadStatus` | 是 | 分片上传状态 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |
| `completed_date` | `completedDate` | `completedDate` | 否 | 完成时间 |
| `aborted_date` | `abortedDate` | `abortedDate` | 否 | 取消时间 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `upload_id` 由 Service 生成，作为对外会话业务键。
- `provider_upload_id` 只保存底层后端返回的会话标识，不作为 Sandwich 对外标识。
- `upload_status` 只能写入 `INITIATED`、`UPLOADING`、`COMPLETED`、`ABORTED`。
- `uploaded_part_count` 默认值固定为 `0`。

建议索引：

- 主键：`pk_assist_storage_multipart_upload(id)`
- 唯一索引：`uk_assist_storage_multipart_upload_upload_id(upload_id)`
- 普通索引：`idx_assist_storage_multipart_upload_object_key(storage_type, bucket_name, object_key)`
- 普通索引：`idx_assist_storage_multipart_upload_owner(owner_type, owner_id, upload_status, create_date)`

### 6.4 assist_storage_multipart_upload_part

`assist_storage_multipart_upload_part` 保存分片上传的单片记录。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 分片记录数据库主键 |
| `upload_id` | `uploadId` | `uploadId` | 是 | 分片上传会话业务键 |
| `part_number` | `partNumber` | `partNumber` | 是 | 分片序号 |
| `etag` | `etag` | `etag` | 是 | 分片校验标识 |
| `size` | `size` | `size` | 是 | 分片大小，字节 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `part_number` 从 `1` 开始。
- 同一 `upload_id` 内 `part_number` 不得重复。

建议索引：

- 主键：`pk_assist_storage_multipart_upload_part(id)`
- 唯一索引：`uk_assist_storage_multipart_upload_part(upload_id, part_number)`
- 普通索引：`idx_assist_storage_multipart_upload_part_upload_id(upload_id)`

## 7. Relationship Rules

- `assist_storage_business.file_id` 引用 `assist_storage.id`。
- `assist_storage_multipart_upload_part.upload_id` 引用 `assist_storage_multipart_upload.upload_id`。
- 当前项目不强制数据库外键。
- 业务绑定关系一致性由 Service 编排和数据库约束共同保证。
- `StorageService.insertBusiness` 写入绑定关系前必须确保对应 `Storage` 已存在。
- `StorageService.removeBusiness` 固定按 `business_type` 和 `business_id` 清理绑定关系。
- 当前绑定表以 `file_id` 作为主键，同一个存储资源同一时间只能保留一条绑定关系。
- 分片上传完成后，Service 必须创建 `assist_storage` 记录，并将对应会话状态更新为 `COMPLETED`。
- 分片上传取消后，Service 必须将对应会话状态更新为 `ABORTED`。

## 8. Persistence Rules

- `StorageMapper` 固定继承 `BaseMapper<StorageDO>`。
- `StorageBusinessMapper` 固定继承 `BaseMapper<StorageBusinessDO>`。
- `MultipartUploadSessionMapper` 固定继承 `BaseMapper<MultipartUploadSessionDO>`。
- `MultipartUploadPartMapper` 固定继承 `BaseMapper<MultipartUploadPartDO>`。
- Mapper interface 不新增注解 SQL、Mapper XML 或 SQL Provider。
- `StorageDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询、更新和删除。
- `StoragePersistenceAssembler` 只负责 `Entity <-> DO` 转换。
- `StoragePersistenceAssembler` 不调用 Service、DAO 或 Mapper。
- `StorageDaoImpl.insert` 写入 `StorageDO` 后必须回填 `del_flag = '0'` 并清理缓存。
- `StorageDaoImpl.update` 必须清理对应资源缓存。
- `StorageDaoImpl.deleteById` 必须清理对应资源缓存。
- `StorageDaoImpl.updateStatus` 和 `StorageDaoImpl.updateVisibility` 必须清理对应资源缓存。
- 分片上传会话和分片记录的持久化实现必须放在 `sandwish-infra`。
- 底层存储后端适配不得直接暴露给 Controller。

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
- `businessId` 通过 `assist_storage_business.business_id` 过滤 `assist_storage.id`
- `businessType` 通过 `assist_storage_business.business_type` 过滤 `assist_storage.id`
- `name` 模糊匹配
- `remarks` 模糊匹配
- `del_flag = '0'`

当前 DAO list/page 排序固定为：

1. `create_date` 降序。
2. `priority` 升序。

当前 DAO 列表类查询规则：

- `businessId` 和 `businessType` 存在任一条件时，先从 `assist_storage_business` 查询 `file_id`，再过滤 `assist_storage.id`。
- `listMimeTypes` 固定追加 `del_flag = '0'` 条件。
- `listBusinessTypes` 当前按 `business_type` 分组排序，绑定表没有逻辑删除字段。

## 10. Open Items

- 补齐真实数据库 DDL，并与本文档字段、索引和约束逐项核对。
- 明确 `assist_storage_business` 是否允许一个文件绑定多个业务对象；若允许，必须调整主键或增加联合唯一约束。
- 明确 `assist_storage_business` 是否需要 `create_date`、`update_date` 和 `del_flag`。
- 明确删除语义是 MyBatis-Plus 物理删除、逻辑删除插件，还是状态删除。
- 补齐 `StorageDO` 的 `storageType`、`bucketName`、`objectKey`、`size` 和 `accessEndpoint` 字段。
- 补齐 `MultipartUploadSessionDO`、`MultipartUploadPartDO`、Mapper、DAO 和 assembler。
- 明确 `OSS` 配置是否单独建表；当前数据库设计不新增对象存储供应商配置表。
