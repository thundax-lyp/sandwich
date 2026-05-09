# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `service-method-test/baseline`：补齐 Service 方法规约架构测试
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceNamingArchitectureTest.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceMethodParameterArchitectureTest.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceMethodModelArchitectureTest.java
  - 处理动作：补齐 Service 方法名禁止名单、参数三态、Query/PageQuery/PageResult/Command 边界和临时放行清单测试
  - 验收点：测试能识别 `batch*`、散参数、Domain Entity 写入口参数、Controller Request、DO/DataObject 和非法多参数组合；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：10/10

- [ ] `service-method-page-model`：拆分分页输入输出模型
  - 范围文件：sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/page/PageDTO.java
  - 范围文件：sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/page/PageQuery.java
  - 范围文件：sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/page/PageResult.java
  - 范围文件：sandwish-common/sandwish-common-core/src/main/java/com/github/thundax/common/page/PageRules.java
  - 范围文件：sandwish-common/sandwish-common-web/src/main/java/com/github/thundax/common/web/response/PageResponseHelper.java
  - 处理动作：新增 PageQuery 和 PageResult，迁移 PageResponseHelper，并删除混合输入输出职责的 PageDTO
  - 验收点：common 中不再存在 PageDTO；PageQuery 只承载分页输入，PageResult 只承载分页结果；`mvn -pl sandwish-common-web -am test` 通过
  - 重要度：10/10

- [ ] `service-method-sys/dict`：规约化 DictService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DictService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DictServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/DictQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/DictServiceImplTest.java
  - 处理动作：新增 Dict Command 和 DictServiceImplTest，改造查询和写入口为 Query、PageQuery、PageResult、Command 三态，并将宽泛写方法拆成业务动作
  - 验收点：DictService 方法名不重复主体名、不含 `update/batch*` 等禁止词；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：9/10

- [ ] `service-method-admin/dict`：同步 DictController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DictController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DictIdRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DictPageRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DictQueryRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DictSaveRequest.java
  - 处理动作：将 API Request 显式组装为 Dict Query 或 Command，不把 Request 直接下沉到 Service
  - 验收点：DictController 调用新的 DictService 契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：8/10

- [ ] `service-method-sys/role`：规约化 RoleService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/RoleService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/RoleServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/RoleQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/RoleServiceImplTest.java
  - 处理动作：新增 Role Command，规约化角色创建、改名、授权、排序、状态和菜单/用户关系写入口
  - 验收点：RoleService 写方法使用业务动作名和 `*Command`；分页使用 `RoleQuery + PageQuery` 并返回 `PageResult`
  - 重要度：9/10

- [ ] `service-method-admin/role`：同步 RoleController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/RoleController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleAssignUserRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleIdRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleMenuRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RolePriorityRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleQueryRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleSaveRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleStatusRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/RoleUserRequest.java
  - 处理动作：将 Role API Request 显式组装为 Role Query 或 Command，并移除对旧 Service 写入口的调用
  - 验收点：RoleController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：8/10

- [ ] `service-method-sys/user`：规约化 UserService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/UserService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/UserServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/UserQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/UserServiceImplTest.java
  - 处理动作：新增 User Command，规约化用户创建、资料变更、状态、部门、角色和删除级联入口
  - 验收点：UserService 写入口不接收 User Entity；查询和分页符合三态参数规则
  - 重要度：10/10

- [ ] `service-method-admin/user`：同步 UserController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/UserController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserAvatarRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserCheckRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserDepartmentRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserIdRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserQueryRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserRoleRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserSaveRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/UserStatusRequest.java
  - 处理动作：将 User API Request 显式组装为 User Query 或 Command，并移除对旧 Service 写入口的调用
  - 验收点：UserController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：9/10

- [ ] `service-method-sys/menu`：规约化 MenuService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/MenuService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/MenuServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/MenuQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/MenuServiceImplTest.java
  - 处理动作：新增 Menu Command，规约化菜单创建、信息变更、显示状态、移动和删除入口
  - 验收点：MenuService 写方法使用业务动作名；菜单查询符合 Query / PageQuery / PageResult 参数和返回规则
  - 重要度：9/10

