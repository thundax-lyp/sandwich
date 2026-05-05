# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `common-oss`：补充对象存储客户端抽象与本地文件实现
  - 范围文件：
    - `pom.xml`
    - `sandwish-common/pom.xml`
    - `sandwish-common/sandwish-common-oss/pom.xml`
    - `sandwish-common/sandwish-common-oss/src/main/java/com/github/thundax/common/oss/client/ObjectStorageClient.java`
    - `sandwish-common/sandwish-common-oss/src/main/java/com/github/thundax/common/oss/config/SandwishOssAutoConfiguration.java`
    - `sandwish-common/sandwish-common-oss/src/main/java/com/github/thundax/common/oss/config/SandwishOssProperties.java`
    - `sandwish-common/sandwish-common-oss/src/main/java/com/github/thundax/common/oss/model/ObjectStorageWriteResult.java`
    - `sandwish-common/sandwish-common-oss/src/main/java/com/github/thundax/common/oss/support/LocalFileObjectStorageClient.java`
    - `sandwish-common/sandwish-common-oss/src/test/java/com/github/thundax/common/oss/support/LocalFileObjectStorageClientTest.java`
  - 处理动作：新增 common-oss 模块，定义对象存储契约、配置和 local file backend。
  - 验收点：local file 写入、读取、删除测试通过，模块不依赖 storage 业务。
  - 重要度：9/10

- [ ] `common-oss`：补充 S3 对象存储实现
  - 范围文件：
    - `sandwish-common/sandwish-common-oss/pom.xml`
    - `sandwish-common/sandwish-common-oss/src/main/java/com/github/thundax/common/oss/config/SandwishOssProperties.java`
    - `sandwish-common/sandwish-common-oss/src/main/java/com/github/thundax/common/oss/support/S3ObjectStorageClient.java`
    - `sandwish-common/sandwish-common-oss/src/test/java/com/github/thundax/common/oss/support/S3ObjectStorageClientTest.java`
  - 处理动作：在 common-oss 内提供 S3 backend，保持 endpoint、bucket、access key 等配置外置。
  - 验收点：S3 client 创建和对象 key 组装有测试，不提交任何环境密钥。
  - 重要度：8/10

- [ ] `storage`：基于 common-oss 重建存储后端适配
  - 范围文件：
    - `sandwish-infra/pom.xml`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/store/StoredObjectStore.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/store/LocalFileStoredObjectStore.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/store/ObjectStorageStoredObjectStore.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/storage/store/LocalFileStoredObjectStoreTest.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/storage/store/ObjectStorageStoredObjectStoreTest.java`
  - 处理动作：让 storage infra 通过 common-oss 的 `ObjectStorageClient` 完成对象读写，同时保留 local file 和 S3 backend 能力。
  - 验收点：storage 服务测试不再直接处理本地/S3 细节，local file backend 行为保持兼容。
  - 重要度：9/10

- [ ] `storage`：清理旧存储实现现场
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/store/LocalFileStoredObjectStore.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/storage/store/LocalFileStoredObjectStoreTest.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/enums/StorageType.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/utils/StorageUtils.java`
  - 处理动作：删除或收窄被 common-oss 替代的重复后端逻辑、无用工具和残留分支。
  - 验收点：`rg "UploadFile|StorageServlet|LocalFileStoredObjectStore"` 无无效残留，storage 相关测试通过。
  - 重要度：8/10

## 待审阅任务项

## 待讨论项
