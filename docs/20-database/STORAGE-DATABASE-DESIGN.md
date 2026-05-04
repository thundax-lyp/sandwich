# STORAGE DATABASE DESIGN

## 1. Purpose

本文档定义 Sandwich `Storage` 模块的数据库表、字段映射、关系约束和持久化规则。

本文档以目标 `StoredObject`、`StoredObjectReference`、`MultipartUploadSession` 和 `MultipartUploadPart` 模型为准。当前仓库未提供独立建表 SQL，真实数据库 DDL 必须在上线前与本文档完成核对。

## 2. Scope

当前覆盖范围：

- `assist_storage_object`
- `assist_storage_object_reference`
- `assist_storage_multipart_upload`
- `assist_storage_multipart_upload_part`
- `StoredObjectDO`
- `StoredObjectReferenceDO`
- `MultipartUploadSessionDO`
- `MultipartUploadPartDO`
- `StoredObjectMapper`
- `StoredObjectReferenceMapper`
- `MultipartUploadSessionMapper`
- `MultipartUploadPartMapper`
- `StoredObjectDao`
- `StoredObjectReferenceDao`
- `MultipartUploadDao`
- 持久化装配器

当前不覆盖范围：

- 存储审计日志表
- 存储审计 outbox 表
- CDN 分发配置表

## 3. Database Rules

- 数据库平台以当前项目实际配置为准。
- 存储引擎优先使用 `InnoDB`。
- 字符集优先使用 `utf8mb4`。
- `StoredObjectDO.id` 是独立数据库表主键，Java 类型固定为 `String`，使用 `IdType.ASSIGN_UUID`。
- `StoredObjectReferenceDO.objectId` 映射数据库列 `object_id`。
- 对象删除使用 `object_status` 表达，不通过公开业务接口暴露逻辑删除字段。
- DAO get/list/page 查询应该排除 `DELETED` 对象，除非当前查询明确读取删除态。
- 枚举字段使用 `varchar` 存储。
- `storage_type` 固定使用 `LOCAL_FILE` 或 `OSS`。
- `object_status` 固定使用 `ACTIVE`、`DELETING`、`DELETED`。
- `reference_status` 固定使用 `UNREFERENCED`、`REFERENCED`。
- `upload_status` 固定使用 `INITIATED`、`UPLOADING`、`COMPLETED`、`ABORTED`。
- `DO/DataObject` 不暴露给 Controller 或 Service。

## 4. Naming Rules

- 存储对象主表固定为 `assist_storage_object`。
- 存储对象引用表固定为 `assist_storage_object_reference`。
- 存储对象主键列固定为 `id`。
- 对象引用表的存储对象主键列固定为 `object_id`。
- 原始文件名列固定为 `original_filename`。
- 内容类型列固定为 `content_type`。
- 引用方字段固定为 `owner_id` 和 `owner_type`。
- 底层存储类型字段固定为 `storage_type`。
- 存储桶或本地逻辑目录字段固定为 `bucket_name`。
- 底层对象键字段固定为 `object_key`。
- 派生访问端点字段固定为 `access_endpoint`。
- 对象状态字段固定为 `object_status`。
- 引用状态字段固定为 `reference_status`。
- 分片上传会话表固定为 `assist_storage_multipart_upload`。
- 分片上传分片表固定为 `assist_storage_multipart_upload_part`。

## 5. Table Mapping

| Table | DO | Mapper | Entity |
| --- | --- | --- | --- |
| `assist_storage_object` | `StoredObjectDO` | `StoredObjectMapper` | `StoredObject` |
| `assist_storage_object_reference` | `StoredObjectReferenceDO` | `StoredObjectReferenceMapper` | `StoredObjectReference` |
| `assist_storage_multipart_upload` | `MultipartUploadSessionDO` | `MultipartUploadSessionMapper` | `MultipartUploadSession` |
| `assist_storage_multipart_upload_part` | `MultipartUploadPartDO` | `MultipartUploadPartMapper` | `MultipartUploadPart` |

## 6. Table Design

### 6.1 assist_storage_object

`assist_storage_object` 保存已存储对象主数据，不保存文件二进制内容。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 存储对象主键 |
| `storage_type` | `storageType` | `storageType` | 是 | 底层存储类型 |
| `bucket_name` | `bucketName` | `bucketName` | 否 | 存储桶或本地逻辑目录 |
| `object_key` | `objectKey` | `objectKey` | 是 | 底层对象键 |
| `original_filename` | `originalFilename` | `originalFilename` | 是 | 原始文件名 |
| `content_type` | `contentType` | `contentType` | 是 | 内容类型 |
| `size` | `size` | `size` | 是 | 文件大小，字节 |
| `access_endpoint` | `accessEndpoint` | `accessEndpoint` | 否 | 派生访问端点 |
| `object_status` | `objectStatus` | `objectStatus` | 是 | 对象状态 |
| `reference_status` | `referenceStatus` | `referenceStatus` | 是 | 引用状态 |
| `create_date` | `createDate` | `createDate` | 是 | 创建时间 |
| `update_date` | `updateDate` | `updateDate` | 否 | 更新时间 |

字段规则：

- `id` 由 MyBatis-Plus `IdType.ASSIGN_UUID` 生成。
- `storage_type` 通过 `StorageType.value()` 写入。
- `object_status` 通过 `StoredObjectStatus.value()` 写入。
- `reference_status` 通过 `StoredObjectReferenceStatus.value()` 写入。
- `object_key` 在当前底层存储内必须唯一。
- `access_endpoint` 是派生访问端点，不保存临时签名 URL。

