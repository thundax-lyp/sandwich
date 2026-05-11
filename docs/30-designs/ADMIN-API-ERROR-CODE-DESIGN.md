# ADMIN API ERROR CODE DESIGN

## 1. Purpose

本文档定义后台 API 响应错误码。

后台 API error code 是后台接口协议的一部分，固定用于后台前端、第三方后台调用方和测试契约识别失败原因。

## 2. Scope

当前覆盖范围：

- `sandwish-admin-api`
- `sandwish-common-web` 输出到后台 API 的通用错误响应
- 后台 `ExceptionTranslator` 使用的 error code

当前不覆盖范围：

- 前台 API error code
- Java、Spring、MyBatis 或第三方异常类名
- 日志事件编码
- 权限编码
- i18n 文案正文维护

## 3. Format Rules

- error code 固定为 `String`。
- error code 格式固定为 `<DOMAIN>-<NUMBER>`。
- `DOMAIN` 固定使用大写业务域标识。
- `NUMBER` 固定使用 5 位数字，从 `00001` 开始递增。
- 后台和前台不共享编号空间。
- 同一入口内 error code 不得重复。
- error code 不表达 reason，不携带 HTTP status，不跟随文案变化。
- reason 固定由 `messageKey`、`defaultMessage` 和响应 `message` 表达。
- i18n `messageKey` 不要求等于 error code。

## 4. Code Table

| Code | HTTP Status | Message Key | Default Message | Trigger | Entry |
| --- | --- | --- | --- | --- | --- |
| COMMON-00001 | 400 | common.exception.bad-request | 请求参数错误 | 请求参数绑定、格式或业务前置参数无效 | admin |
| COMMON-00002 | 401 | common.exception.unauthorized | 未认证 | 缺少有效登录态或 access token 无效 | admin |
| COMMON-00003 | 403 | common.exception.forbidden | 无访问权限 | 当前用户无权限访问资源或动作 | admin |
| COMMON-00004 | 404 | common.exception.not-found | 资源不存在 | 请求的通用资源不存在 | admin |
| COMMON-00005 | 409 | common.exception.conflict | 资源状态冲突 | 通用资源状态冲突、乐观锁冲突或并发修改 | admin |
| COMMON-00006 | 500 | common.exception.system-error | 系统异常 | 未识别系统错误 | admin |
| AUTH-00001 | 400 | auth.exception.invalid-captcha | 验证码错误 | 后台登录、短信登录或验证码刷新校验失败 | admin |
| AUTH-00002 | 400 | auth.exception.invalid-username-password | 用户名或密码错误 | 后台账号密码登录失败 | admin |
| AUTH-00003 | 400 | auth.exception.invalid-password | 密码错误 | 后台当前用户密码校验失败 | admin |
| AUTH-00004 | 403 | auth.exception.banned-account | 用户已禁用 | 后台用户状态不可登录 | admin |
| AUTH-00005 | 400 | auth.exception.login-request-too-many | 登录请求过多 | 后台登录请求触发频率限制 | admin |
| AUTH-00006 | 400 | auth.exception.invalid-token | token 已失效 | 后台 access token、refresh token 或登录表单 token 无效 | admin |
| AUTH-00007 | 500 | auth.exception.oauth2-authorization-not-configured | OAuth2 authorization 未配置 | 后台 OAuth2 授权上下文缺失 | admin |
| AUTH-00008 | 400 | auth.exception.oauth2-grant-type-unsupported | OAuth2 grant type unsupported | 后台 OAuth2 grant_type 不受支持 | admin |
| AUTH-00009 | 500 | auth.exception.oauth2-client-not-configured | OAuth2 client 未配置 | 后台 OAuth2 client 不存在或未配置 | admin |
| AUTH-00010 | 400 | auth.exception.oauth2-client-secret-invalid | OAuth2 client secret invalid | 后台 OAuth2 client_secret 校验失败 | admin |
| AUTH-00011 | 400 | auth.exception.oauth2-client-request-invalid | OAuth2 client request invalid | 后台 OAuth2 client 请求参数不合法 | admin |
| AUTH-00012 | 500 | auth.exception.wecom-login-not-configured | 企业微信登录未配置 | 后台企业微信登录配置缺失 | admin |
| AUTH-00013 | 500 | auth.exception.github-login-not-configured | GitHub 登录未配置 | 后台 GitHub 登录配置缺失 | admin |
| SYS-00001 | 400 | sys.exception.invalid-parameter | 参数无效 | 后台系统管理请求参数不合法 | admin |
| SYS-00002 | 404 | sys.exception.object-not-found | 资源不存在 | 后台系统管理对象不存在 | admin |
| SYS-00003 | 409 | sys.exception.object-exists | 资源已存在 | 后台系统管理新增对象已存在 | admin |
| SYS-00004 | 400 | sys.exception.move-tree-node | 树节点移动失败 | 部门或菜单树节点移动不合法 | admin |
| SYS-00005 | 400 | sys.exception.sort-empty-input | 排序输入不能为空 | 系统管理排序输入为空 | admin |
| SYS-00006 | 400 | sys.exception.sort-missing-id | 排序实体集合与查询范围不一致 | 系统管理排序目标缺失 | admin |
| SYS-00007 | 400 | sys.exception.sort-duplicate-id | 排序实体存在重复 ID | 系统管理排序输入存在重复 ID | admin |
| SYS-00008 | 409 | sys.exception.sort-concurrent-modification | 排序存在并发修改，请重试 | 系统管理排序发生并发修改 | admin |
| SYS-00009 | 500 | sys.exception.sort-db-failure | 排序数据库异常 | 系统管理排序持久化失败 | admin |
| STORAGE-00001 | 400 | storage.exception.invalid-file | 文件无效 | 后台上传文件为空或不合法 | admin |
| STORAGE-00002 | 404 | storage.exception.object-not-found | 存储对象不存在 | 后台存储对象不存在 | admin |
| STORAGE-00003 | 400 | storage.exception.invalid-part-number | 分片序号无效 | 分片上传 partNumber 不合法 | admin |
| STORAGE-00004 | 400 | storage.exception.multipart-upload-invalid | 分片上传无效 | 分片上传会话或分片状态不合法 | admin |
| AUDIT-00001 | 400 | audit.exception.invalid-query | 审计查询参数无效 | 后台审计查询参数不合法 | admin |

## 5. Maintenance Rules

- 新增后台业务失败时，必须先在本文档登记 error code。
- 后台 `com.github.thundax.common.exception.AdminExceptionTranslator` 输出的 code 必须存在于本文档。
- 修改 `defaultMessage` 或 i18n 文案时，不得修改既有 code。
- 删除 code 前必须确认后台接口、测试和前端不再引用该 code。

## 6. Open Items

无
