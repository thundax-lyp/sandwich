# TODO List

## 说明

- `TODO.md` 是未关闭任务面板，不是完成历史。
- 宏观任务必须先讨论边界，再拆解为可执行 TODO。
- 已完成任务必须删除，不在 `TODO.md` 中打勾长期保留。
- 完成记录保留在 commit 或 PR 中。

## 当前任务项

## 待审阅任务项

- [ ] `captcha`：增加验证码值白名单配置模型
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/model/CaptchaWhitelistProperties.java
  - 处理动作：新增按验证码值配置白名单的模型
  - 验收点：模型能表达 `6666`、`8888` 等验证码值白名单
  - 重要度：8/10

- [ ] `captcha`：实现验证码值白名单校验
  - 范围文件：sandwish-biz/src/main/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImpl.java
  - 范围文件：sandwish-biz/src/test/java/com/github/thundax/modules/auth/service/impl/PreAuthSessionServiceImplTest.java
  - 处理动作：在验证码校验链路中增加仅 integration profile 生效的验证码值白名单
  - 验收点：白名单值放行、非白名单值走原校验、非 integration profile 不放行
  - 重要度：10/10

- [ ] `integration-support`：增加集成测试 HTTP 客户端
  - 范围文件：sandwish-common/sandwish-common-test/src/main/java/com/github/thundax/common/test/integration/IntegrationHttpClient.java
  - 处理动作：封装 JSON POST、GET 和 multipart 请求
  - 验收点：admin、front、open 集成测试能复用同一 HTTP 客户端
  - 重要度：9/10

- [ ] `integration-support`：增加集成测试认证客户端
  - 范围文件：sandwish-common/sandwish-common-test/src/main/java/com/github/thundax/common/test/integration/IntegrationAuthClient.java
  - 处理动作：封装 admin 和 front 登录、token refresh、logout 测试调用
  - 验收点：入口集成测试能通过该客户端获取测试 token
  - 重要度：9/10

- [ ] `integration-support`：增加集成测试数据库脚本执行器
  - 范围文件：sandwish-common/sandwish-common-test/src/main/java/com/github/thundax/common/test/integration/IntegrationDatabaseScriptRunner.java
  - 处理动作：封装 cleanup、baseline、scenario SQL 的执行入口
  - 验收点：测试类能按固定顺序重置并装载集成测试数据
  - 重要度：10/10

- [ ] `integration-support`：增加 Redis 测试清理器
  - 范围文件：sandwish-common/sandwish-common-test/src/main/java/com/github/thundax/common/test/integration/IntegrationRedisCleaner.java
  - 处理动作：封装按 integration prefix 清理 Redis key 的能力
  - 验收点：集成测试前后能清理测试 Redis key 且不影响其他前缀
  - 重要度：8/10

- [ ] `integration-support`：增加本地 OSS 测试清理器
  - 范围文件：sandwish-common/sandwish-common-test/src/main/java/com/github/thundax/common/test/integration/IntegrationOssCleaner.java
  - 处理动作：封装集成测试本地 OSS 临时目录清理能力
  - 验收点：上传类集成测试结束后临时对象可清理
  - 重要度：8/10

- [ ] `integration-support`：增加 admin API 集成测试基类
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/AbstractAdminApiIT.java
  - 处理动作：新增 admin 入口 HTTP 端口、token、数据重置和 profile guard 基类
  - 验收点：admin 入口 `*IT.java` 能继承基类完成登录和数据准备
  - 重要度：9/10

- [ ] `integration-support`：增加 front API 集成测试基类
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/AbstractFrontApiIT.java
  - 处理动作：新增 front 入口 HTTP 端口、token、数据重置和 profile guard 基类
  - 验收点：front 入口 `*IT.java` 能继承基类完成登录和数据准备
  - 重要度：9/10

- [ ] `integration-support`：增加 open API 集成测试基类
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/AbstractOpenApiIT.java
  - 处理动作：新增 open 入口 HTTP 端口、签名、数据重置和 profile guard 基类
  - 验收点：open 入口 `*IT.java` 能继承基类完成签名请求和数据准备
  - 重要度：9/10

