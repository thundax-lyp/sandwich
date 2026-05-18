# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `admin-it`：补齐后台认证与当前用户集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/auth/AdminAuthSessionIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminCurrentUserIT.java
  - 处理动作：覆盖后台登录、验证码白名单、token refresh、logout、权限失败、会话失效、当前用户信息、更新、密码、头像、菜单和权限接口
  - 验收点：auth 和 CurrentUserController 关键链路通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `admin-sys-it`：补齐用户与角色集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminUserQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminUserMutationIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminRoleQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminRoleMutationIT.java
  - 处理动作：覆盖 UserController 和 RoleController 的查询、写入、状态、排序、头像、角色分配和失败链路
  - 验收点：用户与角色全部业务接口通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `admin-sys-it`：补齐菜单、部门、字典和日志集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminMenuQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminMenuMutationIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDepartmentQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDepartmentMutationIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDictQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDictMutationIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminLogIT.java
  - 处理动作：覆盖菜单、部门、字典、日志的查询、写入、移动、排序、删除和失败链路
  - 验收点：MenuController、DepartmentController、DictController、LogController 全部业务接口通过真实 HTTP 入口完成断言
  - 重要度：10/10

- [ ] `admin-open-storage-it`：补齐 OpenClient 与存储集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/open/AdminOpenClientQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/open/AdminOpenClientMutationIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/storage/AdminStorageObjectQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/storage/AdminStorageObjectMutationIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/storage/AdminMultipartUploadIT.java
  - 处理动作：覆盖 OpenClient 查询/写入/密钥重置、存储对象查询/上传/删除/排序、分片上传完整链路
  - 验收点：OpenClientController、StorageController、MultipartUploadController 全部业务接口通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `admin-submission-audit-it`：补齐提交内容与审计集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/submission/AdminSubmissionQueryIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/submission/AdminSubmissionMutationIT.java
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/audit/AdminAuditLogIT.java
  - 处理动作：覆盖 submission 查询、创建、状态变更、删除、排序、图片上传，以及 audit meta、history、detail、object overview、object page、page、options 和 fields 接口
  - 验收点：SubmissionController 和 AuditController 全部业务接口通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `front-it`：补齐前台认证与注册集成测试
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontAuthSessionIT.java
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterAccountIT.java
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterMobileIT.java
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterEmailIT.java
  - 处理动作：覆盖预认证会话、账号登录、短信登录替身链路、token refresh、login status、check-login、logout、账号/手机号/邮箱注册接口
  - 验收点：LoginController 和会员注册链路具备成功链路；验证码、凭据、token、重复账号/手机号/邮箱失败链路具备断言
  - 重要度：10/10

- [ ] `open-api-it`：补齐开放接口签名与提交集成测试
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/auth/OpenApiSignatureIT.java
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionQueryIT.java
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionMutationIT.java
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionUploadIT.java
  - 处理动作：覆盖签名成功、签名失败、nonce 重放、IP 白名单失败、开放 submission 查询/创建/状态变更/图片上传接口
  - 验收点：OpenAPI 认证过滤链路和开放提交业务接口通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `integration-docs`：补齐接口覆盖清单和最终运行说明
  - 范围文件：deploy/integration/API-COVERAGE.md
  - 范围文件：deploy/integration/README.md
  - 处理动作：新增 Controller 接口到 `*IT.java` 的覆盖映射清单，并同步最终命令、数据装载、覆盖清单和排错说明
  - 验收点：admin、front、open 全部业务接口均能在清单中定位到集成测试文件；README 与实际 compose、SQL、profile 和 Maven 命令一致
  - 重要度：10/10

- [ ] `integration-docker`：同步 Docker 运行配置
  - 范围文件：deploy/docker-compose.yml
  - 范围文件：deploy/images/admin-api.Dockerfile
  - 范围文件：deploy/images/front-api.Dockerfile
  - 范围文件：deploy/images/open-api.Dockerfile
  - 处理动作：代码和 profile 变更完成后同步 API Docker 运行配置
  - 验收点：Docker 运行配置能加载最新 jar、profile 和集成测试相关运行参数
  - 重要度：9/10

- [ ] `integration-image-files`：同步 API 镜像 tar 文件
  - 范围文件：deploy/image-files/manifest.txt
  - 范围文件：deploy/image-files/sandwish-admin-api-dev.tar
  - 范围文件：deploy/image-files/sandwish-front-api-dev.tar
  - 范围文件：deploy/image-files/sandwish-open-api-dev.tar
  - 范围文件：deploy/image-files/sandwish-nginx-dev.tar
  - 处理动作：代码和 Docker 配置变更完成后重建并同步 API 相关镜像 tar
  - 验收点：`deploy/image-files/*.tar` 与最新代码、Dockerfile 和 manifest 一致
  - 重要度：9/10

- [ ] `integration-cleanup`：清理集成测试 RUNBOOK
  - 范围文件：docs/30-designs/RUNBOOK-INTEGRATION-TEST.md
  - 范围文件：TODO.md
  - 处理动作：集成测试体系完成后删除 RUNBOOK 并删除或收窄对应 TODO
  - 验收点：无残留 RUNBOOK 引用，`TODO.md` 不保留已完成任务
  - 重要度：7/10

## 待讨论项
