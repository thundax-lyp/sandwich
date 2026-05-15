# STORAGE REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich `Storage` 模块的业务需求边界。

`Storage` 负责统一管理已存储对象、对象引用关系、分片上传会话、底层存储适配和对象生命周期。文件二进制内容不入库，必须通过 `common-oss` 提供的本地文件或 S3 对象存储客户端保存。

## 2. Scope

当前覆盖范围：

- 后台普通对象上传。
- 大文件分片上传。
- 存储对象分页查询。
- 存储对象内容读取。
- 存储对象删除。
- 存储对象引用建立和清理。
- 基于 `common-oss` 的本地文件和 S3 底层存储适配。
- 存储对象状态和引用状态维护。

当前不覆盖范围：

- 前台普通对象上传接口。
- CDN 分发。
- 图片裁剪、压缩和水印。
- 视频转码。
- 在线预览转换。
- 文件安全扫描。
- 存储审计日志。
- 存储审计 outbox。

## 3. Bounded Context

`Storage` 是共享基础设施能力，供后台、前台和其他业务模块复用。

`Storage` 只表达存储对象本身、对象内容和对象引用状态，不表达具体业务单据规则。业务模块需要使用文件时，只保存稳定对象标识，并通过 `StoredObjectReference` 建立或清理引用关系。

文件二进制内容由入口层接收，由 Storage Service 编排写入底层存储端口；核心对象主数据和引用关系由 `sandwish-biz` 的 Storage Service 管理，持久化读写由 `sandwish-infra` 实现。

## 4. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/storage`
  - 定义 `StoredObject`、`StoredObjectReference`、`MultipartUploadSession`、`MultipartUploadPart`、枚举、Storage Service、Multipart Upload Service、DAO interface 和查询对象。
