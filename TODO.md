# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `storage-docs`：固定 StoredObject 目标模型文档
  - 范围文件：docs/10-requirements/STORAGE-REQUIREMENTS.md
    docs/20-database/STORAGE-DATABASE-DESIGN.md
    docs/30-designs/STORAGE-STORED-OBJECT-RUNBOOK.md
  - 处理动作：同步 Storage 需求、数据库设计和 runbook 的 `StoredObject / StoredObjectReference` 目标口径
  - 验收点：文档只描述当前目标模型和 REST 入口，不再把 `/servlet/storage/...` 作为公开接口形态
  - 重要度：9/10

- [ ] `storage-entity`：重命名存储主对象和引用对象
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/Storage.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StorageBusiness.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StorageBackendType.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StorageOwnerType.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StorageStatus.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StorageVisibility.java
  - 处理动作：将 `Storage` / `StorageBusiness` 收敛为 `StoredObject` / `StoredObjectReference` 语义
  - 验收点：业务 Entity 使用 `StoredObject`、`StoredObjectReference`、`ownerType`、`ownerId`、`contentType` 和对象引用状态语义
  - 重要度：10/10

- [ ] `storage-service`：调整 Storage Service 对象语义
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/StorageService.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/query/StorageQuery.java
  - 处理动作：将 Service 入参、返回值和查询条件改为 `StoredObject` 语义并保留事务编排
  - 验收点：Service 对外不再暴露 `StorageBusiness` 或 `StorageBackend` 业务语义，上传、引用、删除和内容读取以 `StoredObject` 表达
  - 重要度：10/10

- [ ] `storage-dao`：调整 DAO 端口语义
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/dao/StorageDao.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/dao/StorageBusinessDao.java
    sandwish-biz/src/main/java/com/github/thundax/modules/storage/dao/MultipartUploadDao.java
  - 处理动作：将 DAO interface 收敛为 `StoredObjectDao`、`StoredObjectReferenceDao` 和 `MultipartUploadDao`
  - 验收点：DAO 端口按存储对象主数据、引用关系、分片上传运行态分离，命名不再出现 `StorageBusiness`
  - 重要度：9/10

- [ ] `storage-infra`：调整持久化实现和 DO 命名
  - 范围文件：sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao/StorageDaoImpl.java
    sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao/StorageBusinessDaoImpl.java
    sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/StorageDO.java
    sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/StorageBusinessDO.java
    sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/mapper/StorageMapper.java
    sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/mapper/StorageBusinessMapper.java
    sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssembler.java
  - 处理动作：将 infra 持久化对象、Mapper、Assembler 和 DAO implementation 对齐 `StoredObject` / `StoredObjectReference`
  - 验收点：infra 不再使用 `StorageDO` / `StorageBusinessDO` 作为当前模型名，数据库字段映射与设计文档一致
  - 重要度：10/10

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
