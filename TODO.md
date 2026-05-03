# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

- [ ] `storage-backend`：引入 LOCAL_FILE / OSS 底层存储后端抽象
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/storage`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/storage`
    - `docs/10-requirements/STORAGE-REQUIREMENTS.md`
    - `docs/20-database/STORAGE-DATABASE-DESIGN.md`
  - 处理动作：抽象统一存储后端接口，并让普通上传和文件访问通过 LOCAL_FILE 后端适配当前行为，预留 OSS 后端装配点
  - 验收点：当前本地上传和访问行为不回退，Storage 元数据能记录 storageType、bucketName、objectKey、size 和 accessEndpoint
  - 重要度：9/10

- [ ] `storage-multipart-schema`：增加分片上传会话和分片持久化模型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/mapper`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/storage/persistence/assembler`
    - `docs/20-database/STORAGE-DATABASE-DESIGN.md`
  - 处理动作：新增 MultipartUploadSession、MultipartUploadPart 及其 DO、Mapper、assembler 和字段转换测试
  - 验收点：分片上传会话和分片记录的字段、枚举、唯一约束设计与数据库设计文档一致
  - 重要度：8/10

- [ ] `storage-multipart-service`：实现分片上传业务流程
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/dao`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl`
    - `docs/10-requirements/STORAGE-REQUIREMENTS.md`
  - 处理动作：实现初始化分片上传、上传分片、完成分片上传和取消分片上传的 Service 与 DAO 编排
  - 验收点：初始化、上传分片、完成、取消、重复 partNumber 和非法状态都有测试覆盖
  - 重要度：8/10

## 待审阅任务项

## 待讨论项