- [ ] `integration-data`：补充后台用户 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/001-admin-user.sql
  - 处理动作：新增后台集成测试管理员和普通用户数据
  - 验收点：admin 登录、当前用户、用户查询链路具备稳定测试账号
  - 重要度：10/10

- [ ] `integration-data`：补充后台角色菜单权限 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/002-admin-role-menu-permission.sql
  - 处理动作：新增角色、菜单、权限、用户角色和角色菜单关系数据
  - 验收点：admin 权限成功和权限失败链路具备稳定数据
  - 重要度：10/10

- [ ] `integration-data`：补充部门 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/003-department.sql
  - 处理动作：新增部门树测试数据
  - 验收点：部门列表、树、移动和用户部门查询链路具备稳定数据
  - 重要度：8/10

- [ ] `integration-data`：补充字典 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/004-dict.sql
  - 处理动作：新增字典类型和值测试数据
  - 验收点：字典列表、分页、创建、更新、删除和排序链路具备稳定参照数据
  - 重要度：8/10

- [ ] `integration-data`：补充 OpenClient baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/005-open-client.sql
  - 处理动作：新增 OpenClient、权限、密钥和 IP 白名单测试数据
  - 验收点：admin open client 和 open-api 签名链路具备稳定数据
  - 重要度：10/10

- [ ] `integration-data`：补充会员 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/006-member.sql
  - 处理动作：新增前台会员、登录标识、认证凭据和会话测试数据
  - 验收点：front 登录、注册冲突和 token refresh 链路具备稳定数据
  - 重要度：10/10

- [ ] `integration-data`：补充存储 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/007-storage.sql
  - 处理动作：新增存储对象、对象引用和分片上传测试数据
  - 验收点：storage 查询、下载、删除、排序和 multipart 链路具备稳定数据
  - 重要度：9/10

- [ ] `integration-data`：补充提交内容 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/008-submission.sql
  - 处理动作：新增 submission 和 submission image 测试数据
  - 验收点：admin 和 open submission 查询、状态变更、删除和图片链路具备稳定数据
  - 重要度：9/10

- [ ] `integration-data`：补充审计 baseline 数据
  - 范围文件：deploy/integration/db/10-baseline/009-audit.sql
  - 处理动作：新增审计元数据、审计日志和审计对象测试数据
  - 验收点：audit meta、history、detail、page、options、fields 链路具备稳定数据
  - 重要度：9/10

- [ ] `integration-data`：补充业务数据 cleanup 脚本
  - 范围文件：deploy/integration/db/90-cleanup/001-clean-business-data.sql
  - 处理动作：新增 sys、open、storage、submission、audit、member 业务测试数据清理脚本
  - 验收点：脚本可重复执行且清理集成测试业务数据
  - 重要度：10/10

- [ ] `integration-data`：补充认证数据 cleanup 脚本
  - 范围文件：deploy/integration/db/90-cleanup/002-clean-auth-data.sql
  - 处理动作：新增 token、会话、验证码、nonce 和登录事件测试数据清理脚本
  - 验收点：脚本可重复执行且清理集成测试认证运行态数据
  - 重要度：10/10

- [ ] `admin-auth-it`：补充后台认证集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/auth/AdminAuthSessionIT.java
  - 处理动作：覆盖后台登录、验证码白名单、token refresh、logout、权限失败和会话失效链路
  - 验收点：auth 关键链路通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `admin-sys-it`：补充当前用户集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminCurrentUserIT.java
  - 处理动作：覆盖当前用户信息、更新、密码、头像、菜单和权限接口
  - 验收点：CurrentUserController 全部业务接口具备成功链路，密码和头像接口具备失败链路
  - 重要度：10/10

- [ ] `admin-sys-it`：补充用户查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminUserQueryIT.java
  - 处理动作：覆盖用户 get、list、page、check、department tree、role list 和头像访问接口
  - 验收点：UserController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：9/10

- [ ] `admin-sys-it`：补充用户写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminUserMutationIT.java
  - 处理动作：覆盖用户 create、update、enable、delete、avatar upload 和 avatar delete 接口
  - 验收点：UserController 写入类接口具备成功链路，重复账号、无效部门、删除约束和上传错误具备失败链路
  - 重要度：10/10

