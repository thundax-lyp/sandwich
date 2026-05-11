# FRONT API ERROR CODE DESIGN

## 1. Purpose

本文档定义前台 API 响应错误码。

前台 API error code 是前台接口协议的一部分，固定用于前台应用、第三方前台调用方和测试契约识别失败原因。

## 2. Scope

当前覆盖范围：

- `sandwish-front-api`
- `sandwish-common-web` 输出到前台 API 的通用错误响应
- 前台 `ExceptionTranslator` 使用的 error code

当前不覆盖范围：

- 后台 API error code
- Java、Spring、MyBatis 或第三方异常类名
- 日志事件编码
- 权限编码
- i18n 文案正文维护

## 3. Format Rules

- error code 固定为 `String`。
- error code 格式固定为 `<DOMAIN>-<NUMBER>`。
- `DOMAIN` 固定使用大写业务域标识。
- `NUMBER` 固定使用 5 位数字，从 `00001` 开始递增。
- 前台和后台不共享编号空间。
- 同一入口内 error code 不得重复。
- error code 不表达 reason，不携带 HTTP status，不跟随文案变化。
- reason 固定由 `messageKey`、`defaultMessage` 和响应 `message` 表达。
- i18n `messageKey` 不要求等于 error code。

## 4. Code Table

| Code | HTTP Status | Message Key | Default Message | Trigger | Entry |
| --- | --- | --- | --- | --- | --- |
| COMMON-00001 | 400 | common.exception.bad-request | 请求参数错误 | 请求参数绑定、格式或业务前置参数无效 | front |
| COMMON-00002 | 401 | common.exception.unauthorized | 未认证 | 缺少有效会员登录态或 access token 无效 | front |
| COMMON-00003 | 403 | common.exception.forbidden | 无访问权限 | 当前会员无权限访问资源或动作 | front |
| COMMON-00004 | 404 | common.exception.not-found | 资源不存在 | 请求的通用资源不存在 | front |
| COMMON-00005 | 409 | common.exception.conflict | 资源状态冲突 | 通用资源状态冲突、乐观锁冲突或并发修改 | front |
| COMMON-00006 | 500 | common.exception.system-error | 系统异常 | 未识别系统错误 | front |
| AUTH-00001 | 400 | auth.exception.invalid-captcha | 图形验证码错误 | 前台注册或登录图形验证码校验失败 | front |
| AUTH-00002 | 400 | auth.exception.invalid-sms-code | 短信验证码错误 | 前台短信注册或短信登录验证码校验失败 | front |
| AUTH-00003 | 400 | auth.exception.invalid-email-code | 邮箱验证码错误 | 前台邮箱注册验证码校验失败 | front |
| AUTH-00004 | 400 | auth.exception.invalid-username-password | 用户名或密码错误 | 前台账号密码登录失败 | front |
| AUTH-00005 | 400 | auth.exception.refresh-token-expired | refreshToken 已失效 | 前台 refresh token 过期或不存在 | front |
| AUTH-00006 | 400 | auth.exception.access-token-expired | accessToken 已失效 | 前台 access token 过期或不存在 | front |
| AUTH-00007 | 400 | auth.exception.login-request-too-many | 登录请求过多 | 前台登录请求触发频率限制 | front |
| AUTH-00008 | 400 | auth.exception.login-form-key-expired | 登录表单密钥已失效 | 前台登录表单 RSA 密钥不存在或过期 | front |
| AUTH-00009 | 400 | auth.exception.login-form-expired | 登录表单已失效 | 前台登录表单会话不存在或过期 | front |
| MEMBER-00001 | 409 | member.exception.identity-exists | 会员标识已存在 | 前台注册身份标识已存在 | front |
| MEMBER-00002 | 403 | member.exception.disabled | 会员状态不可用 | 前台会员状态不可登录或不可操作 | front |
| MEMBER-00003 | 400 | member.exception.required-field-empty | 必填字段不能为空 | 前台会员注册必填字段为空 | front |
| MEMBER-00004 | 400 | member.exception.sort-empty-input | 排序输入不能为空 | 前台会员排序输入为空 | front |
| MEMBER-00005 | 400 | member.exception.sort-missing-id | 排序实体集合与查询范围不一致 | 前台会员排序目标缺失 | front |
| MEMBER-00006 | 400 | member.exception.sort-duplicate-id | 排序实体存在重复 ID | 前台会员排序输入存在重复 ID | front |
| MEMBER-00007 | 409 | member.exception.sort-concurrent-modification | 排序存在并发修改，请重试 | 前台会员排序发生并发修改 | front |
| MEMBER-00008 | 500 | member.exception.sort-db-failure | 排序数据库异常 | 前台会员排序持久化失败 | front |

## 5. Maintenance Rules

- 新增前台业务失败时，必须先在本文档登记 error code。
- 前台 `FrontExceptionTranslator` 输出的 code 必须存在于本文档。
- 修改 `defaultMessage` 或 i18n 文案时，不得修改既有 code。
- 删除 code 前必须确认前台接口、测试和前端不再引用该 code。

## 6. Open Items

无
