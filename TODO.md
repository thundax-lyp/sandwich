# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

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
