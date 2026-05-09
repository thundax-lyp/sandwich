# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成历史保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

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
