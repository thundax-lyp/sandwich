# Integration API Coverage

本文档记录业务 Controller 到集成测试用例的覆盖关系。鉴权类 OAuth 第三方登录端点允许例外；其他业务接口需要能定位到对应 `*IT.java`。

## Admin API

| Controller | Endpoints | Integration tests |
| --- | --- | --- |
| `AuthController` | `POST /api/auth/session/pre-auth-session`, `pre-auth-session/refresh`, `login`, `login/sms`, `logout`, `token/verify`, `token/refresh` | `AdminAuthSessionIT` |
| `AuthController` | `POST /api/auth/session/login/wecom`, `login/github`, `oauth2/introspect`, `oauth2/userinfo`, `oauth2/authorize`, `oauth2/decision`, `oauth2/token`, `oauth2/revoke` | 鉴权扩展端点，当前按例外处理 |
| `CaptchaController` | `GET /api/auth/captcha`, `POST /api/auth/captcha/refresh` | `AdminAuthSessionIT` |
| `CurrentUserController` | `POST /api/sys/current-user/info`, `info/update`, `password/update`, `avatar/upload`, `avatar/delete`, `menus`, `perms` | `AdminCurrentUserIT` |
| `DepartmentController` | `POST /api/sys/department/get`, `list`, `tree` | `AdminDepartmentQueryIT` |
| `DepartmentController` | `POST /api/sys/department/create`, `update`, `move`, `delete` | `AdminDepartmentMutationIT` |
| `DictController` | `POST /api/sys/dict/get`, `list`, `page` | `AdminDictQueryIT` |
| `DictController` | `POST /api/sys/dict/create`, `update`, `sort`, `delete` | `AdminDictMutationIT` |
| `MenuController` | `POST /api/sys/menu/get`, `list`, `tree` | `AdminMenuQueryIT` |
| `MenuController` | `POST /api/sys/menu/create`, `update`, `display`, `move`, `delete` | `AdminMenuMutationIT` |
| `RoleController` | `POST /api/sys/role/get`, `list`, `menu/tree`, `user/tree`, `user/list` | `AdminRoleQueryIT` |
| `RoleController` | `POST /api/sys/role/create`, `update`, `enable`, `sort`, `user/assign`, `delete` | `AdminRoleMutationIT` |
| `UserController` | `POST /api/sys/user/get`, `list`, `page`, `check`, `department/tree`, `role/list`; `GET /api/sys/user/avatar` | `AdminUserQueryIT` |
| `UserController` | `POST /api/sys/user/create`, `update`, `avatar/upload`, `avatar/delete`, `avatar`, `enable`, `delete` | `AdminUserMutationIT` |
| `LogController` | `POST /api/sys/log/page` | `AdminLogIT` |
| `OpenClientController` | `POST /api/open/client/page`, `get` | `AdminOpenClientQueryIT` |
| `OpenClientController` | `POST /api/open/client/create`, `update`, `change-status`, `secret/reset` | `AdminOpenClientMutationIT` |
| `StorageController` | `POST /api/storage/object/page`, `tree`; `GET /api/storage/object/{id}/content` | `AdminStorageObjectQueryIT` |
| `StorageController` | `POST /api/storage/object/upload`, `delete`, `sort` | `AdminStorageObjectMutationIT` |
| `MultipartUploadController` | `POST /api/storage/multipart-upload`, `{uploadId}/parts`, `{uploadId}/complete`, `{uploadId}/abort` | `AdminMultipartUploadIT` |
| `SubmissionController` | `POST /api/submission/submission/get`, `page` | `AdminSubmissionQueryIT` |
| `SubmissionController` | `POST /api/submission/submission/create`, `change-status`, `delete`, `sort`, `image/upload` | `AdminSubmissionMutationIT` |
| `AuditController` | `POST /api/audit/log/meta`, `history`, `detail`, `object/overview`, `object/page`, `page`, `options`, `fields` | `AdminAuditLogIT` |

## Front API

| Controller | Endpoints | Integration tests |
| --- | --- | --- |
| `LoginController` | `POST /api/auth/session/pre-auth-session`, `pre-auth-session/refresh`, `login`, `login/sms`, `token/refresh`, `login/status`, `check-login`, `logout` | `FrontAuthSessionIT` |
| `RegisterController` | `POST /api/auth/register/account` | `FrontRegisterAccountIT` |
| `RegisterController` | `POST /api/auth/register/mobile/code`, `mobile` | `FrontRegisterMobileIT` |
| `RegisterController` | `POST /api/auth/register/email/code`, `email` | `FrontRegisterEmailIT` |

## Open API

| Controller | Endpoints | Integration tests |
| --- | --- | --- |
| `OpenApiAuthenticationFilter` | 签名成功、签名失败、nonce 重放、IP 白名单失败 | `OpenApiSignatureIT` |
| `SubmissionController` | `POST /api/submission/submission/page` | `OpenSubmissionQueryIT` |
| `SubmissionController` | `POST /api/submission/submission/create`, `change-status` | `OpenSubmissionMutationIT` |
| `SubmissionController` | `POST /api/submission/submission/image/upload` | `OpenSubmissionUploadIT` |

## Data Fixtures

共享数据来自 `deploy/integration/db/10-baseline/`：

- `001-admin-user.sql`、`002-admin-role-menu-permission.sql`：后台用户、角色、菜单和权限。
- `003-department.sql`、`004-dict.sql`：系统基础业务数据。
- `005-open-client.sql`：开放平台固定客户端、密钥、权限和 IP 白名单。
- `006-member.sql`：前台会员、登录标识和认证凭据。
- `007-storage.sql`、`008-submission.sql`、`009-audit.sql`：存储、提交内容和审计数据。

清理脚本来自 `deploy/integration/db/90-cleanup/`，每个 IT 的 `prepareIntegrationData()` 会先清理 SQL、Redis key 和本地 OSS 临时目录，再装载 baseline。
