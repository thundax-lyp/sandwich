# OPEN API AUTH DESIGN

## 1. Purpose

本文档定义 `sandwish-open-api` 的开放接口认证、签名、权限和上下文设计。

Open API 面向第三方系统调用。第三方系统不使用后台登录态、前台会员登录态或 OAuth 授权流程，而是使用 API KEY / API SECRET 进行无状态签名认证。

本文档固定当前共识：

- `sandwish-open-api` 是与 `sandwish-admin-api`、`sandwish-front-api` 并列的独立 API 入口模块。
- Open API 的 auth 入口适配、签名校验、防重放、上下文注入和权限校验固定归属 `sandwish-open-api`。
- `OpenClient` 是与 `User`、`Member` 对等的可认证主体主数据。
- `OpenClient` 主表固定命名为 `open_client`。
- `OpenClient` 权限表固定命名为 `open_client_permission`。
- `Principal*` 仍归属现有 `auth_*` 统一认证底座。

## 2. Scope

当前覆盖范围：

- Open API 入口模块边界。
- OpenClient 与 `auth_principal_identity` / `auth_principal_credential` 的关系。
- API KEY / API SECRET 展示与存储规则。
- HMAC 签名认证流程。
- nonce 防重放规则。
- OpenClient 权限模型。
- Open API 与 Audit 操作者上下文的衔接。

当前不覆盖范围：

- OpenClient 管理 API 的完整 request / response 设计。
- OpenClient 数据库字段完整 DDL。
- Open API 错误码完整表，见 [`OPEN-API-ERROR-CODE-DESIGN.md`](./OPEN-API-ERROR-CODE-DESIGN.md)。
- Open API SDK。
- 非 HMAC 的公私钥签名方案。
- OAuth2 授权码、client credentials 或 OIDC 流程。

## 3. Module Boundary

模块边界固定如下：

- `sandwish-open-api`
  - Open API HTTP 入口。
  - API KEY header 读取。
  - timestamp、nonce、body hash 和 signature 校验。
  - Open API auth filter / interceptor。
  - Open API permission checker。
  - Open API `SandwishContextHolder` 注入。
  - Open API 专属错误响应适配。
- `sandwish-biz`
  - `OpenClient` 领域模型、DAO 契约和业务 Service。
  - `OpenClientPermission` 领域模型、DAO 契约和查询能力。
  - 可复用的 `auth` Principal 查询、身份查询、凭据读取和凭据状态校验能力。
- `sandwish-infra`
  - `open_client` 和 `open_client_permission` 持久化实现。
  - `auth_principal_identity` 和 `auth_principal_credential` 持久化实现复用现有 auth infra。
- `sandwish-admin-api`
  - 提供 OpenClient 管理入口，例如创建、禁用、重置 secret 和维护权限。

`sandwish-open-api` 不依赖 `sandwish-admin-api` 或 `sandwish-front-api`。

## 4. Subject Model

Open API 固定引入新的可认证主体：

```text
OpenClient
```

`OpenClient` 与现有主体对等：

```text
User        -> 后台用户主体
Member      -> 前台会员主体
OpenClient  -> 开放接口第三方主体
```

认证底座继续复用 `auth_principal_identity` 和 `auth_principal_credential`：

```text
open_client.id
  -> auth_principal_identity.principal_type = OPEN_CLIENT
  -> auth_principal_identity.principal_id = open_client.id
  -> auth_principal_identity.identity_type = API_KEY
  -> auth_principal_identity.identity_value = apiKey

open_client.id
  -> auth_principal_credential.principal_type = OPEN_CLIENT
  -> auth_principal_credential.principal_id = open_client.id
  -> auth_principal_credential.identity_id = API_KEY identity id
  -> auth_principal_credential.credential_type = API_SECRET
```

固定约束：

- API KEY 是登录标识，不放入 `open_client` 主表。
- API SECRET 是认证凭据，不放入 `open_client` 主表。
- `open_client` 保存第三方主体业务资料。
- `open_client_permission` 保存第三方主体被授予的业务权限。

## 5. API SECRET Rule

API SECRET 明文固定只在创建或重置时返回一次。

系统不得提供查询 API SECRET 明文的接口。后台管理页面不得二次展示 API SECRET 明文。

这里的“一次性展示”不等于“服务端没有可验证材料”。Open API 采用 HMAC 签名时，服务端必须保存能够验证签名的材料，否则无法重新计算服务端签名。

固定取舍：

- 不使用每次请求传输 `X-Api-Secret` 明文的方案。
- 不把 API SECRET 当作普通登录密码处理。
- 不把 `credential_value` 理解为只能保存不可逆 password hash。
- HMAC 签名要求服务端保存可验证材料。
- 可验证材料固定使用 API SECRET 的加密密文。
- 服务端校验签名前解密得到 signing secret。
- 因此 API SECRET、加密密文和解密后的 signing secret 的存储、读取和日志输出必须按敏感凭据处理。

API SECRET 明文只允许在创建或重置响应中出现一次。

## 6. Request Headers

Open API HMAC 请求头固定为：

```text
X-Sandwish-Api-Key
X-Sandwish-Timestamp
X-Sandwish-Nonce
X-Sandwish-Content-SHA256
X-Sandwish-Signature
```

字段含义：

- `X-Sandwish-Api-Key`：第三方 client 的 API KEY。
- `X-Sandwish-Timestamp`：客户端发起请求的时间戳。
- `X-Sandwish-Nonce`：请求随机串，用于防重放。
- `X-Sandwish-Content-SHA256`：请求 body 的 SHA-256 摘要。
- `X-Sandwish-Signature`：使用 API SECRET 计算出的 HMAC-SHA256 签名。

