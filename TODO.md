# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项

- [ ] `Dict.Service 接口`：补齐 Dict 排序入口为 FlatSort 统一签名
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java)
  - 处理动作：移除单值 priority 操作，新增/统一 `sort(List<DictId> orderedIds, SortDirection sortDirection)`。
  - 验收点：Sortable Dict 接口不再暴露 `changePriority`。
  - 重要度：10/10

- [ ] `Dict.Service 实现`：实现 Dict create+sort 的交换式排序链路
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java)
  - 处理动作：按 `orderedIds + sortDirection` 验证集合一致性并执行交换更新，新增实体采用 `max+10`。
  - 验收点：排序无插值重排且失败全回滚。
  - 重要度：10/10

- [ ] `Dict.DAO 接口`：补齐 Dict 排序查询与更新能力
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DictDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DictDao.java)
  - 处理动作：新增域内最大优先级、按 ID 列表查询、按方向排序与批量优先级更新能力。
  - 验收点：Service 可基于 DAO 完成完整交换序列。
  - 重要度：9/10

- [ ] `Dict.DAO 实现`：补齐 Dict Mapper/DAO 语义
  - 范围文件：
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/DictDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/DictDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/DictMapper.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/DictMapper.java)
  - 处理动作：落地 `maxPriorityByType`、`listByTypeOrder` 与排序更新 SQL。
  - 验收点：排序链路数据库读写覆盖全域。
  - 重要度：9/10

- [ ] `Dict.Controller 协议`：新增/更新 Dict sort 请求端点
  - 范围文件：
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DictController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DictController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DictSortRequest.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DictSortRequest.java)
  - 处理动作：改造排序接口为 `orderedIds + sortDirection`，不接受 priority。
  - 验收点：Dict API 不再支持 priority 写入。
  - 重要度：10/10

- [ ] `Dict.数据库约束`：补齐 Dict 排序域唯一索引
  - 范围文件：
    - [db/schema/system.sql](db/schema/system.sql)
  - 处理动作：为 `sys_dict` 增加 `UNIQUE(type, priority)`。
  - 验收点：同 type 的 priority 冲突可被数据库拒绝。
  - 重要度：8/10

- [ ] `Role.遗留单点优先级清理`：移除 Role 单点 priority 接口
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/command/ChangeRolePriorityCommand.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/command/ChangeRolePriorityCommand.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RolePriorityRequest.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RolePriorityRequest.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java)
  - 处理动作：废弃/替换 `changePriority`，不再依赖 request 中 priority。
  - 验收点：无可直接提交的 priority 更新入口。
  - 重要度：9/10

- [ ] `Role.Service 接口`：补齐 Role FlatSort 签名
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java)
  - 处理动作：增加 `sort(List<RoleId> orderedIds, SortDirection sortDirection)`。
  - 验收点：接口层仅保留 orderedIds 驱动排序。
  - 重要度：10/10

- [ ] `Role.Service 实现`：实现 Role 交换式排序与新增优先级步长
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java)
  - 处理动作：新增实体按 `max(status)+10`，排序按目标顺序交换 priority。
  - 验收点：越权/越界请求返回 SORT_* 错误；幂等重放。
  - 重要度：10/10

- [ ] `Role.DAO 接口`：补齐 Role 排序域查询能力
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/RoleDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/RoleDao.java)
  - 处理动作：补全 `maxPriorityByScope`、`listIdsByScope`、`updatePriority`。
  - 验收点：Service 可一次性加载域内排序候选。
  - 重要度：9/10

- [ ] `Role.DAO 实现`：补齐 Role 读写 SQL
  - 范围文件：
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/RoleDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/RoleDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/RoleMapper.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/RoleMapper.java)
  - 处理动作：实现 sortDirection 下推与单/批更新。
  - 验收点：数据库无插值更新逻辑。
  - 重要度：9/10

- [ ] `Role.Controller 协议`：切换 Role 排序端点
  - 范围文件：
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleSortRequest.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleSortRequest.java)
  - 处理动作：新增 sort 接口，移除 `priority` 列表式批更新。
  - 验收点：请求体仅包含 `orderedIds` 与 `sortDirection`。
  - 重要度：10/10

- [ ] `Role.数据库约束`：补齐 Role 排序域唯一索引
  - 范围文件：
    - [db/schema/system.sql](db/schema/system.sql)
  - 处理动作：为 `sys_role` 的排序域加 `UNIQUE(status, priority)`。
  - 验收点：同域内无重复 priority。
  - 重要度：8/10

- [ ] `User.Service 接口`：补齐 User FlatSort 协议
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java)
  - 处理动作：新增 `sort(List<UserId> orderedIds, SortDirection sortDirection)`。
  - 验收点：服务接口不再依赖单条 priority 入参。
  - 重要度：10/10

- [ ] `User.Service 实现`：实现 User 交换式排序与并发回滚
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java)
  - 处理动作：按可见域校验并执行 exchanges，创建使用 `max+10`。
  - 验收点：ASC/DESC 均可复现请求顺序。
  - 重要度：10/10

- [ ] `User.DAO 接口`：补齐 User 域查询能力
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserDao.java)
  - 处理动作：新增 max、列表顺序与优先级更新 API。
  - 验收点：排序前置检查可全量拉取当前域。
  - 重要度：9/10

- [ ] `User.DAO 实现`：补齐 User Mapper/DAO 语义
  - 范围文件：
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/UserDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/UserDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/UserMapper.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/mapper/UserMapper.java)
  - 处理动作：按 `sortDirection` 查询与更新。
  - 验收点：User 列表排序逻辑与 sort API 方向保持一致。
  - 重要度：9/10

- [ ] `User.Query 协议`：收敛 User 查询方向到 SortDirection
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/UserQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/UserQuery.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java)
  - 处理动作：移除自由 `orderBy`，改为 SortDirection。
  - 验收点：非 FlatSort 查询不再通过字符串 orderBy 控制排序。
  - 重要度：8/10

- [ ] `User.Controller/API 协议`：新增 User sort 接口
  - 范围文件：
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserSortRequest.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserSortRequest.java)
  - 处理动作：新增 sort 入口并接收 orderedIds 与 sortDirection。
  - 验收点：排序请求返回值与失败码与 Runbook 一致。
  - 重要度：9/10

- [ ] `User.数据库约束`：补齐 User 排序域唯一索引
  - 范围文件：
    - [db/schema/system.sql](db/schema/system.sql)
  - 处理动作：为 `sys_user` 的排序域加 `UNIQUE(department_id, priority)`。
  - 验收点：同域出现 duplicate priority 时数据库可拒绝。
  - 重要度：8/10

- [ ] `Member.Service/DAO`：核对 Member 已有排序链路与 create 落位
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java](sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberDao.java)
  - 处理动作：对齐 exchange sort 与 `max(priority in scope)+10` 约定，补齐 scope 取值边界。
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

- [ ] `Member.数据库约束`：补齐成员排序域唯一索引
  - 范围文件：
    - [db/schema/member.sql](db/schema/member.sql)
  - 处理动作：为 member 表补充域内优先级唯一约束。
  - 验收点：同域重复 priority 被数据库拒绝。
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
  - 处理动作：按 `max+10`+交换式方式复核并收敛异常映射。
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

- [ ] `StoredObject.数据库约束`：补齐存储排序域唯一性索引
  - 范围文件：
    - [db/schema/storage.sql](db/schema/storage.sql)
  - 处理动作：为 `assist_storage` 添加同域唯一性约束与历史重复清理。
  - 验收点：插入/交换冲突可被 DB 检测。
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
  - 处理动作：按 scope 校验并执行交换更新。
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
