# OPEN API ERROR CODE DESIGN

## 1. Purpose

本文档定义开放 API 响应错误码。

开放 API error code 是开放接口协议的一部分，固定用于第三方调用方、测试契约和开放接口排障识别失败原因。

## 2. Scope

当前覆盖范围：

- `sandwish-open-api`
- `sandwish-common-web` 输出到开放 API 的通用错误响应
- Open API `ExceptionTranslator` 使用的 error code
- Open API API KEY / API SECRET 签名认证失败
- Open API 初始开放的 Submission 和 Storage 业务失败

当前不覆盖范围：

- 后台 API error code
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
- 开放、后台和前台不共享编号空间。
- 同一入口内 error code 不得重复。
- error code 不表达 reason，不携带 HTTP status，不跟随文案变化。
- reason 固定由 `messageKey`、`defaultMessage` 和响应 `message` 表达。
- i18n `messageKey` 不要求等于 error code。

## 4. Code Table

| Code | HTTP Status | Message Key | Default Message | Trigger | Entry |
| --- | --- | --- | --- | --- | --- |
| COMMON-00001 | 400 | common.exception.bad-request | 请求参数错误 | 请求参数绑定、格式或业务前置参数无效 | open |
| COMMON-00002 | 401 | common.exception.unauthorized | 未认证 | Open API 认证信息缺失或无效 | open |
| COMMON-00003 | 403 | common.exception.forbidden | 无访问权限 | OpenClient 无权限访问资源或动作 | open |
| COMMON-00004 | 404 | common.exception.not-found | 资源不存在 | 请求的通用资源不存在 | open |
| COMMON-00005 | 409 | common.exception.conflict | 资源状态冲突 | 通用资源状态冲突、乐观锁冲突或并发修改 | open |
| COMMON-00006 | 500 | common.exception.system-error | 系统异常 | 未识别系统错误 | open |
| AUTH-00001 | 401 | auth.exception.missing-api-key | 缺少 API KEY | 请求未携带 `X-Sandwish-Api-Key` | open |
| AUTH-00002 | 401 | auth.exception.invalid-api-key | API KEY 无效 | API KEY 不存在、禁用或不属于 OpenClient | open |
| AUTH-00003 | 401 | auth.exception.open-client-unavailable | OpenClient 不可用 | OpenClient 禁用或已过期 | open |
| AUTH-00004 | 403 | auth.exception.ip-not-allowed | IP 不在白名单内 | 请求来源 IP 未命中 OpenClient 白名单 | open |
| AUTH-00005 | 401 | auth.exception.invalid-timestamp | timestamp 无效 | timestamp 缺失、格式错误或超出时间窗 | open |
| AUTH-00006 | 401 | auth.exception.replayed-nonce | nonce 已使用 | 同一 API KEY 时间窗内重复 nonce | open |
| AUTH-00007 | 401 | auth.exception.invalid-content-sha256 | body 摘要无效 | body SHA-256 与请求头不一致 | open |
| AUTH-00008 | 401 | auth.exception.invalid-signature | 签名无效 | HMAC 签名校验失败 | open |
| AUTH-00009 | 500 | auth.exception.api-secret-not-configured | API SECRET 未配置 | OpenClient 缺少可用 API SECRET 凭据 | open |
| SUBMISSION-00001 | 400 | submission.exception.invalid-parameter | 提交内容参数无效 | Open API 创建提交内容请求不合法 | open |
| STORAGE-00001 | 400 | storage.exception.invalid-file | 文件无效 | Open API 上传图片为空或不合法 | open |

## 5. Maintenance Rules

- 新增开放接口业务失败时，必须先在本文档登记 error code。
- Open API `ExceptionTranslator` 输出的 code 必须存在于本文档。
- 修改 `defaultMessage` 或 i18n 文案时，不得修改既有 code。
- 删除 code 前必须确认开放接口、测试和第三方契约不再引用该 code。

## 6. Open Items

无
