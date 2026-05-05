# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
