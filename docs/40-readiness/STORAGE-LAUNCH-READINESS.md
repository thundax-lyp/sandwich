# STORAGE LAUNCH READINESS

## 1. Purpose

本文档只保留 Sandwich `Storage` 上线前的验收门槛。

本文档不重复需求和数据库设计内容。

## 2. Scope

覆盖范围：

- `sandwish-biz/src/main/java/com/github/thundax/modules/storage`
- `sandwish-infra/src/main/java/com/github/thundax/modules/storage`
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage`
- `sandwish-front-api/src/main/java/com/github/thundax/modules/storage`
- `sandwish-biz/src/test/java/com/github/thundax/modules/storage`
- `sandwish-infra/src/test/java/com/github/thundax/modules/storage`

## 3. Release Gates

### 3.1 Contract Gate

上线前必须确认：

- 后台和前台存储访问路径与当前 Controller / Converter 实现一致
- 文档中的接口路径、字段名、状态值与当前代码一致
- `StorageResponse`、`StorageQuery`、`StorageStatus`、`StorageVisibility`、`StorageOwnerType` 的公开语义一致
- 分片上传初始化、上传分片、完成和取消接口与需求文档一致
- `LOCAL_FILE` 和 `OSS` 后端类型的公开配置与运行时装配一致

### 3.2 Data Gate

上线前必须确认：

- 存储相关数据库表与 `StoredObjectDO`、`StoredObjectReferenceDO` 字段一致
- 分片上传相关数据库表与 `MultipartUploadSessionDO`、`MultipartUploadPartDO` 字段一致
- `StoredObjectDO` 中底层存储定位字段与数据库 `bucket_name`、`object_key`、`size`、`access_endpoint` 一致
- `StoragePersistenceAssembler` 中 `Entity <-> DO` 字段转换完整
- 枚举字段写入值与数据库 `varchar` 存储值一致
- 共享主键或业务绑定关系符合 `DATABASE-RULES.md`

### 3.3 Lifecycle Gate

上线前必须确认：

- 普通上传能生成 `Storage`
- 普通上传能通过 `LOCAL_FILE` 后端保持当前行为
- 分片上传能生成 `MultipartUploadSession` 和 `MultipartUploadPart`
- 分片上传完成后能生成 `Storage`
- 分片上传取消后能释放底层后端临时资源
- 上传后能建立业务绑定关系
- 私有资源只能由 owner 访问
- 删除后对象进入已删除语义，且不能再作为可用对象继续使用
- 缓存失效能覆盖新增、更新、删除和业务绑定变化

### 3.4 Access Gate

上线前必须确认：

- 后台存储文件访问可用
- 前台存储文件访问可用
- 文件不存在时返回明确的 404 或等价错误
- MIME type 与文件内容输出行为一致
- 上传允许的内容类型与 `StorageUtils` 当前白名单一致
- 文件访问不直接依赖底层存储后端实现细节

### 3.5 Query Gate

上线前必须确认：

- 详情查询可用
- 分页查询可用
- MIME type 列表查询可用
- `status`、`visibility`、`ownerType`、`ownerId` 等筛选字段与 Service / DAO 查询实现一致

### 3.6 Test Gate

上线前至少应覆盖：

- 普通上传主流程
- 查询主流程
- 分页筛选主流程
- 删除流程
- 底层存储后端选择与 `LOCAL_FILE` 写入流程
- 分片上传初始化、上传分片、完成和取消流程
- `StoragePersistenceAssembler` 字段转换
- `MultipartUploadSession` 与 `MultipartUploadPart` 字段转换
- 私有资源 owner 访问边界
- 缓存失效边界

## 4. Current Rule

- 若存在阻断上线的问题，应直接补到本文件，不要散落到其他文档
- 已关闭问题从本文件移除
- 本文件始终只保留“当前仍需要确认的事项”

## 5. Open Items

无
