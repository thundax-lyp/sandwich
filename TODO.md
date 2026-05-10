# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


- [ ] `Department.TreeSort 边界`：禁止 Department FlatSort 暴露
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DepartmentService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DepartmentService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImpl.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DepartmentController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DepartmentController.java)
  - 处理动作：确认无 sort 排序入口，仅保留 move/move-tree。
  - 验收点：树形排序仅由 lft 与 move 接口驱动。
  - 重要度：8/10

- [ ] `Menu.TreeSort 边界`：禁止 Menu FlatSort 暴露
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/MenuService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/MenuService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/MenuController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/MenuController.java)
  - 处理动作：确认无平铺 sort 入口，仅保留树形 move。
  - 验收点：菜单顺序仅受树排序约束。
  - 重要度：8/10

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