索引设计：

- 主键：`pk_assist_storage_object(id)`
- 唯一索引：`uk_assist_storage_object_key(storage_type, bucket_name, object_key)`
- 普通索引：`idx_assist_storage_object_status(object_status, reference_status, create_date)`
- 普通索引：`idx_assist_storage_object_content_type(content_type)`

### 6.2 assist_storage_object_reference

`assist_storage_object_reference` 保存业务模块对存储对象的引用关系。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `object_id` | `objectId` | `objectId` | 是 | 存储对象 ID |
| `owner_type` | `ownerType` | `ownerType` | 是 | 引用方类型 |
| `owner_id` | `ownerId` | `ownerId` | 是 | 引用方业务主键 |

字段规则：

- `object_id` 来源是 `StoredObject.id`，不生成新 UUID。
- 同一个对象允许被多个业务资源引用。
- 引用关系唯一性固定由 `object_id + owner_type + owner_id` 表达。
- `StoredObjectReferenceDO` 固定不包含创建时间、更新时间和逻辑删除字段。

索引设计：

- 联合唯一索引：`uk_assist_storage_object_reference_owner(object_id, owner_type, owner_id)`
- 普通索引：`idx_assist_storage_object_reference_owner(owner_type, owner_id)`

### 6.3 assist_storage_multipart_upload

`assist_storage_multipart_upload` 保存分片上传会话。

| Column | DO Field | Entity Field | Required | Description |
| --- | --- | --- | --- | --- |
| `id` | `id` | `id` | 是 | 会话数据库主键 |
| `upload_id` | `uploadId` | `uploadId` | 是 | 分片上传会话业务键 |
| `owner_id` | `ownerId` | `ownerId` | 是 | 上传发起人 ID |
| `owner_type` | `ownerType` | `ownerType` | 是 | 上传发起人类型 |
| `category` | `category` | `category` | 否 | 业务分类 |
| `original_filename` | `originalFilename` | `originalFilename` | 是 | 原始文件名 |
| `content_type` | `contentType` | `contentType` | 是 | 内容类型 |
| `storage_type` | `storageType` | `storageType` | 是 | 底层存储类型 |
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
- `provider_upload_id` 只保存底层存储返回的会话标识，不作为 Sandwich 对外标识。
- `upload_status` 只能写入 `INITIATED`、`UPLOADING`、`COMPLETED`、`ABORTED`。
- `uploaded_part_count` 默认值固定为 `0`。

索引设计：

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

索引设计：

- 主键：`pk_assist_storage_multipart_upload_part(id)`
- 唯一索引：`uk_assist_storage_multipart_upload_part(upload_id, part_number)`
- 普通索引：`idx_assist_storage_multipart_upload_part_upload_id(upload_id)`

## 7. Relationship Rules

- `assist_storage_object_reference.object_id` 引用 `assist_storage_object.id`。
- `assist_storage_multipart_upload_part.upload_id` 引用 `assist_storage_multipart_upload.upload_id`。
- 当前项目不强制数据库外键。
- 对象引用关系一致性由 Service 编排和数据库约束共同保证。
- 建立引用前必须确保对应 `StoredObject` 已存在且处于 `ACTIVE`。
- 清理最后一个引用后，Service 必须更新 `StoredObject.referenceStatus` 为 `UNREFERENCED`。
- 分片上传完成后，Service 必须创建 `assist_storage_object` 记录，并将对应会话状态更新为 `COMPLETED`。
- 分片上传取消后，Service 必须将对应会话状态更新为 `ABORTED`。

## 8. Persistence Rules

- `StoredObjectMapper` 固定继承 `BaseMapper<StoredObjectDO>`。
- `StoredObjectReferenceMapper` 固定继承 `BaseMapper<StoredObjectReferenceDO>`。
- `MultipartUploadSessionMapper` 固定继承 `BaseMapper<MultipartUploadSessionDO>`。
- `MultipartUploadPartMapper` 固定继承 `BaseMapper<MultipartUploadPartDO>`。
- Mapper interface 不新增注解 SQL、Mapper XML 或 SQL Provider。
- `StoredObjectDaoImpl` 固定通过 MyBatis-Plus wrapper 构造查询、更新和删除。
- 持久化装配器只负责 `Entity <-> DO` 转换。
- 持久化装配器不调用 Service、DAO 或 Mapper。
- `StoredObjectDaoImpl.insert` 写入 `StoredObjectDO` 后必须清理缓存。
- `StoredObjectDaoImpl.update` 必须清理对应对象缓存。
- `StoredObjectDaoImpl.deleteById` 必须更新对象状态并清理对应对象缓存。
- `StoredObjectDaoImpl.updateReferenceStatus` 必须清理对应对象缓存。
- 分片上传会话和分片记录的持久化实现必须放在 `sandwish-infra`。
- 底层存储端口不得直接暴露给 Controller。

## 9. Query Model Rules

目标对象查询支持以下条件：

- `storageType`
- `objectStatus`
- `referenceStatus`
- `originalFilename`
- `contentType`
- `objectKey`

目标 DAO list/page 已落库支持以下条件：

- `storageType` 转换为 `storage_type`
- `objectStatus` 转换为 `object_status`
- `referenceStatus` 转换为 `reference_status`
- `originalFilename` 模糊匹配
- `contentType` 精确匹配
- `objectKey` 精确匹配

目标 DAO list/page 排序固定为：

1. `create_date` 降序。

## 10. Open Items

无