## 7. Canonical Request

签名串固定包含以下部分：

```text
METHOD
PATH
QUERY_STRING
TIMESTAMP
NONCE
CONTENT_SHA256
```

规则：

- `METHOD` 使用大写 HTTP method。
- `PATH` 使用 Open API 请求 path，不包含 scheme、host 和 fragment。
- `QUERY_STRING` 使用规范化后的 query string；无 query 时使用空行。
- `TIMESTAMP` 使用 `X-Sandwish-Timestamp` 原始值。
- `NONCE` 使用 `X-Sandwish-Nonce` 原始值。
- `CONTENT_SHA256` 使用 `X-Sandwish-Content-SHA256` 原始值。

服务端必须用相同规则重建 canonical request 并计算 HMAC。

签名比较必须使用常量时间比较。

## 8. Authentication Flow

Open API 请求认证流程固定如下：

1. 读取 `X-Sandwish-Api-Key`。
2. 根据 `identity_type=API_KEY` 和 `identity_value=apiKey` 查询 `auth_principal_identity`。
3. 校验 identity 存在且状态可用。
4. 校验 identity 的 `principal_type=OPEN_CLIENT`。
5. 使用 `principal_id` 查询 `open_client`。
6. 校验 `open_client` 存在、状态可用、未过期。
7. 校验调用方 IP 是否命中 `open_client` 白名单。
8. 查询 `auth_principal_credential` 中 `credential_type=API_SECRET` 的凭据。
9. 读取服务端签名校验材料。
10. 校验 `X-Sandwish-Timestamp` 在允许时间窗内。
11. 校验 `X-Sandwish-Nonce` 在同一 API KEY 的时间窗内未使用。
12. 校验 request body SHA-256 与 `X-Sandwish-Content-SHA256` 一致。
13. 重建 canonical request。
14. 计算 HMAC-SHA256。
15. 常量时间比较服务端签名和 `X-Sandwish-Signature`。
16. 查询 `open_client_permission`。
17. 注入 OpenClient 上下文。
18. 进入 Controller 权限校验和业务处理。

任一认证步骤失败，请求不得进入业务 Service。

## 9. IP Whitelist Rule

`open_client.ipWhitelist` 固定使用 JSON array 字符串保存。

示例：

```json
["127.0.0.1", "10.0.0.0/24"]
```

固定约束：

- 空值固定表示不限制调用来源 IP。
- 空数组固定表示不限制调用来源 IP。
- 元素固定支持单 IP 和 CIDR。
- 元素不支持域名。
- JSON 解析失败固定视为 OpenClient 配置错误，请求不得进入业务 Service。

## 10. Nonce Rule

nonce 固定按 API KEY 维度防重放。

固定 key：

```text
open-api:nonce:{apiKey}:{nonce}
```

TTL 固定等于或略大于 timestamp 允许时间窗。

默认时间窗固定为：

```text
5 minutes
```

nonce 写入必须是原子“仅当不存在才写入”。如果 nonce 已存在，请求固定判定为重放请求。

首批实现使用 `sandwish-open-api` 进程内内存 TTL store，不做磁盘落盘。多节点部署前必须替换为 Redis、JetCache 或等价集中式原子存储，并保持 key 格式、TTL 和原子写入语义。

## 11. Permission Model

OpenClient 权限固定使用独立表：

```text
open_client_permission
```

不使用后台 menu / role 组合。

权限关系固定为：

```text
client_id -> permission
```

最小字段：

```text
id
client_id
permission
```

固定约束：

- `client_id + permission` 唯一。
- `permission` 使用业务模块权限码。
- Open API 不使用 `open:` 前缀表达入口类型。
- 入口类型由认证主体类型 `OPEN_CLIENT` 表达。

Submission 首批开放权限：

```text
submission:submission:create
submission:submission:page
submission:submission:change-status
submission:submission:image:upload
```

含义：

- `submission:submission:create`：允许创建 Submission。
- `submission:submission:page`：允许分页查询 Submission。
- `submission:submission:change-status`：允许调整 Submission 状态。
- `submission:submission:image:upload`：允许上传 Submission 图片。

## 12. Context And Audit

Open API 认证成功后必须注入统一上下文。

固定上下文语义：

```text
subjectType = OPEN_CLIENT
subjectId = open_client.id
principalType = OPEN_CLIENT
principalId = open_client.id
```

Audit 操作者解析需要支持：

```text
SandwishSubjectType.OPEN_CLIENT -> AuditOperatorType.OPEN_CLIENT
```

Submission 等业务写操作进入 Audit 时，操作者必须能追踪到 OpenClient。

OpenClient 来源不进入 `submission_submission` 主表。第三方来源归属认证上下文和 Audit operator 维度。

首批 Open API 不增加独立调用日志表。第三方来源固定通过认证上下文和 Audit operator 维度追踪。

## 13. Reuse And Non-Reuse

Open API 可以复用 `biz.auth` 的能力：

- PrincipalIdentity 查询。
- PrincipalCredential 查询。
- Principal / identity / credential 状态校验。
- Principal 与业务主体坐标关系。
- 认证失败的通用业务异常封装。

Open API 不复用以下流程：

- 后台 pre-auth-session。
- 验证码。
- 后台账号密码登录。
- 前台会员登录。
- access token / refresh token 会话流程。
- OAuth2 authorize / token / introspection 流程。
- 后台菜单权限会话。

Open API 的签名认证是入口模块专属流程，固定放在 `sandwish-open-api`。

## 14. Open Items

无
