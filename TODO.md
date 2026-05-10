# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


- [ ] `全局排序错误码`：收敛 SORT 异常码映射
  - 范围文件：
    - [sandwish-common/src/main/java/com/github/thundax/common/exception/ErrorCode.java](sandwish-common/src/main/java/com/github/thundax/common/exception/ErrorCode.java)
    - [sandwish-common/src/main/java/com/github/thundax/common/exception/BusinessExceptionHandler.java](sandwish-common/src/main/java/com/github/thundax/common/exception/BusinessExceptionHandler.java)
  - 处理动作：统一补齐 SORT_EMPTY_INPUT、SORT_DUPLICATE_ID、SORT_MISSING_ID、SORT_CONCURRENT_MODIFICATION、SORT_DB_FAILURE。
  - 验收点：并发冲突返回统一编码。
  - 重要度：10/10

- [ ] `任务收口与一致性核对`：清理 TODO 与代码覆盖关系
  - 范围文件：
    - [TODO.md](TODO.md)
    - [docs/30-designs/RUNBOOK-SORTABLE-REFACTOR.md](docs/30-designs/RUNBOOK-SORTABLE-REFACTOR.md)
    - [docs/30-designs/SORT-ORDERING-SPECIAL-DESIGN.md](docs/30-designs/SORT-ORDERING-SPECIAL-DESIGN.md)
  - 处理动作：核对每个任务的文件范围，完成后删除已关闭项。
  - 验收点：未完成任务无遗漏且无重复。
  - 重要度：6/10

## 待审阅任务项

- 无

## 待讨论项

- 无