- [ ] `service-method-admin/menu`：同步 MenuController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/MenuController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/MenuDisplayRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/MenuIdRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/MenuMoveRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/MenuQueryRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/MenuSaveRequest.java
  - 处理动作：将 Menu API Request 显式组装为 Menu Query 或 Command，并移除对旧 Service 写入口的调用
  - 验收点：MenuController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：8/10

- [ ] `service-method-sys/department`：规约化 DepartmentService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/DepartmentService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/DepartmentQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/DepartmentServiceImplTest.java
  - 处理动作：新增 Department Command，规约化部门创建、信息变更、移动和删除入口
  - 验收点：DepartmentService 写入口使用 Command；方法名表达业务动作
  - 重要度：8/10

- [ ] `service-method-admin/department`：同步 DepartmentController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/DepartmentController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DepartmentIdRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DepartmentMoveRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DepartmentQueryRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/DepartmentSaveRequest.java
  - 处理动作：将 Department API Request 显式组装为 Department Query 或 Command，并移除对旧 Service 写入口的调用
  - 验收点：DepartmentController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：8/10

- [ ] `service-method-sys/current-user`：规约化 CurrentUserService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/CurrentUserService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/CurrentUserServiceImpl.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/CurrentUserServiceImplTest.java
  - 处理动作：新增当前用户相关 Command 或 Query，规约化资料、密码、头像和菜单读取入口
  - 验收点：CurrentUserService 公开方法符合参数三态；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：8/10

- [ ] `service-method-admin/current-user`：同步 CurrentUserController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/CurrentUserController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/PersonalAvatarUploadRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/PersonalInfoUpdateRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/PersonalPasswordUpdateRequest.java
  - 处理动作：将当前用户 API Request 显式组装为 Query 或 Command，并移除对旧 Service 写入口的调用
  - 验收点：CurrentUserController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：8/10

- [ ] `service-method-sys/log`：规约化 LogService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/LogService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/impl/LogServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/sys/service/query/LogQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/sys/service/impl/LogServiceImplTest.java
  - 处理动作：规约化日志查询和条件清理入口，条件删除使用 `deleteByXxx(*Query)` 窄口径，不使用 `batch*`
  - 验收点：LogService 查询和清理入口符合 Query / PageQuery / PageResult / Command 三态例外口径
  - 重要度：7/10

- [ ] `service-method-admin/log`：同步 LogController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/LogController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/sys/controller/request/LogPageRequest.java
  - 处理动作：将 Log API Request 显式组装为 Log Query 和 PageQuery，并适配条件清理入口
  - 验收点：LogController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：7/10

- [ ] `service-method-storage/storage`：规约化 StorageService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/StorageService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/StorageServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/query/StorageQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/StorageServiceImplTest.java
  - 处理动作：新增 Storage Command，规约化上传、读取、分页、删除和引用关系入口
  - 验收点：StorageService 方法参数符合三态规则；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：8/10

- [ ] `service-method-admin/storage`：同步 StorageController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/StorageController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/request/StorageIdRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/request/StoragePageRequest.java
  - 处理动作：将 Storage API Request 显式组装为 Storage Query、PageQuery 或 Command，并适配 PageResult 分页返回
  - 验收点：StorageController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：7/10

- [ ] `service-method-storage/multipart`：规约化 MultipartUploadService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/MultipartUploadService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/storage/service/impl/MultipartUploadServiceImpl.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/storage/service/impl/MultipartUploadServiceImplTest.java
  - 处理动作：新增 Multipart Upload Command，规约化初始化、分片、完成和取消入口
  - 验收点：MultipartUploadService 写入口使用业务动作名和 Command；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：8/10

- [ ] `service-method-admin/multipart`：同步 MultipartUploadController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/MultipartUploadController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/request/MultipartUploadCompleteRequest.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/storage/controller/request/MultipartUploadInitRequest.java
  - 处理动作：将分片上传 API Request 显式组装为 Multipart Upload Command
  - 验收点：MultipartUploadController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：7/10

