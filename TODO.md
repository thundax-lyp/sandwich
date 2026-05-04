# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `storage-store`：下沉底层存储端口
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/backend/StorageBackend.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/backend/StorageBackendObject.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/backend/LocalFileStorageBackend.java
    sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java
    sandwish-front-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java
  - 处理动作：把 `StorageBackend` 从业务模型迁移为 infra 内部 `StoredObjectStore` 或等价底层存储端口
  - 验收点：Controller、Service 接口、Response 和业务模块不再依赖 `StorageBackend` 类型或命名
  - 重要度：9/10

- [ ] `storage-rest-api`：用 REST content 入口替换业务 Servlet
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/StorageController.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/servlet/StorageServlet.java
    sandwish-front-api/src/main/java/com/github/thundax/modules/storage/servlet/StorageServlet.java
    sandwish-admin-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java
    sandwish-front-api/src/main/java/com/github/thundax/autoconfigure/WebMvcConfiguration.java
  - 处理动作：新增 REST 对象内容读取接口并移除业务 Storage Servlet 注册
  - 验收点：公开存储访问路径为 REST resource，代码和配置不再用 `servletPath` 表达业务访问路径
  - 重要度：10/10

- [ ] `storage-api-model`：调整接口请求响应模型
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/StorageController.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/StorageIdRequest.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/StoragePageRequest.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/StorageResponse.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/StorageTreeNodeResponse.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/StorageUploadResponse.java
  - 处理动作：将上传、查询、引用和内容读取的 Request / Response 对齐 `StoredObject` 语义
  - 验收点：接口响应不包含 `/servlet/storage/...`，对象访问信息以 REST content 资源或派生访问字段表达
  - 重要度：9/10

- [ ] `storage-tests`：补齐 Storage 模型迁移测试
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java
    sandwish-biz/src/test/java/com/github/thundax/modules/storage/backend/LocalFileStorageBackendTest.java
    sandwish-infra/src/test/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssemblerTest.java
  - 处理动作：更新上传、引用、分片完成、内容读取和删除流程测试
  - 验收点：Storage 相关单测覆盖 `StoredObject` 生成、引用状态变化、REST content 读取和旧 Servlet 路径消失
  - 重要度：10/10

- [ ] `storage-cleanup`：清理 Storage 迁移现场
  - 范围文件：TODO.md
    docs/30-designs/STORAGE-STORED-OBJECT-RUNBOOK.md
    docs/30-designs/AUTH-IDENTITY-CREDENTIAL-RUNBOOK.md
    docs/30-designs/COMMON-CACHE-JETCACHE-RUNBOOK.md
    docs/10-requirements/STORAGE-REQUIREMENTS.md
    docs/20-database/STORAGE-DATABASE-DESIGN.md
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/Storage.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StorageBusiness.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/backend/StorageBackend.java
    sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/servlet/StorageServlet.java
    sandwish-front-api/src/main/java/com/github/thundax/modules/storage/servlet/StorageServlet.java
  - 处理动作：删除失效 `*-RUNBOOK.md`、旧 Servlet 残留、旧命名残留、空目录和已完成 TODO
  - 验收点：`rg "StorageBusiness|/servlet/storage|servletPath|StorageBackend"` 不再命中公开业务接口和正式文档，完成项从 `TODO.md` 删除或收窄
  - 重要度：10/10

## 待审阅任务项

## 待讨论项
