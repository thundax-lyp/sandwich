# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项



- [ ] `SYS 接口服务定义`：补齐 Sys 领域排序服务接口
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DepartmentService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DepartmentService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/MenuService.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/MenuService.java)
  - 处理动作：替换/新增 sort 接口为“按实体序列交换 priority”，保留树形 move。
  - 验收点：服务层 API 不再暴露单实体 priority 设置能力。
  - 重要度：10/10

- [ ] `SYS Sortable 业务实现`：实现 Sys 领域 create 最大值+步长及交换排序流程
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java)
  - 处理动作：实现 create 时 max(priority)+step，并按 id 序列执行交换持久化和冲突重试。
  - 验收点：同域排序无插值重排，create 和 sort 均生成唯一间隔 priority。
  - 重要度：10/10

- [ ] `SYS 查询约束同步`：限制 Sys 查询排序字段为优先级方向
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/DictQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/DictQuery.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/RoleQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/RoleQuery.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/UserQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/UserQuery.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/DepartmentQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/DepartmentQuery.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/MenuQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/MenuQuery.java)
  - 处理动作：限制排序入口仅允许 priority asc/desc。
  - 验收点：非法 orderBy 被统一拒绝或降级，排序行为可预期。
  - 重要度：8/10

- [ ] `SYS DAO 数据访问扩展`：补齐 max 与批量更新能力
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DictDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DictDao.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/RoleDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/RoleDao.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserDao.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DepartmentDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DepartmentDao.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/MenuDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/MenuDao.java)
  - 处理动作：新增“按域 max priority”与“批量 priority 读写”接口。
  - 验收点：服务端交换排序可在事务内一次性获取并更新多个实体。
  - 重要度：9/10

- [ ] `SYS DAO 实现`：实现 Sys 领域 priority 查询与批量更新 SQL
  - 范围文件：
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/DictDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/DictDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/RoleDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/RoleDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/UserDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/UserDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/DepartmentDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/DepartmentDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/MenuDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/dao/MenuDaoImpl.java)
  - 处理动作：补齐批量查询和批量更新 SQL，支持 create 与排序交换。
  - 验收点：可复用方法可在单次 SQL 调用内返回可交换的 priority 集合并完成批量持久化。
  - 重要度：9/10

- [ ] `SYS 控制器接口调整`：改造 Sys 控制器排序接口为新协议
  - 范围文件：
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DictController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DictController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DepartmentController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DepartmentController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/MenuController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/MenuController.java)
  - 处理动作：新增/更新 FlatSort 接口为 id 顺序 + sortDirection，保留树形 move 独立。
  - 验收点：前端排序调用只提交 id 数组，返回成功即完成交换。
  - 重要度：10/10

- [ ] `Assist 领域排序闭环`：清理 async task priority 外露并完成可选排序能力
  - 范围文件：
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/AsyncTaskController.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/AsyncTaskController.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/AsyncTaskResponse.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/response/AsyncTaskResponse.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/AsyncTaskInterfaceAssembler.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/assembler/AsyncTaskInterfaceAssembler.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/command/AsyncTaskCommand.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/command/AsyncTaskCommand.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/AsyncTaskService.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/AsyncTaskService.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImpl.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/dao/AsyncTaskDao.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/dao/AsyncTaskDao.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/query/AsyncTaskQuery.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/query/AsyncTaskQuery.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/AsyncTask.java](sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/AsyncTask.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dao/AsyncTaskDaoImpl.java](sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/dao/AsyncTaskDaoImpl.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java)
  - 处理动作：清理 response/assembler/command 里的 priority 外显，并补齐排序能力。
  - 验收点：异步任务排序流程与其他域一致，controller 不再透出 priority。
  - 重要度：8/10

- [ ] `持久化映射清理`：核对各实体 priority 映射在持久化层仅作为内部字段
  - 范围文件：
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DictPersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DictPersistenceAssembler.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/RolePersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/RolePersistenceAssembler.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserPersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/UserPersistenceAssembler.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DepartmentPersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/DepartmentPersistenceAssembler.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/MenuPersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/sys/persistence/assembler/MenuPersistenceAssembler.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/storage/persistence/assembler/StoragePersistenceAssembler.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberPersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/member/persistence/assembler/MemberPersistenceAssembler.java)
    - [sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java](sandwish-infra/src/main/java/com/github/thundax/modules/assist/persistence/assembler/AsyncTaskPersistenceAssembler.java)
  - 处理动作：统一持久化映射逻辑，不影响外部响应与入参语义。
  - 验收点：priority 映射只在 DAO/实体模型层流转。
  - 重要度：7/10

- [ ] `SQL 约束与索引`：补齐排序域内唯一策略
  - 范围文件：
    - [db/schema/system.sql](db/schema/system.sql)
    - [db/schema/member.sql](db/schema/member.sql)
    - [db/schema/storage.sql](db/schema/storage.sql)
  - 处理动作：为各领域排序域补齐优先级唯一性与性能友好索引。
  - 验收点：高并发交换时数据库层面可检测冲突，索引可支持按 parent/type 等域查询。
  - 重要度：9/10

- [ ] `任务收口与一致性核对`：输出本次 TODO 与代码文件覆盖一致性
  - 范围文件：
    - [TODO.md](TODO.md)
  - 处理动作：核对每个可改文件是否被单一任务覆盖且无重复。
  - 验收点：任务项与实际待改文件一一映射，且无遗漏/重复。
  - 重要度：6/10

## 待审阅任务项

- [ ] `SYS 旧优先级命令与 controller 兼容收敛`：评估是否保留 ChangeRolePriorityCommand 与 role/legacy priority 请求 DTO
  - 范围文件：
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/command/ChangeRolePriorityCommand.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/command/ChangeRolePriorityCommand.java)
    - [sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RolePriorityRequest.java](sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RolePriorityRequest.java)
    - [sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java](sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java)
  - 处理动作：确认是否保留向后兼容路径或一次性替换。
  - 验收点：决议冻结后再进入可执行任务。
  - 重要度：8/10

## 待讨论项

- [ ] `TreeSort 与 FlatSort 边界`：确认 Menu/Department 在对外接口是否仅保留 move，或同时提供扁平重排
  - 关联任务：`SYS 控制器接口调整`
  - 决策要求：明确是否允许非树形拖拽/列表顺序与树移动并存。
  - 重要度：10/10
