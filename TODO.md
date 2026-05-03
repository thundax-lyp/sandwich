# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `storage-access`：补齐私有资源 owner 访问校验
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/StorageController.java`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/servlet/StorageServlet.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/storage/servlet/StorageServlet.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java`
  - 处理动作：在前后台文件访问链路中按 `StorageVisibility` 和 owner 信息补齐私有资源访问边界
  - 验收点：公开资源可访问，私有资源仅 owner 可访问，资源不存在和无权限访问返回明确错误
  - 重要度：9/10

- [ ] `storage-query`：对齐存储查询条件与绑定关系过滤
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao/StorageDaoImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java`
    - `docs/20-database/STORAGE-DATABASE-DESIGN.md`
  - 处理动作：明确并实现 `StorageQuery.businessId`、`StorageQuery.businessType` 与 `listMimeTypes` 的数据库过滤语义
  - 验收点：分页和列表查询的所有公开查询字段都有对应持久化行为和测试覆盖
  - 重要度：8/10

- [ ] `storage-delete`：固化存储删除语义
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao/StorageDaoImpl.java`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java`
    - `docs/10-requirements/STORAGE-REQUIREMENTS.md`
    - `docs/20-database/STORAGE-DATABASE-DESIGN.md`
  - 处理动作：明确删除是物理删除、逻辑删除还是状态删除，并让 Service、DAO、测试和文档一致
  - 验收点：删除后的资源不会继续作为可用资源查询、预览或绑定，缓存失效行为有测试覆盖
  - 重要度：8/10

- [ ] `storage-business-binding`：确认并收敛业务绑定表约束
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StorageBusiness.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/StorageBusinessDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao/StorageDaoImpl.java`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssemblerTest.java`
    - `docs/20-database/STORAGE-DATABASE-DESIGN.md`
  - 处理动作：确认一个文件是否允许绑定多个业务对象，并按结论调整主键、唯一约束或绑定写入规则
  - 验收点：绑定关系的数据库约束、DO 主键来源、DAO 写入行为和 assembler 测试一致
  - 重要度：7/10

## 待讨论项

- [ ] 是否引入分片上传
  - 关联任务：`storage-multipart`
  - 决策要求：确认 Sandwich 是否需要分片上传、断点续传、分片会话和分片 part 表
  - 重要度：6/10

- [ ] 是否引入对象存储供应商抽象
  - 关联任务：`storage-backend`
  - 决策要求：确认当前本地文件存储是否需要演进为 local / OSS 可切换后端
  - 重要度：6/10

- [ ] 是否引入存储审计日志和 outbox
  - 关联任务：`storage-audit`
  - 决策要求：确认上传、删除、访问和业务绑定变化是否需要追加审计日志与 outbox
  - 重要度：5/10
