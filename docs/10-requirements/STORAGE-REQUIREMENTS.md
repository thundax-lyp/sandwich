# STORAGE REQUIREMENTS

## 1. Purpose

本文档定义 Sandwich `Storage` 模块的业务需求边界。

`Storage` 负责统一管理文件元数据、文件访问路径、业务绑定关系和资源生命周期。文件二进制内容当前固定落在本地存储路径，数据库只保存元数据和绑定关系。

## 2. Scope

当前覆盖范围：

- 后台普通文件上传
- 后台存储资源分页查询
- 后台存储资源预览
- 后台存储资源删除
- 前后台文件访问 Servlet
- 文件元数据管理
- 文件与业务对象绑定
- 存储资源状态和可见性维护

当前不覆盖范围：

- 分片上传
- 断点续传
- 对象存储供应商抽象
- CDN 分发
- 图片裁剪、压缩和水印
- 视频转码
- 在线预览转换
- 文件安全扫描
- 存储审计日志
- 存储审计 outbox

## 3. Bounded Context

`Storage` 是共享业务能力，供后台和前台入口复用。

`Storage` 只表达文件资源本身，不表达具体业务单据的业务规则。业务单据需要引用文件时，通过 `StorageBusiness` 建立绑定关系。

文件二进制内容由入口模块完成接收和输出适配，核心元数据和绑定关系由 `sandwish-biz` 的 `StorageService` 管理，持久化读写由 `sandwish-infra` 实现。

## 4. Module Mapping

- `sandwish-biz/src/main/java/com/github/thundax/modules/storage`
  - 定义 `Storage`、`StorageBusiness`、枚举、`StorageService`、`StorageDao` 和查询对象。