- [ ] `service-method-auth/principal-auth`：规约化 PrincipalAuthService 和 PreAuthSessionService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PrincipalAuthService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PrincipalAuthServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PreAuthSessionService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImpl.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/auth/service/impl/PrincipalAuthServiceImplTest.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImplTest.java
  - 处理动作：新增认证运行态 Query 和 Command，规约化登录前会话、认证会话、token 创建/刷新/撤销入口
  - 验收点：PrincipalAuthService 和 PreAuthSessionService 方法参数符合三态规则；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：9/10

- [ ] `service-method-admin/auth`：同步后台 AuthController 和入口认证 Service
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/controller/AuthController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/AdminAuthService.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/auth/service/impl/AdminAuthServiceImpl.java
  - 处理动作：将后台认证 Request 显式组装为 Query 或 Command，并适配 biz auth Service 新契约
  - 验收点：后台认证入口适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：8/10

- [ ] `service-method-auth/principal-identity`：规约化 PrincipalIdentityService 和 PrincipalCredentialService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PrincipalIdentityService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PrincipalIdentityServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/PrincipalCredentialService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PrincipalCredentialServiceImpl.java
  - 处理动作：新增 Principal Identity / Credential Command，规约化标识和凭据创建、更新、锁定、清零失败次数入口
  - 验收点：PrincipalIdentityService 和 PrincipalCredentialService 写入口使用业务动作名和 Command
  - 重要度：9/10

- [ ] `service-method-member/member`：规约化 MemberService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/member/service/MemberService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/member/service/impl/MemberServiceImpl.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/member/service/query/MemberQuery.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/member/service/impl/MemberServiceImplTest.java
  - 处理动作：新增 Member Command，规约化会员创建、资料变更、状态变更和查询入口
  - 验收点：MemberService 符合 Query / PageQuery / PageResult / Command 三态规则；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：8/10

- [ ] `service-method-front/member-auth`：同步前台会员认证入口
  - 范围文件：sandwish-front-api/src/main/java/com/github/thundax/modules/auth/controller/LoginController.java
  - 范围文件：sandwish-front-api/src/main/java/com/github/thundax/modules/auth/controller/RegisterController.java
  - 范围文件：sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/MemberAuthService.java
  - 范围文件：sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberAuthServiceImpl.java
  - 范围文件：sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/MemberRegistrationService.java
  - 范围文件：sandwish-front-api/src/main/java/com/github/thundax/modules/auth/service/impl/MemberRegistrationServiceImpl.java
  - 处理动作：将前台会员认证和注册 Request 显式组装为 Query 或 Command，并适配 biz Service 新契约
  - 验收点：前台会员认证入口适配新契约；`mvn -pl sandwish-front-api -am test` 通过
  - 重要度：8/10

- [ ] `service-method-assist/async-task`：规约化 AsyncTaskService
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/AsyncTaskService.java
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImpl.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/assist/service/impl/AsyncTaskServiceImplTest.java
  - 处理动作：新增 AsyncTask Query、Command 和 AsyncTaskServiceImplTest，规约化异步任务创建、状态推进、查询和条件清理入口
  - 验收点：AsyncTaskService 符合 Query / PageQuery / PageResult / Command 三态规则；`mvn -pl sandwish-biz -am test` 通过
  - 重要度：7/10

- [ ] `service-method-admin/async-task`：同步 AsyncTaskController 入口适配
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/AsyncTaskController.java
  - 范围文件：sandwish-admin-api/src/main/java/com/github/thundax/modules/assist/controller/request/AsyncTaskIdRequest.java
  - 处理动作：将 AsyncTask API Request 显式组装为 Query 或 Command，并适配 AsyncTaskService 新契约
  - 验收点：AsyncTaskController 适配新契约；`mvn -pl sandwish-admin-api -am test` 通过
  - 重要度：7/10

- [ ] `service-method-final/guards`：收口架构约束和残留扫描
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceNamingArchitectureTest.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceMethodParameterArchitectureTest.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/architecture/ServiceMethodModelArchitectureTest.java
  - 范围文件：TODO.md
  - 范围文件：docs/30-designs/RUNBOOK-SERVICE-METHOD-REFORM.md
  - 处理动作：清理临时放行清单、删除已完成 TODO、删除 RUNBOOK，并执行最终残留扫描
  - 验收点：`mvn install` 通过；`git status --short` 干净；不残留已完成 TODO 或临时执行说明
  - 重要度：10/10

## 待讨论项