- [ ] `admin-sys-it`：补充角色查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminRoleQueryIT.java
  - 处理动作：覆盖角色 get、list、menu tree、user tree 和 user list 接口
  - 验收点：RoleController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：9/10

- [ ] `admin-sys-it`：补充角色写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminRoleMutationIT.java
  - 处理动作：覆盖角色 create、update、enable、sort、delete 和 user assign 接口
  - 验收点：RoleController 写入类接口具备成功链路，重复编码、删除约束和无效分配具备失败链路
  - 重要度：10/10

- [ ] `admin-sys-it`：补充菜单查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminMenuQueryIT.java
  - 处理动作：覆盖菜单 get、list 和 tree 接口
  - 验收点：MenuController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：8/10

- [ ] `admin-sys-it`：补充菜单写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminMenuMutationIT.java
  - 处理动作：覆盖菜单 create、update、display、delete 和 move 接口
  - 验收点：MenuController 写入类接口具备成功链路，重复编码、层级错误和删除约束具备失败链路
  - 重要度：9/10

- [ ] `admin-sys-it`：补充部门查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDepartmentQueryIT.java
  - 处理动作：覆盖部门 get、list 和 tree 接口
  - 验收点：DepartmentController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：8/10

- [ ] `admin-sys-it`：补充部门写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDepartmentMutationIT.java
  - 处理动作：覆盖部门 create、update、delete 和 move 接口
  - 验收点：DepartmentController 写入类接口具备成功链路，层级错误和删除约束具备失败链路
  - 重要度：9/10

- [ ] `admin-sys-it`：补充字典查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDictQueryIT.java
  - 处理动作：覆盖字典 get、list 和 page 接口
  - 验收点：DictController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：8/10

- [ ] `admin-sys-it`：补充字典写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminDictMutationIT.java
  - 处理动作：覆盖字典 create、update、delete 和 sort 接口
  - 验收点：DictController 写入类接口具备成功链路，重复值和删除约束具备失败链路
  - 重要度：9/10

- [ ] `admin-sys-it`：补充日志集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/sys/AdminLogIT.java
  - 处理动作：覆盖系统日志 page 接口
  - 验收点：LogController 业务接口通过真实 HTTP 入口完成分页断言
  - 重要度：7/10

- [ ] `admin-open-it`：补充 OpenClient 查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/open/AdminOpenClientQueryIT.java
  - 处理动作：覆盖 OpenClient page 和 get 接口
  - 验收点：OpenClientController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：8/10

- [ ] `admin-open-it`：补充 OpenClient 写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/open/AdminOpenClientMutationIT.java
  - 处理动作：覆盖 OpenClient create、update、change-status 和 secret reset 接口
  - 验收点：OpenClientController 写入类接口具备成功链路，重复 client 和无效状态具备失败链路
  - 重要度：9/10

- [ ] `admin-storage-it`：补充存储对象查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/storage/AdminStorageObjectQueryIT.java
  - 处理动作：覆盖对象 page、content 和 tree 接口
  - 验收点：StorageController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：9/10

- [ ] `admin-storage-it`：补充存储对象写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/storage/AdminStorageObjectMutationIT.java
  - 处理动作：覆盖对象 upload、delete 和 sort 接口
  - 验收点：StorageController 写入类接口具备成功链路，非法文件、缺失对象和删除约束具备失败链路
  - 重要度：10/10

- [ ] `admin-storage-it`：补充分片上传集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/storage/AdminMultipartUploadIT.java
  - 处理动作：覆盖 multipart initiate、upload part、complete 和 abort 接口
  - 验收点：MultipartUploadController 全部业务接口具备成功链路，缺失分片和无效 uploadId 具备失败链路
  - 重要度：10/10

- [ ] `admin-submission-it`：补充提交内容查询集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/submission/AdminSubmissionQueryIT.java
  - 处理动作：覆盖 submission page 和 get 接口
  - 验收点：SubmissionController 查询类接口均通过真实 HTTP 入口完成断言
  - 重要度：8/10