- `sandwish-infra/src/main/java/com/github/thundax/modules/storage`
  - 实现 `StoredObjectDao`、`StoredObjectReferenceDao`、`MultipartUploadDao`，并通过 `StoredObjectStore` 适配 `common-oss` 对象存储客户端。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/StorageController.java`
  - 提供后台上传、分页、内容读取、删除和引用管理接口。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/helper/StorageUploadRequestHelper.java`
  - 封装入口层 multipart 请求校验、文件元数据转换和 Storage Service 上传命令组装，供后台通用上传入口和业务专用上传入口复用。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/MultipartUploadController.java`
  - 提供后台分片上传初始化、分片上传、完成和取消接口。
- `sandwish-front-api`
  - 当前不提供前台 Storage Controller；前台业务需要文件能力时，通过业务 Service 复用 Storage Service，不复制后台上传入口。
- `sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java`
  - 装配管理端 `StoredObjectStore`。
- `sandwish-front-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java`
  - 装配前台 `StoredObjectStore`。

## 5. Core Business Objects

### 5.1 StoredObject

`StoredObject` 是已存储对象主数据。

核心字段：

- `id`：存储对象 ID，使用 `EntityId`。
- `name`：文件基础名。
- `extendName`：文件扩展名。
- `mimeType`：内容 MIME 类型。
- `ownerId`：上传或持有方 ID。
- `ownerType`：上传或持有方类型。
- `bucketName`：存储桶或本地逻辑目录。
- `objectKey`：底层对象键。
- `originalFilename`：原始文件名；未显式提供时由 `name + extendName` 派生。
- `contentType`：内容类型；未显式提供时使用 `mimeType`。
- `size`：文件大小，字节。
- `accessEndpoint`：派生访问端点。
- `objectStatus`：对象状态。
- `referenceStatus`：引用状态。

说明：

- `accessEndpoint` 只表达 Storage 派生的访问端点，不作为业务模块主数据。
- 业务模块不得保存 `bucketName`、`objectKey` 或物理路径。

### 5.2 StoredObjectReference

`StoredObjectReference` 是业务模块对存储对象的引用关系。

核心字段：

- `objectId`：存储对象 ID。
- `ownerId`：引用方业务主键。
- `ownerType`：引用方类型。
- `ownerParams`：引用方附加参数。
- `referenceStatus`：引用状态。

说明：

- 同一 `objectId + ownerType + ownerId` 引用关系应该幂等。
- 引用关系只表达“谁正在引用该对象”，不替代业务模块自身规则。

### 5.3 StorageOwnerType

`StorageOwnerType` 固定表达存储对象上传、持有或引用方类型。

固定值：

- `USER`：后台用户。
- `MEMBER`：前台会员。
- `SUBMISSION`：提交内容。

### 5.4 StoredObjectStatus

`StoredObjectStatus` 固定表达存储对象生命周期状态。

固定值：

- `ACTIVE`
- `DELETING`
- `DELETED`

### 5.5 StoredObjectReferenceStatus

`StoredObjectReferenceStatus` 固定表达存储对象引用状态。

固定值：

- `UNREFERENCED`
- `REFERENCED`

### 5.6 底层存储配置

底层存储类型由运行时 `StoredObjectStore` 和配置文件决定，不作为业务字段持久化，也不进入 `StoredObject` 或 Service Command。

### 5.7 MultipartUploadSession

`MultipartUploadSession` 是分片上传会话对象。

核心字段：

- `id`：会话数据库主键。
- `uploadId`：分片上传会话业务键。
- `ownerId`：上传发起人 ID。
- `ownerType`：上传发起人类型。
- `businessType`：业务分类。
- `originalFilename`：原始文件名。
- `mimeType`：内容 MIME 类型。
- `bucketName`：存储桶或本地逻辑目录。
- `objectKey`：底层对象键。
- `providerUploadId`：底层存储供应商分片会话标识。
- `totalSize`：文件总大小。
- `partSize`：固定分片大小。
- `uploadedPartCount`：已上传分片数。
- `uploadStatus`：分片上传状态。
- `completedDate`：完成时间。
- `abortedDate`：取消时间。

### 5.8 MultipartUploadPart

`MultipartUploadPart` 是分片上传的单片记录。

核心字段：

- `id`：分片记录数据库主键。
- `uploadId`：分片上传会话业务键。
- `partNumber`：分片序号。
- `etag`：分片校验标识。
- `size`：分片大小。

### 5.9 MultipartUploadStatus

`MultipartUploadStatus` 固定表达分片上传会话状态。

固定值：

- `INITIATED`
- `UPLOADING`
- `COMPLETED`
- `ABORTED`

## 6. Global Constraints

- Storage Service 是业务流程入口，Controller 不直接访问 DAO / Mapper。
- DAO interface 只定义持久化访问契约，不承载 HTTP 适配。
- 底层存储端口固定下沉到 infra，实际读写通过 `common-oss` 的 `ObjectStorageClient` 完成，不作为业务接口模型暴露。
- Storage 当前公开通用上传入口固定在后台 API；业务模块可以按自身权限边界提供专用上传入口，并复用 `StorageUploadRequestHelper` 和 `sandwish-biz` 的 Storage Service。
- 业务专用上传入口不得绕过 Storage Service，不得直接访问 `StoredObjectStore` 或 DAO。
- 前台 API 当前只装配底层存储能力，不开放通用上传 Controller。
- 前台后续需要上传时，必须先沉淀明确业务资源和权限边界，再新增前台业务专用接口，并复用 `sandwish-biz` 的 Storage Service。
- 公开 API 路径应该是 REST resource。
- 公开 API、Response、数据库主数据和业务模块不得固定暴露 `/servlet/...`。
- 其他业务模块只能保存 `objectId` 或自身语义包装后的稳定对象标识。
- 其他业务模块不得保存底层 `objectKey`、物理路径或入口实现路径。
- 分片上传会话必须由 `uploadId` 唯一标识。
- 分片记录必须按 `uploadId + partNumber` 唯一约束。
- 审计日志和 outbox 固定不纳入 Sandwich `Storage` 当前实现。
- 前后台入口不得复制业务规则；共享规则必须进入 `sandwish-biz`。
- OSS 运行配置固定由 `sandwish.oss` 配置树提供，并允许通过 `SANDWISH_OSS_*` 环境变量注入。
- `sandwish.oss.type=local` 时使用本地文件存储，必填运行含义为 `sandwish.oss.local.root-path`；`sandwish.oss.local.location-prefix` 用于派生访问端点。
- `sandwish.oss.type=s3` 时使用 S3 API 存储，必填运行含义为 `sandwish.oss.s3.bucket`、`sandwish.oss.s3.access-key` 和 `sandwish.oss.s3.secret-key`；`endpoint`、`region`、`location-prefix` 和 `path-style-access` 按部署环境配置。
- 本地开发和自动化测试默认使用 `local`；S3 行为通过 `S3ObjectStorageClient` 单元测试和 Docker 部署样例中的 MinIO 配置替身覆盖。

## 7. REST Resources

Storage 公开入口固定使用资源型路径。

- 上传对象：`POST /api/storage/object/upload`
- 分片初始化：`POST /api/storage/multipart-upload`
- 上传分片：`POST /api/storage/multipart-upload/{uploadId}/parts`
- 完成分片：`POST /api/storage/multipart-upload/{uploadId}/complete`
- 取消分片：`POST /api/storage/multipart-upload/{uploadId}/abort`
- 读取对象内容：`GET /api/storage/object/{objectId}/content`
- 删除对象：`POST /api/storage/object/delete`

## 8. Functional Requirements

### 8.1 普通上传

- 上传接口固定接收 `multipart/form-data`。
- 上传请求不是 multipart 请求时返回上传错误响应。
- 文件为空时返回上传错误响应。
- 文件后缀不在允许列表内时返回上传错误响应。
- 上传成功后必须通过底层存储端口写入对象内容。
- 上传成功后必须生成 `StoredObject`。
- `StoredObject.objectStatus` 初始值固定为 `ACTIVE`。
- `StoredObject.referenceStatus` 初始值固定为 `UNREFERENCED`。
- 上传响应必须返回对象 ID、原始文件名、contentType、size 和对象内容 REST 资源路径。

### 8.2 对象内容读取

- 内容读取接口固定通过 `objectId` 定位对象。
- 对象不存在时返回 404 或等价错误。
- 底层对象不存在时返回 404 或等价错误。
- 输出内容时必须设置 `Content-Type` 为 `StoredObject.contentType`。
- 内容读取不得要求调用方感知底层存储类型。

### 8.3 查询

- 分页查询必须支持 `contentType`、`ownerId`、`ownerType`、`objectStatus`、`referenceStatus`、`referenceOwnerId`、`referenceOwnerType`、`originalFilename` 和 `remarks` 筛选。
- Service 列表查询必须使用查询对象表达过滤条件。
- Service 分页查询必须使用查询对象表达过滤条件，使用 `PageQuery` 表达分页窗口，并返回 `PageResult<T>`。
- Service 写入口固定使用业务动作名并接收 `*Command`。
- 管理端可以查询所有对象元数据。
- 业务模块按对象 ID 查询时只能获取稳定对象信息，不获取底层存储实现细节。

### 8.4 引用管理

- 建立引用固定使用 `objectId + ownerType + ownerId`。
- 建立引用前必须确认 `StoredObject` 存在且处于 `ACTIVE`。
- 同一引用关系重复建立应该幂等。
- 建立至少一条引用后，`StoredObject.referenceStatus` 必须进入 `REFERENCED`。
- 清理最后一个引用后，`StoredObject.referenceStatus` 必须进入 `UNREFERENCED`。
- 存在引用的对象不得被业务流程误判为可直接清理。

### 8.5 删除

- 删除前必须确认 `StoredObject` 存在。
- 删除流程由 Storage 负责调用底层存储删除对象。
- 删除完成后 `StoredObject.objectStatus` 必须进入 `DELETED`。
- 已删除对象不得再建立新引用。

### 8.6 分片上传

- 分片上传流程由 `MultipartUploadService` 承载，不放入 `StorageService`。
- 初始化分片上传时必须创建 `MultipartUploadSession`。
- 一个分片上传会话必须由 `uploadId` 唯一标识。
- 上传分片时必须校验会话存在且未完成、未取消。
- 每个分片必须记录 `uploadId`、`partNumber`、`etag` 和 `size`。
- 同一会话内 `partNumber` 不得重复。
- 完成分片上传时必须校验已上传分片满足合并条件。
- 完成分片上传时必须通过底层存储端口合并对象内容。
- 完成分片上传后必须生成 `StoredObject`。
- 完成分片上传后必须把会话状态改为 `COMPLETED`。
- 取消分片上传后必须把会话状态改为 `ABORTED`，并释放底层后端临时资源。

## 9. Key Flows

### 9.1 上传流程

1. Controller 接收 multipart 请求。
2. Controller 完成入口参数校验。
3. Service 调用底层存储端口写入对象内容。
4. 底层存储端口返回 `bucketName`、`objectKey`、`size` 和 `contentType`。
5. Service 创建 `StoredObject` 主数据。
6. Controller 组装上传响应。

### 9.2 内容读取流程

1. Controller 接收 `GET /api/storage/object/{objectId}/content`。
2. Service 读取 `StoredObject` 主数据。
3. Service 校验对象状态。
4. Service 调用底层存储端口读取对象内容。
5. Controller 按 `StoredObject.contentType` 输出内容。

### 9.3 引用管理流程

1. 业务模块完成自身业务校验。
2. 业务模块调用 Storage 建立对象引用。
3. Storage 写入 `StoredObjectReference`。
4. Storage 刷新 `StoredObject.referenceStatus`。
5. 业务模块清理引用时调用 Storage 删除引用关系。
6. Storage 在最后一个引用被清理后刷新 `StoredObject.referenceStatus`。

### 9.4 分片上传流程

1. `MultipartUploadController` 接收初始化分片上传请求。
2. `MultipartUploadService` 创建 `MultipartUploadSession` 并初始化底层后端分片会话。
3. `MultipartUploadController` 按 `uploadId` 接收分片上传请求。
4. `MultipartUploadService` 写入底层分片并记录 `MultipartUploadPart`。
5. `MultipartUploadController` 接收完成分片上传请求。
6. `MultipartUploadService` 调用底层存储端口完成合并。
7. `MultipartUploadService` 创建 `StoredObject` 并将会话状态改为 `COMPLETED`。

## 10. Non-Functional Requirements

- 普通上传、查询、删除、引用管理和分片上传必须有 Service 层测试覆盖。
- 持久化装配器字段转换必须有测试覆盖。
- 内容读取不存在对象和不存在底层内容时必须稳定返回错误，不输出空内容。
- 缓存失效必须覆盖新增、更新、删除和引用状态变化。
- 上传后缀白名单和 contentType 输出行为必须在上线前人工确认。
- 数据库字段、枚举持久化值和 Response 对外字段必须保持一致。
- 分片上传必须覆盖初始化、上传分片、完成、取消、重复分片和非法状态校验测试。
- 底层存储端口必须覆盖 `LOCAL_FILE` 实现测试；`OSS` 后端至少保留可替换接口和配置装配测试。

## 11. Open Items

无
