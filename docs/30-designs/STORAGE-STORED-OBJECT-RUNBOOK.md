# Storage Stored Object Runbook

## 1. Purpose

本文档定义 Storage 从文件业务绑定模型收敛到 `StoredObject / StoredObjectReference` 模型的执行手册。

目标是让 Storage 成为存储基础设施能力：Storage 负责对象生命周期、底层存储适配、对象引用关系和内容读取；其他业务模块只保存稳定对象标识，并通过 Storage 能力建立或清理引用。

## 2. Scope

当前范围：

- `Storage` 主对象改为 `StoredObject` 语义。
- `StorageBusiness` 绑定关系改为 `StoredObjectReference` 语义。
- 业务 Servlet 下载入口改为 REST resource。
- `StorageBackend` 从业务接口语义中下沉为 infra 内部底层存储端口。
- Storage 文档、测试、DAO、Service、Controller 和数据库设计同步收口。

不在范围内：

- 引入 bacon 的多模块 `api/application/domain/infra/interfaces` 分层。
- 引入微服务远程 Facade。
- 引入 OSS 生产配置。
- 执行真实生产数据迁移。

## 3. Target Model

目标模型固定为：

`StoredObject -> StoredObjectReference`

对象边界：

- `StoredObject` 是已存储对象主数据，承载对象标识、底层存储类型、bucket、objectKey、原始文件名、contentType、size、访问端点派生信息、对象状态和引用状态。
- `StoredObjectReference` 是其他业务模块对存储对象的引用关系，承载 `objectId`、`ownerType` 和 `ownerId`。
- `MultipartUploadSession` 和 `MultipartUploadPart` 继续作为分片上传运行态对象，完成上传后生成正式 `StoredObject`。
- 其他业务模块只保存 `objectId` 或自身语义包装后的稳定对象标识，不保存底层 `objectKey`、物理路径或入口实现路径。

## 4. REST Boundary

Storage 对外入口应该是 REST resource，不暴露业务 Servlet。

目标入口形态：

- 上传对象：`POST /api/storage/objects`
- 分片初始化：`POST /api/storage/objects/multipart`
- 上传分片：`POST /api/storage/objects/multipart/{uploadId}/parts`
- 完成分片：`POST /api/storage/objects/multipart/{uploadId}/complete`
- 取消分片：`DELETE /api/storage/objects/multipart/{uploadId}`
- 读取对象元数据：`GET /api/storage/objects/{objectId}`
- 读取对象内容：`GET /api/storage/objects/{objectId}/content`
- 建立引用：`POST /api/storage/objects/{objectId}/references`
- 清理引用：`DELETE /api/storage/objects/{objectId}/references`
- 删除对象：`DELETE /api/storage/objects/{objectId}`

接口、Response、数据库主数据和业务模块不得固定暴露 `/servlet/...`。底层使用 Servlet、Controller、本地文件、Nginx 或 OSS 签名 URL 都是入口或 infra 实现细节。

## 5. Storage Port Boundary

`StorageBackend` 语义应该从业务层消失，下沉为 infra 内部底层存储端口。

目标端口：

- `StoredObjectDao`：持久化 `StoredObject` 主数据。
- `StoredObjectReferenceDao`：持久化对象引用关系。
- `MultipartUploadDao`：持久化分片上传 session 和 part 运行态。
- `StoredObjectStore` 或等价 infra 端口：适配本地文件、OSS 等底层存储后端。

Service 负责事务、对象状态、引用状态和跨 DAO 编排；infra 负责 DO、Mapper、底层存储客户端和文件 IO。

## 6. Execution Order

固定执行顺序：

1. 先同步需求和数据库设计文档，固定目标模型和 REST 入口。
2. 再改业务 Entity、枚举、Query 和 Service 接口命名。
3. 再改 DAO interface 和 infra 持久化实现。
4. 再改底层存储端口，把 `StorageBackend` 下沉为 infra 实现细节。
5. 再改 Controller，新增 REST content 入口并移除业务 Servlet 注册。
6. 再改前后台接口装配、Response 和测试。
7. 最后清理旧命名、旧 Servlet 文档、失效 runbook、TODO 和未使用空目录。

每一步完成后应该小步提交。不要把模型改名、REST 入口替换、底层存储下沉和文档清理混成一个提交。

## 7. Verification

常用检查：

```bash
mvn -q -pl sandwish-biz,sandwish-infra,sandwish-admin-api,sandwish-front-api -am test
mvn -q -pl sandwish-biz,sandwish-infra,sandwish-admin-api,sandwish-front-api -am checkstyle:check
git diff --check
```

最小人工检查：

- 普通上传生成 `StoredObject`。
- 分片上传完成后生成 `StoredObject`。
- `GET /api/storage/objects/{objectId}/content` 可以返回对象内容。
- 建立引用后 `StoredObject.referenceStatus` 进入已引用语义。
- 清理最后一个引用后 `StoredObject.referenceStatus` 进入未引用语义。
- 已删除对象不能继续建立引用。
- API 响应和文档不出现 `/servlet/storage/...`。

## 8. Open Items

无