- [ ] `admin-submission-it`：补充提交内容写入集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/submission/AdminSubmissionMutationIT.java
  - 处理动作：覆盖 submission create、change-status、delete、sort 和 image upload 接口
  - 验收点：SubmissionController 写入类接口具备成功链路，无效状态、缺失对象和非法图片具备失败链路
  - 重要度：10/10

- [ ] `admin-audit-it`：补充审计日志集成测试
  - 范围文件：sandwish-admin-api/src/test/java/com/github/thundax/integration/audit/AdminAuditLogIT.java
  - 处理动作：覆盖 audit meta、history、detail、object overview、object page、page、options 和 fields 接口
  - 验收点：AuditController 全部业务接口通过真实 HTTP 入口完成断言
  - 重要度：9/10

- [ ] `front-auth-it`：补充前台认证会话集成测试
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontAuthSessionIT.java
  - 处理动作：覆盖预认证会话、账号登录、短信登录替身链路、token refresh、login status、check-login 和 logout 接口
  - 验收点：LoginController 关键业务接口具备成功链路，验证码、凭据和 token 失败链路具备断言
  - 重要度：10/10

- [ ] `front-register-it`：补充账号注册集成测试
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterAccountIT.java
  - 处理动作：覆盖账号注册接口
  - 验收点：账号注册成功链路和重复账号失败链路通过真实 HTTP 入口完成断言
  - 重要度：9/10

- [ ] `front-register-it`：补充手机号注册集成测试
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterMobileIT.java
  - 处理动作：覆盖手机号验证码和手机号注册接口
  - 验收点：手机号验证码白名单成功、非白名单失败和重复手机号失败链路完成断言
  - 重要度：9/10

- [ ] `front-register-it`：补充邮箱注册集成测试
  - 范围文件：sandwish-front-api/src/test/java/com/github/thundax/integration/auth/FrontRegisterEmailIT.java
  - 处理动作：覆盖邮箱验证码和邮箱注册接口
  - 验收点：邮箱验证码白名单成功、非白名单失败和重复邮箱失败链路完成断言
  - 重要度：9/10

- [ ] `open-api-it`：补充 OpenAPI 签名集成测试
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/auth/OpenApiSignatureIT.java
  - 处理动作：覆盖签名成功、签名失败、nonce 重放和 IP 白名单失败链路
  - 验收点：OpenAPI 认证过滤链路通过真实 HTTP 入口完成成功和失败断言
  - 重要度：10/10

- [ ] `open-submission-it`：补充开放提交查询集成测试
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionQueryIT.java
  - 处理动作：覆盖开放 submission page 接口
  - 验收点：开放提交查询接口通过签名请求完成分页断言
  - 重要度：8/10

- [ ] `open-submission-it`：补充开放提交写入集成测试
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionMutationIT.java
  - 处理动作：覆盖开放 submission create 和 change-status 接口
  - 验收点：开放提交写入接口具备成功链路，无效状态和无权限 client 具备失败链路
  - 重要度：9/10

- [ ] `open-submission-it`：补充开放提交图片上传集成测试
  - 范围文件：sandwish-open-api/src/test/java/com/github/thundax/integration/submission/OpenSubmissionUploadIT.java
  - 处理动作：覆盖开放 submission image upload 接口
  - 验收点：开放图片上传具备成功链路，非法图片和签名失败具备失败链路
  - 重要度：9/10

- [ ] `integration-coverage`：补充接口覆盖清单
  - 范围文件：deploy/integration/API-COVERAGE.md
  - 处理动作：新增 Controller 接口到 `*IT.java` 的覆盖映射清单
  - 验收点：admin、front、open 全部业务接口均能在清单中定位到集成测试文件
  - 重要度：10/10

- [ ] `integration-readme`：同步集成测试最终运行说明
  - 范围文件：deploy/integration/README.md
  - 处理动作：在集成测试能力落齐后补充最终命令、数据装载、覆盖清单和排错说明
  - 验收点：README 与实际 compose、SQL、profile 和 Maven 命令一致
  - 重要度：8/10

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
