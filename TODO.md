# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

### sys-dict-query

- 重要度：P1。
- 范围对象：`Dict` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Dict.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/DictDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/DictQuery.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DictController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/DictQueryRequest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/DictPageRequest.java`
- 处理动作：按 RUNBOOK 将 `Dict.Query` 迁移为服务层 `DictQuery`，Controller 装配 `DictQuery` 后调用 Service，移除当前 domain 已失效的 `Dict.Query/getQuery/setQuery`。
- 验收点：`DictController` 不再构造 `Dict.Query`；`DictServiceImpl` 不再读取 `dict.getQuery()`；后台 API package 编译通过。

### sys-menu-query

- 重要度：P1。
- 范围对象：`Menu` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/MenuService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/MenuDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/MenuQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/MenuServiceImplTest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/MenuApiController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/MenuQueryRequest.java`
- 处理动作：按 RUNBOOK 将 `Menu.Query` 迁移为服务层 `MenuQuery`，保持树查询和 `maxRank` 过滤语义，不改写移动、保存、删除动作。
- 验收点：`MenuApiController` 不再构造 `Menu.Query`；`MenuServiceImplTest` 覆盖查询对象过滤；后台 API package 编译通过。

### sys-role-query

- 重要度：P1。
- 范围对象：`Role` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/RoleDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/RoleQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/RoleServiceImplTest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleApiController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserApiController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/RoleQueryRequest.java`
- 处理动作：按 RUNBOOK 将 `Role.Query` 迁移为服务层 `RoleQuery`，同步处理 `UserApiController` 中角色列表读取调用。
- 验收点：后台 Controller 不再构造 `Role.Query`；`RoleServiceImplTest` 覆盖启用状态过滤；后台 API package 编译通过。

### sys-user-query

- 重要度：P1。
- 范围对象：`User` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/UserDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/UserQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserServiceImplTest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserApiController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/UserQueryRequest.java`
- 处理动作：按 RUNBOOK 将 `User.Query` 迁移为服务层 `UserQuery`，Controller 不再创建查询用空 `User`。
- 验收点：`UserApiController` 不再构造 `User.Query`；新增或补齐 `UserServiceImplTest` 覆盖分页过滤；后台 API package 编译通过。

### sys-office-query

- 重要度：P2。
- 范围对象：`Office` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Office.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/OfficeService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/OfficeServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/OfficeDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/OfficeQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/OfficeServiceImplTest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/OfficeApiController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/OfficeQueryRequest.java`
- 处理动作：按 RUNBOOK 将 `Office.Query` 迁移为服务层 `OfficeQuery`，不扩展组织树移动和保存动作。
- 验收点：`OfficeApiController` 不再构造 `Office.Query`；`OfficeServiceImplTest` 覆盖父级、名称、备注过滤；后台 API package 编译通过。

### sys-log-query

- 重要度：P2。
- 范围对象：`Log` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Log.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/LogService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/LogServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/dao/LogDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/LogQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/LogServiceImplTest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/LogApiController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/request/LogPageRequest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/utils/SysLogUtils.java`
- 处理动作：按 RUNBOOK 将 `Log.Query` 迁移为服务层 `LogQuery`，同步处理 `SysLogUtils` 中的查询条件创建。
- 验收点：`LogApiController` 和 `SysLogUtils` 不再构造 `Log.Query`；`LogServiceImplTest` 覆盖日志类型和时间过滤；后台 API package 编译通过。

### storage-query

- 重要度：P2。
- 范围对象：`Storage` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/Storage.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/StorageService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/dao/StorageDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/query/StorageQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/StorageController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/request/StoragePageRequest.java`
- 处理动作：按 RUNBOOK 将 `Storage.Query` 迁移为服务层 `StorageQuery`，不顺手删除 `StorageBusiness.Query`。
- 验收点：`StorageController` 不再构造 `Storage.Query`；`StorageServiceImplTest` 覆盖分页过滤；后台 API package 编译通过。

### member-query

- 重要度：P2。
- 范围对象：`Member` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/member/dao/MemberDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/member/service/query/MemberQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberServiceImplTest.java`
  - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/service/MemberAccessService.java`
  - `sandwish-front-api/src/main/java/com/github/thundax/modules/member/service/impl/MemberAccessServiceImpl.java`
- 处理动作：按 RUNBOOK 将 `Member.Query` 迁移为服务层 `MemberQuery`，确认前台会员访问服务不下沉 API 模型。
- 验收点：`MemberServiceImpl` 不再读取 `member.getQuery()`；`MemberServiceImplTest` 覆盖列表、分页、计数过滤；前台 API package 编译通过。

### assist-signature-query

- 重要度：P3。
- 范围对象：`Signature` 读取条件迁移。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/Signature.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/SignatureService.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/SignatureServiceImpl.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/dao/SignatureDao.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/query/SignatureQuery.java`
  - `sandwish-biz/src/test/java/com/github/thundax/modules/assist/service/impl/SignatureServiceImplTest.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/SignatureApiController.java`
  - `sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/request/SignaturePageRequest.java`
- 处理动作：按 RUNBOOK 将 `SignatureService.page(String businessType, Page<Signature> page)` 收敛为 `SignatureQuery` 参数，不扩展签名校验和删除动作。
- 验收点：`SignatureApiController` 使用 `SignatureQuery` 调用分页；`SignatureServiceImplTest` 覆盖 `businessType` 过滤；后台 API package 编译通过。

### service-query-runbook-cleanup

- 重要度：P1。
- 范围对象：服务层查询对象迁移现场清理。
- 范围文件：
  - `docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md`
  - `TODO.md`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Dict.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Menu.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Role.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/User.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Office.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/sys/entity/Log.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/storage/entity/Storage.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/member/entity/Member.java`
  - `sandwish-biz/src/main/java/com/github/thundax/modules/assist/entity/Signature.java`
- 处理动作：所有 domain 迁移完成后删除本 RUNBOOK，删除或收窄已完成 TODO，确认已迁移 entity 不再保留查询专用内部类和访问器。
- 验收点：`docs/30-designs/SERVICE-QUERY-OBJECT-RUNBOOK.md` 已删除；`TODO.md` 不保留已完成 Query 迁移任务；`rg "setQuery|getQuery|new .*\\.Query" sandwish-admin-api/src/main/java sandwish-front-api/src/main/java sandwish-biz/src/main/java` 不再命中已迁移 domain。
