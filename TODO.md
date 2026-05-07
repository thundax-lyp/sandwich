# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `snowflake-code-storage-biz`：改代码，迁移存储域 biz ID 类型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StoredObject.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/StoredObjectReference.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/MultipartUploadSession.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/MultipartUploadPart.java`
  - 处理动作：将存储域 biz 层 ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-biz -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-storage-infra`：改代码，迁移存储域 infra ID 类型
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/StoredObjectDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/StoredObjectReferenceDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/MultipartUploadSessionDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dataobject/MultipartUploadPartDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssembler.java`
  - 处理动作：将存储域 infra 层 DO 和 assembler ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-infra -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-member-assist`：改代码，迁移会员和辅助域 ID 类型
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dataobject/MemberDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberPersistenceAssembler.java`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/AsyncTask.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dataobject/AsyncTaskDO.java`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java`
  - 处理动作：将会员和辅助域 ID 类型迁移到雪花 `Long`。
  - 验收点：`mvn -pl sandwish-biz,sandwish-infra -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-code-api-boundary`：改代码，迁移 API 边界 ID 类型
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/**/*.java`
    - `sandwish-front-api/src/main/java/com/github/thundax/modules/**/*.java`
  - 处理动作：将前后台 API Request、Response 和 assembler 中的 ID 类型与雪花 `Long` 口径对齐。
  - 验收点：`mvn -pl sandwish-admin-api,sandwish-front-api -am test` 通过。
  - 重要度：9/10

- [ ] `snowflake-final-close`：全局雪花 ID 迁移收口
  - 范围文件：
    - `TODO.md`
    - `docs/30-designs/RUNBOOK-GLOBAL-SNOWFLAKE-ID-MIGRATION.md`
  - 处理动作：完成全局雪花 ID 迁移后删除 RUNBOOK，并删除、拆分或收窄已完成 TODO 项。
  - 验收点：`rg "ASSIGN_UUID|UuidIdGenerator|UuidHelper" ...` 和 `mvn install` 通过，工作区干净。
  - 重要度：10/10

## 待讨论项
