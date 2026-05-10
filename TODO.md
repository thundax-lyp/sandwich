# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项


- [ ] `Member.Service/DAO`：核对 Member 已有排序链路与 create 落位
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java](sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberDao.java)
  - 处理动作：对齐 exchange sort 与 `max(priority)+10` 约定，确认排序域完整性边界。
  - 验收点：排序异常只通过 `SORT_*` 路径返回，列表查询方向正确。
  - 重要度：9/10

- [ ] `Member.DAO/Mapper 实现`：补齐 Member 排序域 SQL 映射
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberDao.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/dao/MemberDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/mapper/MemberMapper.java](sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/mapper/MemberMapper.java)
  - 处理动作：补齐 max/list/sort/update priority 的 mapper 能力。
  - 验收点：排序更新可在 DAO 一次完成。
  - 重要度：8/10

- [ ] `Member.数据库约束`：补齐成员 priority 全局唯一索引
  - 范围文件：
    - [db/schema/member.sql](db/schema/member.sql)
  - 处理动作：为 member 表补充全局优先级唯一约束。
  - 验收点：全局重复 priority 被数据库拒绝。
  - 重要度：8/10

- [ ] `Storage.Service 接口`：确认/补齐排序方向参数
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/StorageService.java](sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/StorageService.java)
  - 处理动作：确认 sort 接口仅支持 orderedIds 与 SortDirection。
  - 验收点：无 priority 直接写入参数。
  - 重要度：8/10

- [ ] `Storage.Service 与 DAO`：核对 stored object 排序一致性与并发兜底
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/storage/dao/StoredObjectDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/storage/dao/StoredObjectDao.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao/StoredObjectDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/dao/StoredObjectDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/mapper/StoredObjectMapper.java](sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/mapper/StoredObjectMapper.java)
  - 处理动作：按 `max(priority)+10`+交换式方式复核并收敛异常映射。
  - 验收点：排序失败可回滚，返回 SORT 约定码。
  - 重要度：9/10

- [ ] `Storage.Controller 协议`：复用已有 sort 请求并去除外露字段
  - 范围文件：
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/StorageController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/StorageController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/request/StorageSortRequest.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/request/StorageSortRequest.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/assembler/StorageInterfaceAssembler.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/assembler/StorageInterfaceAssembler.java)
  - 处理动作：确认只支持 orderedIds + sortDirection，不返回 priority。
  - 验收点：存储排序 API 完全对齐 runbook 协议。
  - 重要度：8/10

- [ ] `StoredObject.数据库约束`：补齐存储 priority 全局唯一性索引
  - 范围文件：
    - [db/schema/storage.sql](db/schema/storage.sql)
  - 处理动作：为 `assist_storage` 补充全局优先级唯一约束与历史重复清理。
  - 验收点：全局重复/交换冲突可被 DB 检测。
  - 重要度：8/10

- [ ] `AsyncTask.Service 接口`：补齐异步任务排序能力
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/AsyncTaskService.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/AsyncTaskService.java)
  - 处理动作：新增 sort(orderedIds, sortDirection) 入参签名。
  - 验收点：服务层不再支持外部 priority 写入。
  - 重要度：8/10

- [ ] `AsyncTask.Service 实现`：实现/对齐 AsyncTask 交换式排序
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/query/AsyncTaskQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/query/AsyncTaskQuery.java)
  - 处理动作：按当前可见集合校验并执行交换更新。
  - 验收点：无插值重排，重复提交幂等。
  - 重要度：8/10

- [ ] `AsyncTask.DAO`：补齐 AsyncTask 排序查询与更新方法
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/dao/AsyncTaskDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/dao/AsyncTaskDao.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dao/AsyncTaskDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dao/AsyncTaskDaoImpl.java)
  - 处理动作：新增 max/list/sort/update API，支持排序交换。
  - 验收点：Service 可仅通过 DAO 完成排序。
  - 重要度：9/10

- [ ] `AsyncTask.Controller/API`：新增排序端点并移除 response priority 外露
  - 范围文件：
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/AsyncTaskController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/AsyncTaskController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/AsyncTaskResponse.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/AsyncTaskResponse.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/AsyncTaskInterfaceAssembler.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/AsyncTaskInterfaceAssembler.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/AsyncTaskSortRequest.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/AsyncTaskSortRequest.java)
  - 处理动作：新增 sort 端点，移除 priority 字段。
  - 验收点：API 响应与请求中不出现 priority。
  - 重要度：9/10

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
