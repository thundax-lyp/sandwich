# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


## 待审阅任务项

- [ ] `submission-biz`：实现提交内容业务服务
  - 范围文件：
    - `sandwish-biz/src/main/java/com/github/thundax/modules/submission/entity/**`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/submission/dao/**`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/submission/service/**`
    - `sandwish-biz/src/main/java/com/github/thundax/modules/audit/runtime/submission/**`
    - `sandwish-biz/src/test/java/com/github/thundax/modules/submission/**`
  - 处理动作：按 `docs/30-designs/RUNBOOK-SUBMISSION-IMPLEMENTATION.md` 新增 `Submission` 领域对象、Service、排序、审计快照和业务测试
  - 验收点：`Submission` 可创建、查询、状态调整、排序并接入 Audit，且 `mvn -pl sandwish-biz -am -Dtest='*Submission*Test,*Audit*Test' test` 通过
  - 重要度：9/10

- [ ] `submission-infra`：实现提交内容持久化
  - 范围文件：
    - `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/dataobject/**`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/mapper/**`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/assembler/**`
    - `sandwish-infra/src/main/java/com/github/thundax/modules/submission/persistence/dao/**`
    - `sandwish-infra/src/test/java/com/github/thundax/modules/submission/**`
  - 处理动作：按 `docs/20-database/SUBMISSION-DATABASE-DESIGN.md` 和 RUNBOOK 新增 DO、Mapper、DAO implementation、assembler、排序持久化和测试
  - 验收点：持久化字段与 `db/schema/submission.sql` 一致，分页按 `priority asc, id asc`，图片按 `sort_order` 装载，且 `mvn -pl sandwish-infra -am -Dtest='*Submission*Test' test` 通过
  - 重要度：9/10

- [ ] `submission-admin-api`：实现提交内容后台接口
  - 范围文件：
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/submission/controller/**`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/submission/assembler/**`
    - `sandwish-admin-api/src/main/java/com/github/thundax/modules/submission/service/**`
    - `sandwish-admin-api/src/main/resources/**`
    - `sandwish-admin-api/src/test/java/com/github/thundax/modules/submission/**`
    - `docs/30-designs/ADMIN-API-ERROR-CODE-DESIGN.md`
  - 处理动作：按 RUNBOOK 新增后台分页、详情、状态调整、排序接口和契约测试，并同步必要后台错误码
  - 验收点：Controller 不暴露持久化对象，排序入口只接收 `orderedIds` 和 `sortDirection`，且 `mvn -pl sandwish-admin-api -am -Dtest='*Submission*Test' test` 通过
  - 重要度：8/10

- [ ] `submission-closeout`：收口提交内容实现任务
  - 范围文件：
    - `docs/10-requirements/SUBMISSION-REQUIREMENTS.md`
    - `docs/20-database/SUBMISSION-DATABASE-DESIGN.md`
    - `db/schema/submission.sql`
    - `docs/30-designs/SORT-ORDERING-SPECIAL-DESIGN.md`
    - `docs/30-designs/RUNBOOK-SUBMISSION-IMPLEMENTATION.md`
    - `TODO.md`
  - 处理动作：按 RUNBOOK closure checklist 校对文档、SQL、排序清单和测试结果，完成后删除或收窄 RUNBOOK 与 TODO
  - 验收点：文档与代码一致，RUNBOOK 不保留已完成范围，`TODO.md` 不保留已完成任务，并完成最终验证或记录未执行原因
  - 重要度：7/10

## 待讨论项