- `sandwish-infra/src/main/java/com/github/thundax/modules/storage`
  - 实现 `StorageDao`，维护 `DO`、`Mapper` 和持久化转换。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/StorageController.java`
  - 提供后台上传、分页、预览、删除和业务类型树接口。
- `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage`
  - 提供后台文件 URL 转换、本地文件定位和 Servlet 输出。
- `sandwish-front-api/src/main/java/com/github/thundax/modules/storage`
  - 提供前台文件 URL 转换、本地文件定位和 Servlet 输出。

## 5. Core Business Objects

### 5.1 Storage

`Storage` 是文件资源元数据对象。

核心字段：

- `id`：存储资源 ID，使用 `EntityId`。
- `name`：文件名称，不包含扩展名。
- `extendName`：文件扩展名。
- `mimeType`：文件 MIME type。
- `ownerId`：资源所有者 ID。
- `ownerType`：资源所有者类型。
- `status`：资源状态。
- `visibility`：资源可见性。
- `priority`：排序值。
- `remarks`：备注。
- `createDate`：创建时间。
- `updateDate`：更新时间。

### 5.2 StorageBusiness

`StorageBusiness` 是文件与业务对象的绑定关系。

核心字段：

- `id`：绑定的存储资源 ID。
- `businessId`：业务对象 ID。
- `businessType`：业务对象类型。
- `businessParams`：业务侧扩展参数。
- `visibility`：绑定关系可见性。

### 5.3 StorageStatus

`StorageStatus` 固定表达存储资源生命周期状态。已删除资源不得再作为可用资源参与查询、预览或业务绑定。

### 5.4 StorageVisibility

`StorageVisibility` 固定表达资源可见性。公开资源允许直接访问，私有资源必须通过 owner 边界控制访问。

### 5.5 StorageOwnerType

`StorageOwnerType` 固定表达资源所有者类型。后台上传当前以用户作为 owner。

## 6. Global Constraints

- `StorageService` 是业务流程入口，Controller 不直接访问 DAO / Mapper。
- `StorageDao` 只定义持久化访问契约，不承载 HTTP 适配。
- `StorageConverter` 只负责 URL、`Storage` 和本地文件路径之间的入口适配。
- 本地文件路径由 `VltavaProperties.UploadProperties` 提供。
- 上传允许后缀由 `VltavaProperties.UploadProperties.allowSuffix` 控制。
- 文件实际 MIME type 使用上传文件的 `contentType` 记录。
- 文件访问 URL 固定使用 `servletPath + storage.getFileName()` 生成。
- 文件本地路径固定使用 `storagePath + storage.getPathName()` 定位。
- 前后台入口不得复制业务规则；共享规则必须进入 `sandwish-biz`。

## 7. Functional Requirements

### 7.1 普通上传

- 后台上传接口固定接收 `multipart/form-data`。
- 上传请求不是 `MultipartHttpServletRequest` 时返回上传错误响应。
- 文件为空时返回上传错误响应。
- 文件后缀不在允许列表内时返回上传错误响应。
- 上传时必须生成 `Storage` 元数据。
- 上传时必须设置 `ownerType` 和 `ownerId`。
- 上传成功后必须保存本地文件并写入 `Storage` 元数据。
- 上传响应必须返回 `id`、`name`、`extendName`、`mimeType` 和 `url`。

### 7.2 文件访问

- 后台预览接口固定通过 `id` 和 `extendName` 定位资源。
- 前后台 Servlet 固定通过请求 URI 解析资源 ID。
- 资源不存在时返回 404 或等价错误。
- 本地文件不存在时返回 404 或等价错误。
- 输出文件时必须设置 `Content-Type` 为 `Storage.mimeType`。

### 7.3 查询

- 后台分页查询必须支持 `mimeType`、`status`、`visibility`、`name` 和 `remarks` 筛选。
- `StorageService.list` 支持按 `StorageQuery` 查询资源列表。
- `StorageService.page` 支持按 `StorageQuery` 和 `Page` 查询分页结果。
- `StorageService.listMimeTypes` 返回当前存储资源中的 MIME type 列表。
- `StorageService.listBusinessTypes` 返回当前绑定关系中的业务类型列表。

### 7.4 删除

- 删除前必须确认待删除 `Storage` 存在。
- 批量删除必须以 `Storage.id` 列表作为 Service 入参。
- 删除语义必须让资源不能继续作为可用资源使用。

### 7.5 状态和可见性

- `StorageService.updateStatus` 固定负责状态更新。
- `StorageService.updateVisibility` 固定负责可见性更新。
- 状态值和可见性值必须使用枚举表达，不在 Controller 中散落字符串判断。

### 7.6 业务绑定

- `StorageService.insertBusiness` 固定负责新增业务绑定关系。
- `StorageService.removeBusiness` 固定负责按 `businessType` 和 `businessId` 删除业务绑定关系。
- `StorageService.listBusiness` 固定负责读取指定存储资源的业务绑定关系列表。
- 业务绑定关系不得替代 `Storage` 元数据本体。

### 7.7 私有资源访问

- 私有资源必须存在 owner 访问边界。
- 前后台访问私有资源时必须能区分当前访问者与 `Storage.ownerId`。
- 当前代码未完整固化私有资源 owner 校验，必须作为上线前差异项补齐。

## 8. Key Flows

### 8.1 后台上传流程

1. `StorageController.upload` 接收 multipart 请求。
2. `StorageController` 校验请求格式、文件是否为空和后缀白名单。
3. `StorageUtils.applyFileMetadata` 写入文件名、扩展名、MIME type、ID 和创建时间。
4. `StorageConverter.toFile` 计算本地文件路径。
5. `StorageUtils.saveFile` 写入本地文件。
6. `StorageService.add` 写入元数据。
7. `StorageInterfaceAssembler.toUploadResponse` 组装上传响应。

### 8.2 后台分页查询流程

1. `StorageController.page` 接收 `StoragePageRequest`。
2. `StorageInterfaceAssembler.toQuery` 转换为 `StorageQuery`。
3. `StorageController.readStoragePage` 标准化分页参数。
4. `StorageService.page` 查询分页数据。
5. `StorageInterfaceAssembler.toResponse` 组装 `StorageResponse`。

### 8.3 文件输出流程

1. 入口通过预览接口或 Servlet 接收文件访问请求。
2. `StorageConverter` 或 Controller 解析 `Storage.id`。
3. `StorageService.getById` 读取元数据。
4. 入口检查资源和本地文件是否存在。
5. 入口按 `Storage.mimeType` 输出文件内容。

### 8.4 业务绑定流程

1. 业务模块完成自身业务校验。
2. 业务模块通过 `StorageService.removeBusiness` 清理旧绑定关系。
3. 业务模块通过 `StorageService.insertBusiness` 写入新绑定关系。
4. 后续查询通过 `StorageService.listBusiness` 或 `StorageQuery` 装载绑定关系。

## 9. Non-Functional Requirements

- 普通上传、查询、删除和业务绑定必须有 Service 层测试覆盖。
- `StoragePersistenceAssembler` 的字段转换必须有测试覆盖。
- 文件访问不存在资源和不存在本地文件时必须稳定返回错误，不输出空内容。
- 缓存失效必须覆盖新增、更新、删除和业务绑定变化。
- 上传后缀白名单和 MIME type 输出行为必须在上线前人工确认。
- 数据库字段、枚举持久化值和 `StorageResponse` 对外字段必须保持一致。

## 10. Open Items

- 补齐私有资源 owner 访问校验。
- 明确删除是逻辑删除还是物理删除，并同步数据库设计和 Service 测试。
- 明确 `StorageBusiness` 是否需要独立创建时间、更新时间和唯一约束。
- 明确前台是否需要普通上传接口。
- 明确是否引入分片上传。
- 明确是否引入对象存储供应商抽象。
- 明确是否引入存储审计日志和 outbox。
